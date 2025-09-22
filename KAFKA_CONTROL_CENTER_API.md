# Kafka Control Center API 명세서

## 개요
Kafka Control Center의 컴포넌트 구성 기능을 제공하는 REST API입니다. MANAGER 권한을 가진 사용자(sd 유저)가 Kafka 클러스터의 다양한 컴포넌트를 관리할 수 있습니다.

## 인증
- JWT 토큰 기반 인증
- Authorization 헤더에 `Bearer {token}` 형태로 전달
- MANAGER 권한 필요

## Base URL
```
http://localhost:8080/api/kafka
```

---

## 1. 토픽 관리 (기존)

### 1.1 토픽 생성
```http
POST /api/kafka/topics
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "test-topic",
  "partitions": 3,
  "replicationFactor": 1
}
```

### 1.2 토픽 목록 조회
```http
GET /api/kafka/topics
Authorization: Bearer {token}
```

### 1.3 토픽 삭제
```http
DELETE /api/kafka/topics/{topicName}
Authorization: Bearer {token}
```

### 1.4 토픽 상세 정보 조회
```http
POST /api/kafka/topics/describe
Authorization: Bearer {token}
Content-Type: application/json

["topic1", "topic2"]
```

---

## 2. Consumer Group 관리

### 2.1 Consumer Group 목록 조회
```http
GET /api/kafka/consumer-groups
Authorization: Bearer {token}
```

**응답 예시:**
```json
["group1", "group2", "group3"]
```

### 2.2 Consumer Group 상세 정보 조회
```http
GET /api/kafka/consumer-groups/{groupId}
Authorization: Bearer {token}
```

**응답 예시:**
```json
{
  "groupId": "my-consumer-group",
  "state": "Stable",
  "coordinator": "broker1:9092",
  "members": [
    {
      "memberId": "consumer-1",
      "clientId": "client-1",
      "host": "192.168.1.100",
      "assignedPartitions": ["topic1-0", "topic1-1"]
    }
  ],
  "assignments": {
    "topic1-0": "consumer-1",
    "topic1-1": "consumer-1"
  },
  "lag": 0
}
```

### 2.3 Consumer Group 삭제
```http
DELETE /api/kafka/consumer-groups/{groupId}
Authorization: Bearer {token}
```

### 2.4 Consumer Group 오프셋 리셋
```http
POST /api/kafka/consumer-groups/{groupId}/reset-offset?topicName={topicName}&partition={partition}&offset={offset}
Authorization: Bearer {token}
```

**파라미터:**
- `groupId`: Consumer Group ID
- `topicName`: 토픽 이름
- `partition`: 파티션 번호
- `offset`: 새로운 오프셋 값

---

## 3. ACL (Access Control List) 관리

### 3.1 ACL 목록 조회
```http
GET /api/kafka/acls
Authorization: Bearer {token}
```

**응답 예시:**
```json
[
  {
    "principal": "User:alice",
    "resourceType": "TOPIC",
    "resourceName": "test-topic",
    "operation": "READ",
    "permissionType": "ALLOW",
    "host": "*",
    "patternType": "LITERAL"
  }
]
```

### 3.2 ACL 생성
```http
POST /api/kafka/acls
Authorization: Bearer {token}
Content-Type: application/json

{
  "principal": "User:alice",
  "resourceType": "TOPIC",
  "resourceName": "test-topic",
  "operation": "READ",
  "permissionType": "ALLOW",
  "host": "*",
  "patternType": "LITERAL"
}
```

**필드 설명:**
- `principal`: 사용자 또는 그룹 (예: "User:alice", "Group:developers")
- `resourceType`: 리소스 타입 (TOPIC, GROUP, CLUSTER, TRANSACTIONAL_ID, DELEGATION_TOKEN)
- `resourceName`: 리소스 이름
- `operation`: 작업 (READ, WRITE, CREATE, DELETE, ALTER, DESCRIBE, CLUSTER_ACTION, DESCRIBE_CONFIGS, ALTER_CONFIGS, IDEMPOTENT_WRITE, ALL)
- `permissionType`: 권한 타입 (ALLOW, DENY)
- `host`: 호스트 (일반적으로 "*")
- `patternType`: 패턴 타입 (LITERAL, PREFIXED)

### 3.3 ACL 삭제
```http
DELETE /api/kafka/acls
Authorization: Bearer {token}
Content-Type: application/json

{
  "principal": "User:alice",
  "resourceType": "TOPIC",
  "resourceName": "test-topic",
  "operation": "READ",
  "permissionType": "ALLOW",
  "host": "*",
  "patternType": "LITERAL"
}
```

---

## 4. Config 관리

### 4.1 클러스터 설정 조회
```http
GET /api/kafka/configs/cluster
Authorization: Bearer {token}
```

**응답 예시:**
```json
{
  "resourceType": "CLUSTER",
  "resourceName": "",
  "configs": {
    "log.retention.hours": "168",
    "log.segment.bytes": "1073741824",
    "num.partitions": "1"
  }
}
```

