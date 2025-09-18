package com.finalproject.springbackend.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name="`certified-2time`")
@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class Certified2Time {

    /**
     * certified-2time:
     * 일정 시간동안 동일한 client_ip에서 반복된 인증 실패가 2회이상 발생할 경우 저장되는 엔티티 테이블
     */
    @Id @Column(columnDefinition = "text")
    private String id;                      //기본키

    @Column(name="client_ip", columnDefinition="text")
    private String clientIp;                //비인가 접근을 시도한 클라이언트 ip

    @Column(name="alert_time_kst", columnDefinition="timestamptz", nullable = false)
    private OffsetDateTime alertTimeKST;    //이벤트 감지 시간

    @Column(name="alert_type", columnDefinition = "text")
    private String alertType;               //어떤 유형의 비인가 접근인지

    @Column(name="description", columnDefinition = "text")
    private String description;             //해당 비인가 접근에 대한 사람이 이해하기 쉬운 설명

    @Column(name="failure_count", columnDefinition = "BIGINT")
    private Long failureCount;              //각 유형 별 비인가 접근 횟수


}
//{
//  "clientIp": "15.164.187.115",
//  "alertTime": "2025-09-11 18:04:58.891 KST",
//  "failureCount": 5,
//  "message": "Login failures detected from IP 15.164.187.115, 5 times within 5 minutes."
//}