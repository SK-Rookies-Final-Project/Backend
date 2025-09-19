package com.finalproject.springbackend.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "\"resource-level-false\"")
@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceLevelFalse {

    @Id @Column(columnDefinition = "text")
    private String id;      // 기본키

    //지금은 해당 컬럼의 자료형이 text라 나중에 주석 해제하고 사용하기
    @Column(name = "eventTimeKST", columnDefinition = "timestamptz", nullable = false)
    private OffsetDateTime eventTimeKST;    // 특정 이벤트가 일어 난 한국 시간(UTC +9)

    //지금은 해당 컬럼의 자료형이 text라 나중에 주석 해제하고 사용하기
    @Column(name = "processingTimeKST", columnDefinition = "timestamptz", nullable = false)
    private OffsetDateTime processTimeKST;  // Flink로 특정 이벤트가 변경 된 한국 시간 (UTC +9)

    @Column(name = "principal", columnDefinition = "text")
    private String principal;   // 유저 이름

    @Column(name = "clientIp", columnDefinition="text")
    private String clientIp;        //어떤 IP에서 비인가 접근을 시도했는지

    @Column(name = "methodName", columnDefinition="text")
    private String methodName;  // kafka.Produce, MDS.Authorize, kafka.Metadata 등등 개많음

    @Column(name="granted", columnDefinition="boolean")
    private boolean granted;

    @Column(name = "resourceType", columnDefinition="text")
    private String resourceType;

    @Column(name = "resourceName", columnDefinition="text")
    private String resourceName;    // 어디 리소스에서 권한이 없는 행동을 했는지 (ex: audit-topic)

    @Column(name = "operation", columnDefinition = "text")
    private String operation;   // 특정 유저가 해당 리소스에 권한이 없는 어떤 행동을 했는지


}