### 4.2 토픽 설정 조회
```http
GET /api/kafka/configs/topics/{topicName}
Authorization: Bearer {token}
```

**응답 예시:**
```json
{
  "resourceType": "TOPIC",
  "resourceName": "test-topic",
  "configs": {
    "cleanup.policy": "delete",
    "compression.type": "producer",
    "retention.ms": "604800000"
  }
}
```

### 4.3 설정 업데이트
```http
PUT /api/kafka/configs
Authorization: Bearer {token}
Content-Type: application/json

{
  "resourceType": "TOPIC",
  "resourceName": "test-topic",
  "configs": {
    "retention.ms": "86400000",
    "cleanup.policy": "compact"
  }
}
```

---

## 5. 클러스터 정보

### 5.1 클러스터 정보 조회
```http
GET /api/kafka/cluster/info
Authorization: Bearer {token}
```

**응답 예시:**
```json
{
  "clusterId": "cluster-12345",
  "brokers": [
    {
      "id": 1,
      "host": "broker1",
      "port": 9092,
      "rack": "rack1",
      "isController": true
    },
    {
      "id": 2,
      "host": "broker2",
      "port": 9092,
      "rack": "rack2",
      "isController": false
    }
  ],
  "clusterConfigs": {},
  "totalPartitions": 10,
  "totalTopics": 5
}
```

---

## 6. 파티션 관리

### 6.1 파티션 정보 조회
```http
GET /api/kafka/partitions/{topicName}
Authorization: Bearer {token}
```

**응답 예시:**
```json
[
  {
    "topicName": "test-topic",
    "partition": 0,
    "leader": 1,
    "replicas": [1, 2],
    "isr": [1, 2],
    "offline": false,
    "size": 1024000,
    "offset": 1000
  },
  {
    "topicName": "test-topic",
    "partition": 1,
    "leader": 2,
    "replicas": [2, 1],
    "isr": [2, 1],
    "offline": false,
    "size": 2048000,
    "offset": 2000
  }
]
```

### 6.2 파티션 재할당
```http
POST /api/kafka/partitions/reassign
Authorization: Bearer {token}
Content-Type: application/json

[
  {
    "topicName": "test-topic",
    "partition": 0,
    "replicas": [2, 3]
  },
  {
    "topicName": "test-topic",
    "partition": 1,
    "replicas": [3, 1]
  }
]
```

---

## 에러 응답

모든 API는 실패 시 다음과 같은 형태의 에러 응답을 반환합니다:

```json
{
  "error": "❌ Failed to {operation}: {error message}"
}
```

**HTTP 상태 코드:**
- `200`: 성공
- `400`: 잘못된 요청
- `401`: 인증 실패
- `403`: 권한 없음
- `404`: 리소스 없음
- `500`: 서버 내부 오류

---

## 사용 예시

### 1. Consumer Group 모니터링
```bash
# Consumer Group 목록 조회
curl -H "Authorization: Bearer {token}" \
     http://localhost:8080/api/kafka/consumer-groups

# 특정 Consumer Group 상세 정보
curl -H "Authorization: Bearer {token}" \
     http://localhost:8080/api/kafka/consumer-groups/my-group
```

### 2. ACL 관리
```bash
# ACL 목록 조회
curl -H "Authorization: Bearer {token}" \
     http://localhost:8080/api/kafka/acls

# 새로운 ACL 생성
curl -X POST \
     -H "Authorization: Bearer {token}" \
     -H "Content-Type: application/json" \
     -d '{
       "principal": "User:alice",
       "resourceType": "TOPIC",
       "resourceName": "sensitive-topic",
       "operation": "READ",
       "permissionType": "ALLOW",
       "host": "*",
       "patternType": "LITERAL"
     }' \
     http://localhost:8080/api/kafka/acls
```

### 3. 클러스터 모니터링
```bash
# 클러스터 정보 조회
curl -H "Authorization: Bearer {token}" \
     http://localhost:8080/api/kafka/cluster/info

# 특정 토픽의 파티션 정보
curl -H "Authorization: Bearer {token}" \
     http://localhost:8080/api/kafka/partitions/my-topic
```

---

## 주의사항

1. **권한**: 모든 API는 MANAGER 권한이 필요합니다.
2. **인증**: JWT 토큰이 유효해야 합니다.
3. **리소스 제한**: Kafka 클러스터의 리소스 제한을 고려하여 사용하세요.
4. **성능**: 대용량 클러스터에서는 응답 시간이 길어질 수 있습니다.
5. **안전성**: 프로덕션 환경에서는 신중하게 사용하세요.

---

## 프론트엔드 연동 가이드

### React/JavaScript 예시
```javascript
// Consumer Group 목록 조회
const fetchConsumerGroups = async (token) => {
  const response = await fetch('/api/kafka/consumer-groups', {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  });
  return await response.json();
};

// ACL 생성
const createAcl = async (token, aclData) => {
  const response = await fetch('/api/kafka/acls', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(aclData)
  });
  return await response.json();
};
```

이 API를 통해 Kafka Control Center의 주요 기능들을 웹 애플리케이션에서 완전히 관리할 수 있습니다.
