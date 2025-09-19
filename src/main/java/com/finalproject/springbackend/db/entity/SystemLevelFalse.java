package com.finalproject.springbackend.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name="\"system-level-false\"")
@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemLevelFalse {

    @Id @Column(columnDefinition="text")
    private String id;

    @Column(name="eventTimeKST", columnDefinition = "timestamptz", nullable = false)
    private OffsetDateTime eventTimeKST;

    @Column(name="processingTimeKST",columnDefinition = "timestamptz", nullable = false)
    private OffsetDateTime processTimeKST;

    @Column(name="principal", columnDefinition="text")
    private String principal;

    @Column(name="clientIp", columnDefinition="text")
    private String clientIp;

    @Column(name="methodName", columnDefinition="text")
    private String methodName;

    @Column(name="granted", columnDefinition="boolean")
    private boolean granted;

    @Column(name="resourceType", columnDefinition="text")
    private String resourceType;

    @Column(name="resourceName", columnDefinition="text")
    private String resourceName;

    @Column(name="operation", columnDefinition="text")
    private String operation;
}
