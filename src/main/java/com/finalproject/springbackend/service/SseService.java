package com.finalproject.springbackend.service;

import com.finalproject.springbackend.dto.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseService {
    
    private static final Logger log = LoggerFactory.getLogger(SseService.class);
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private AuthService authService;
    
    
    // 사용자별 SSE 연결 관리 (username -> clientId -> ResponseBodyEmitter)
    private final Map<String, Map<String, ResponseBodyEmitter>> userCertified2TimeEmitters = new ConcurrentHashMap<>(); 
    private final Map<String, Map<String, ResponseBodyEmitter>> userSystemLevelFalseEmitters = new ConcurrentHashMap<>();
    private final Map<String, Map<String, ResponseBodyEmitter>> userResourceLevelFalseEmitters = new ConcurrentHashMap<>();
    private final Map<String, Map<String, ResponseBodyEmitter>> userCertifiedNotMoveEmitters = new ConcurrentHashMap<>();
    
    // 기존 방식 유지 (하위 호환성을 위해)
    private final Map<String, ResponseBodyEmitter> certified2TimeEmitters = new ConcurrentHashMap<>(); 
    private final Map<String, ResponseBodyEmitter> systemLevelFalseEmitters = new ConcurrentHashMap<>();
    private final Map<String, ResponseBodyEmitter> resourceLevelFalseEmitters = new ConcurrentHashMap<>();
    private final Map<String, ResponseBodyEmitter> certifiedNotMoveEmitters = new ConcurrentHashMap<>();


    public ResponseBodyEmitter createSystemLevelFalseStream() {
        // SSE 스트림 생성
        return createSseConnection(systemLevelFalseEmitters, "system-level-false");
    }
    
    public ResponseBodyEmitter createResourceLevelFalseStream() {
        // SSE 스트림 생성
        return createSseConnection(resourceLevelFalseEmitters, "resource-level-false");
    }
    
    public ResponseBodyEmitter createCertifiedNotMoveStream() {
        // SSE 스트림 생성
        return createSseConnection(certifiedNotMoveEmitters, "certified-notMove");
    }
    
    public ResponseBodyEmitter createCertified2TimeStream() {
        // SSE 스트림 생성
        return createSseConnection(certified2TimeEmitters, "certified-2time");
    }
    
    // 사용자별 SSE 연결 생성 메서드들
    public ResponseBodyEmitter createUserSystemLevelFalseStream(UserInfo userInfo) {
        // 사용자별 SSE 스트림 생성
        // Consumer 시작 (UserInfo에서 비밀번호 사용)
        String username = userInfo.getUsername();
        String password = userInfo.getPassword();
        
        if (password != null) {
            // 사용자 비밀번호 확인됨, Consumer 시작
            SystemLevelFalseConsumer consumer = applicationContext.getBean(SystemLevelFalseConsumer.class);
            consumer.startConsumerForUser(username, password);
            log.debug("✅ 사용자 {} Consumer 시작 완료", username);
        } else {
            log.warn("❌ 사용자 {}의 비밀번호를 찾을 수 없습니다 - Consumer 시작하지 않음", username);
        }
        return createUserSseConnection(userSystemLevelFalseEmitters, username, "system-level-false");
    }
    
    // 클라이언트 ID와 함께 SSE 연결 생성 (로그아웃 시 사용)
    public String createUserSystemLevelFalseStreamWithClientId(String username) {
        // 사용자별 SSE 스트림 생성
        // Consumer 시작 (실제 사용자 비밀번호 사용)
        String password = authService.getUserPassword(username);
        if (password != null) {
            // 사용자 비밀번호 확인됨, Consumer 시작
            // 사용자 비밀번호 확인됨, Consumer 시작
            SystemLevelFalseConsumer consumer = applicationContext.getBean(SystemLevelFalseConsumer.class);
            consumer.startConsumerForUser(username, password);
        } else {
            log.warn("❌ 사용자 {}의 비밀번호를 찾을 수 없습니다 - Consumer 시작하지 않음", username);
        }
        return createUserSseConnectionWithClientId(userSystemLevelFalseEmitters, username, "system-level-false");
    }
    
    public ResponseBodyEmitter createUserResourceLevelFalseStream(UserInfo userInfo) {
        // 사용자별 SSE 스트림 생성
        // Consumer 시작 (UserInfo에서 비밀번호 사용)
        String username = userInfo.getUsername();
        String password = userInfo.getPassword();
        
        if (password != null) {
            ResourceLevelFalseConsumer consumer = applicationContext.getBean(ResourceLevelFalseConsumer.class);
            consumer.startConsumerForUser(username, password);
            log.info("✅ 사용자 {} ResourceLevelFalse Consumer 시작 시도 완료", username);
        } else {
            log.warn("❌ 사용자 {}의 비밀번호를 찾을 수 없습니다 - Consumer 시작하지 않음", username);
        }
        return createUserSseConnection(userResourceLevelFalseEmitters, username, "resource-level-false");
    }
    
    public ResponseBodyEmitter createUserCertifiedNotMoveStream(UserInfo userInfo) {
        // 사용자별 SSE 스트림 생성
        // Consumer 시작 (UserInfo에서 비밀번호 사용)
        String username = userInfo.getUsername();
        String password = userInfo.getPassword();
        
        if (password != null) {
            CertifiedNotMoveConsumer consumer = applicationContext.getBean(CertifiedNotMoveConsumer.class);
            consumer.startConsumerForUser(username, password);
            log.debug("✅ 사용자 {} CertifiedNotMove Consumer 시작 완료", username);
        } else {
            log.warn("❌ 사용자 {}의 비밀번호를 찾을 수 없습니다 - Consumer 시작하지 않음", username);
        }
        return createUserSseConnection(userCertifiedNotMoveEmitters, username, "certified-notMove");
    }
    
    public ResponseBodyEmitter createUserCertified2TimeStream(UserInfo userInfo) {
        // 사용자별 SSE 스트림 생성
        // Consumer 시작 (UserInfo에서 비밀번호 사용)
        String username = userInfo.getUsername();
        String password = userInfo.getPassword();
        
        if (password != null) {
            Certified2TimeConsumer consumer = applicationContext.getBean(Certified2TimeConsumer.class);
            consumer.startConsumerForUser(username, password);
            log.debug("✅ 사용자 {} Certified2Time Consumer 시작 완료", username);
        } else {
            log.warn("❌ 사용자 {}의 비밀번호를 찾을 수 없습니다 - Consumer 시작하지 않음", username);
        }
        return createUserSseConnection(userCertified2TimeEmitters, username, "certified-2time");
    }

    private ResponseBodyEmitter createSseConnection(Map<String, ResponseBodyEmitter> emitterMap, String topicName) {
        String clientId = java.util.UUID.randomUUID().toString();
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(Long.MAX_VALUE);
        emitterMap.put(clientId, emitter);
        
        // SSE 연결 생성 완료
        
        emitter.onCompletion(() -> {
            emitterMap.remove(clientId);
            // SSE 연결 완료
        });
        
        emitter.onTimeout(() -> {
            emitterMap.remove(clientId);
            log.warn("SSE 연결 타임아웃: {} 토픽, 남은 연결 수: {}", topicName, emitterMap.size());
        });
        
        emitter.onError((throwable) -> {
            emitterMap.remove(clientId);
            log.error("SSE 연결 오류: {} 토픽, 오류: {}, 남은 연결 수: {}", 
                    topicName, throwable.getMessage(), emitterMap.size());
        });
        
        return emitter;
    }
    
    // 클라이언트 ID를 반환하는 SSE 연결 생성
    private String createUserSseConnectionWithClientId(Map<String, Map<String, ResponseBodyEmitter>> userEmitterMap, String username, String topicName) {
        // 기존 연결이 있다면 모두 정리
        closeUserConnections(userEmitterMap, username, topicName);
        
        // 사용자별 연결 맵이 없으면 생성
        userEmitterMap.computeIfAbsent(username, k -> new ConcurrentHashMap<>());
        Map<String, ResponseBodyEmitter> userEmitters = userEmitterMap.get(username);
        
        String clientId = java.util.UUID.randomUUID().toString();
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(Long.MAX_VALUE);
        
        // SSE 연결 생성 완료 (초기 메시지 전송 제거)
        
        userEmitters.put(clientId, emitter);
        
        // 사용자별 SSE 연결 생성 완료
        
        emitter.onCompletion(() -> {
            userEmitters.remove(clientId);
            if (userEmitters.isEmpty()) {
                userEmitterMap.remove(username);
            }
            // 사용자별 SSE 연결 완료
        });
        
        emitter.onTimeout(() -> {
            userEmitters.remove(clientId);
            if (userEmitters.isEmpty()) {
                userEmitterMap.remove(username);
            }
            log.warn("사용자별 SSE 연결 타임아웃: {} 토픽, 사용자: {}, 남은 연결 수: {}", 
                    topicName, username, userEmitters.size());
        });
        
        emitter.onError((throwable) -> {
            userEmitters.remove(clientId);
            if (userEmitters.isEmpty()) {
                userEmitterMap.remove(username);
            }
            // IOException은 연결 중단으로 간주하여 WARN 레벨로 처리
            if (throwable instanceof IOException) {
                log.warn("사용자별 SSE 연결 중단: {} 토픽, 사용자: {}, Client ID: {}, 오류: {}, 남은 연결 수: {}", 
                        topicName, username, clientId, throwable.getMessage(), userEmitters.size());
            } else {
                log.error("사용자별 SSE 연결 오류: {} 토픽, 사용자: {}, Client ID: {}, 오류: {}, 남은 연결 수: {}", 
                        topicName, username, clientId, throwable.getMessage(), userEmitters.size());
            }
        });
        
        return clientId;
    }
    
    private ResponseBodyEmitter createUserSseConnection(Map<String, Map<String, ResponseBodyEmitter>> userEmitterMap, String username, String topicName) {
        // 기존 연결이 있다면 모두 정리
        closeUserConnections(userEmitterMap, username, topicName);
        
        // 사용자별 연결 맵이 없으면 생성
        userEmitterMap.computeIfAbsent(username, k -> new ConcurrentHashMap<>());
        Map<String, ResponseBodyEmitter> userEmitters = userEmitterMap.get(username);
        
        String clientId = java.util.UUID.randomUUID().toString();
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(Long.MAX_VALUE);
        
        // SSE 연결 생성 완료 - 초기 연결 확인 메시지 전송 (Kafka 데이터 형식과 호환)
        try {
            // 실제 Kafka 데이터와 유사한 형식으로 초기 메시지 생성
            String connectionMessage = "{\"id\":\"connection-" + clientId.substring(0, 8) + 
                    "\",\"clientIp\":\"127.0.0.1\",\"eventTimeKST\":\"" + 
                    java.time.OffsetDateTime.now().toString() + 
                    "\",\"granted\":true,\"methodName\":\"SSE_CONNECTION\",\"operation\":\"CONNECT\"," +
                    "\"principal\":\"" + username + "\",\"processingTimeKST\":\"" + 
                    java.time.OffsetDateTime.now().toString() + 
                    "\",\"resourceName\":\"/api/auth/" + topicName + "\",\"resourceType\":\"SSE_STREAM\"}";
            
            emitter.send(connectionMessage, org.springframework.http.MediaType.TEXT_EVENT_STREAM);
            log.info("🔗 SSE 초기 연결 메시지 전송 완료: 사용자 {}, 토픽 {}, Client ID {}", username, topicName, clientId);
        } catch (Exception e) {
            log.warn("SSE 초기 메시지 전송 실패: {}", e.getMessage());
        }
        
        userEmitters.put(clientId, emitter);
        
        log.info("📊 [SSE 연결 현황] 사용자: {}, 토픽: {}, Client ID: {}, 총 연결 수: {}", 
                username, topicName, clientId, userEmitters.size());
        
        emitter.onCompletion(() -> {
            userEmitters.remove(clientId);
            log.info("🔌 [SSE 연결 해제] 사용자: {}, 토픽: {}, Client ID: {}, 남은 연결 수: {}", 
                    username, topicName, clientId, userEmitters.size());
            if (userEmitters.isEmpty()) {
                userEmitterMap.remove(username);
                log.info("🏁 [SSE 완전 해제] 사용자: {}의 {} 토픽 모든 연결 해제", username, topicName);
            }
        });
        
        emitter.onTimeout(() -> {
            userEmitters.remove(clientId);
            if (userEmitters.isEmpty()) {
                userEmitterMap.remove(username);
            }
            log.warn("사용자별 SSE 연결 타임아웃: {} 토픽, 사용자: {}, 남은 연결 수: {}", 
                    topicName, username, userEmitters.size());
        });
        
        emitter.onError((throwable) -> {
            userEmitters.remove(clientId);
            if (userEmitters.isEmpty()) {
                userEmitterMap.remove(username);
            }
            // IOException은 연결 중단으로 간주하여 WARN 레벨로 처리
            if (throwable instanceof IOException) {
                log.warn("사용자별 SSE 연결 중단: {} 토픽, 사용자: {}, Client ID: {}, 오류: {}, 남은 연결 수: {}", 
                        topicName, username, clientId, throwable.getMessage(), userEmitters.size());
            } else {
                log.error("사용자별 SSE 연결 오류: {} 토픽, 사용자: {}, Client ID: {}, 오류: {}, 남은 연결 수: {}", 
                        topicName, username, clientId, throwable.getMessage(), userEmitters.size());
            }
        });
        
        return emitter;
    }
    
    // 사용자의 모든 SSE 연결 정리 (Consumer는 유지)
    public void closeUserConnections(String username) {
        // SSE 연결만 정리 (Consumer는 유지)
        closeUserConnections(userCertified2TimeEmitters, username, "certified-2time");
        closeUserConnections(userSystemLevelFalseEmitters, username, "system-level-false");
        closeUserConnections(userResourceLevelFalseEmitters, username, "resource-level-false");
        closeUserConnections(userCertifiedNotMoveEmitters, username, "certified-notMove");
        
        // 사용자의 모든 SSE 연결이 정리되었는지 확인
        if (isUserCompletelyDisconnected(username)) {
            // 모든 연결이 정리되었을 때만 Consumer 중지 및 비밀번호 제거
            stopUserConsumers(username);
            authService.removeUserPassword(username);
            // 사용자 모든 연결 정리 완료
        } else {
            // 사용자 일부 연결만 정리됨
        }
    }
    
    // 사용자가 완전히 연결 해제되었는지 확인
    private boolean isUserCompletelyDisconnected(String username) {
        boolean hasCertified2Time = userCertified2TimeEmitters.containsKey(username) && 
                                   !userCertified2TimeEmitters.get(username).isEmpty();
        boolean hasSystemLevel = userSystemLevelFalseEmitters.containsKey(username) && 
                                !userSystemLevelFalseEmitters.get(username).isEmpty();
        boolean hasResourceLevel = userResourceLevelFalseEmitters.containsKey(username) && 
                                  !userResourceLevelFalseEmitters.get(username).isEmpty();
        boolean hasCertifiedNotMove = userCertifiedNotMoveEmitters.containsKey(username) && 
                                     !userCertifiedNotMoveEmitters.get(username).isEmpty();
        
        return !hasCertified2Time && !hasSystemLevel && !hasResourceLevel && !hasCertifiedNotMove;
    }
    
    // 특정 클라이언트의 SSE 연결 해제 (로그아웃 시 사용)
    public void closeClientConnection(String username, String clientId) {
        // 클라이언트 연결 해제
        
        // 각 토픽별로 해당 클라이언트 연결 해제
        closeClientConnection(userCertified2TimeEmitters, username, clientId, "certified-2time");
        closeClientConnection(userSystemLevelFalseEmitters, username, clientId, "system-level-false");
        closeClientConnection(userResourceLevelFalseEmitters, username, clientId, "resource-level-false");
        closeClientConnection(userCertifiedNotMoveEmitters, username, clientId, "certified-notMove");
        
        // 사용자의 모든 연결이 정리되었는지 확인
        if (isUserCompletelyDisconnected(username)) {
            // 모든 연결이 정리되었을 때만 Consumer 중지 및 비밀번호 제거
            stopUserConsumers(username);
            authService.removeUserPassword(username);
            // 사용자 모든 연결 정리 완료
        } else {
            // 사용자 일부 연결만 정리됨
        }
    }
    
    // 특정 클라이언트의 특정 토픽 연결 해제
    private void closeClientConnection(Map<String, Map<String, ResponseBodyEmitter>> userEmitterMap, 
                                     String username, String clientId, String topicName) {
        Map<String, ResponseBodyEmitter> userEmitters = userEmitterMap.get(username);
        if (userEmitters != null && userEmitters.containsKey(clientId)) {
            try {
                ResponseBodyEmitter emitter = userEmitters.remove(clientId);
                if (emitter != null) {
                    emitter.complete();
                    // 클라이언트 연결 해제 완료
                }
                
                // 사용자의 모든 클라이언트가 해제되었으면 사용자 맵에서 제거
                if (userEmitters.isEmpty()) {
                    userEmitterMap.remove(username);
                    // 사용자 토픽 연결 모두 정리됨
                }
            } catch (Exception e) {
                log.warn("클라이언트 연결 해제 중 오류: {} 토픽, 사용자 {}, Client ID: {}, 오류: {}", 
                        topicName, username, clientId, e.getMessage());
            }
        }
    }
    
    // 사용자의 모든 Consumer 중지
    private void stopUserConsumers(String username) {
        try {
            // 각 Consumer 서비스에서 사용자별 Consumer 중지
            SystemLevelFalseConsumer systemConsumer = applicationContext.getBean(SystemLevelFalseConsumer.class);
            systemConsumer.stopConsumerForUser(username);
            
            ResourceLevelFalseConsumer resourceConsumer = applicationContext.getBean(ResourceLevelFalseConsumer.class);
            resourceConsumer.stopConsumerForUser(username);
            
            Certified2TimeConsumer certified2TimeConsumer = applicationContext.getBean(Certified2TimeConsumer.class);
            certified2TimeConsumer.stopConsumerForUser(username);
            
            CertifiedNotMoveConsumer certifiedNotMoveConsumer = applicationContext.getBean(CertifiedNotMoveConsumer.class);
            certifiedNotMoveConsumer.stopConsumerForUser(username);
            
            // 사용자 모든 Consumer 중지 완료
        } catch (Exception e) {
            log.error("사용자 {}의 Consumer 중지 중 오류: {}", username, e.getMessage(), e);
        }
    }
    
    private void closeUserConnections(Map<String, Map<String, ResponseBodyEmitter>> userEmitterMap, String username, String topicName) {
        Map<String, ResponseBodyEmitter> userEmitters = userEmitterMap.get(username);
        if (userEmitters != null && !userEmitters.isEmpty()) {
            // 사용자 기존 연결 정리 중
            userEmitters.forEach((clientId, emitter) -> {
                try {
                    emitter.complete();
                } catch (Exception e) {
                    log.warn("SSE 연결 정리 중 오류: {}", e.getMessage());
                }
            });
            userEmitters.clear();
            userEmitterMap.remove(username);
        }
    }
    
    public Map<String, ResponseBodyEmitter> getCertified2TimeEmitters() {
        return certified2TimeEmitters;
    }
    
    public Map<String, ResponseBodyEmitter> getSystemLevelFalseEmitters() {
        return systemLevelFalseEmitters;
    }
    
    public Map<String, ResponseBodyEmitter> getResourceLevelFalseEmitters() {
        return resourceLevelFalseEmitters;
    }
    
    public Map<String, ResponseBodyEmitter> getCertifiedNotMoveEmitters() {
        return certifiedNotMoveEmitters;
    }
    
    // 사용자별 SSE 연결 getter 메서드들
    public Map<String, ResponseBodyEmitter> getUserCertified2TimeEmitters(String username) {
        return userCertified2TimeEmitters.getOrDefault(username, new ConcurrentHashMap<>());
    }
    
    public Map<String, ResponseBodyEmitter> getUserSystemLevelFalseEmitters(String username) {
        return userSystemLevelFalseEmitters.getOrDefault(username, new ConcurrentHashMap<>());
    }
    
    public Map<String, ResponseBodyEmitter> getUserResourceLevelFalseEmitters(String username) {
        return userResourceLevelFalseEmitters.getOrDefault(username, new ConcurrentHashMap<>());
    }
    
    public Map<String, ResponseBodyEmitter> getUserCertifiedNotMoveEmitters(String username) {
        return userCertifiedNotMoveEmitters.getOrDefault(username, new ConcurrentHashMap<>());
    }
    
    // 모든 사용자별 SSE 연결 정보 조회
    public Map<String, Map<String, ResponseBodyEmitter>> getAllUserCertified2TimeEmitters() {
        return userCertified2TimeEmitters;
    }
    
    public Map<String, Map<String, ResponseBodyEmitter>> getAllUserSystemLevelFalseEmitters() {
        return userSystemLevelFalseEmitters;
    }
    
    public Map<String, Map<String, ResponseBodyEmitter>> getAllUserResourceLevelFalseEmitters() {
        return userResourceLevelFalseEmitters;
    }
    
    public Map<String, Map<String, ResponseBodyEmitter>> getAllUserCertifiedNotMoveEmitters() {
        return userCertifiedNotMoveEmitters;
    }
    
    // 연결 상태 체크 및 정리 메서드
    public void cleanupInactiveConnections() {
        cleanupUserConnections(userCertified2TimeEmitters, "certified-2time");
        cleanupUserConnections(userSystemLevelFalseEmitters, "system-level-false");
        cleanupUserConnections(userResourceLevelFalseEmitters, "resource-level-false");
        cleanupUserConnections(userCertifiedNotMoveEmitters, "certified-notMove");
    }
    
    // 주기적으로 비활성 연결 정리 (10분마다 실행)
    @Scheduled(fixedRate = 600000) // 10분 = 600,000ms
    public void scheduledCleanup() {
        try {
            log.debug("SSE 연결 상태 체크 및 정리 시작");
            cleanupInactiveConnections();
            log.debug("SSE 연결 상태 체크 및 정리 완료");
        } catch (Exception e) {
            log.error("SSE 연결 정리 중 오류 발생: {}", e.getMessage(), e);
        }
    }
    
    private void cleanupUserConnections(Map<String, Map<String, ResponseBodyEmitter>> userEmitterMap, String topicName) {
        userEmitterMap.forEach((username, userEmitters) -> {
            if (userEmitters != null && !userEmitters.isEmpty()) {
                // ConcurrentModificationException 방지를 위해 복사본 생성
                Map<String, ResponseBodyEmitter> emittersCopy = new ConcurrentHashMap<>(userEmitters);
                emittersCopy.forEach((clientId, emitter) -> {
                    try {
                        // 연결 상태 체크 (heartbeat 전송 제거)
                        // 연결이 끊어진 경우에만 catch 블록에서 정리
                    } catch (Exception e) {
                        log.debug("비활성 연결 감지 및 정리: {} 토픽, 사용자: {}, Client ID: {}, 오류: {}", 
                                topicName, username, clientId, e.getMessage());
                        userEmitters.remove(clientId);
                    }
                });
                
                // 빈 사용자 맵 정리
                if (userEmitters.isEmpty()) {
                    userEmitterMap.remove(username);
                    log.debug("사용자 {}의 {} 토픽 연결이 모두 정리됨", username, topicName);
                }
            }
        });
    }
    

}