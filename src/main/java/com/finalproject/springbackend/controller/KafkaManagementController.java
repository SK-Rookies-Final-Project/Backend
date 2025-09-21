package com.finalproject.springbackend.controller;

import com.finalproject.springbackend.annotation.RequirePermission;
import com.finalproject.springbackend.dto.*;
import com.finalproject.springbackend.service.ConsumerGroupService;
import com.finalproject.springbackend.service.SchemaService;
import com.finalproject.springbackend.service.TopicService;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.TopicDescription;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kafka")
public class KafkaManagementController {

    private final TopicService topicService;
    private final SchemaService schemaService;
    private final ConsumerGroupService consumerGroupService;

    // === 토픽 관리 ===

    /**
     * 토픽 생성 (ADMIN 또는 MANAGER 권한 필요)
     */
    @PostMapping("/topics")
    @RequirePermission({Permission.ADMIN, Permission.MANAGER})
    public ResponseEntity<?> createTopic(@RequestBody CreateTopicRequestDTO request, Authentication authentication) {
        try {
            UserInfo userInfo = (UserInfo) authentication.getPrincipal();
            topicService.createTopic(request.name, request.partitions, request.replicationFactor, 
                                   userInfo.getUsername(), userInfo.getPassword());
            return ResponseEntity.ok("✅ Created topic: " + request.name);
        } catch (Exception e) {
            log.error("❌ Failed to create topic '{}': {}", request.name, e.getMessage());
            return ResponseEntity.internalServerError().body("❌ Failed to create topic: " + e.getMessage());
        }
    }

    /**
     * 토픽 목록 조회 (ADMIN 또는 MANAGER 권한 필요)
     */
    @GetMapping("/topics")
    @RequirePermission({Permission.ADMIN, Permission.MANAGER})
    public ResponseEntity<?> listTopics(Authentication authentication) {
        try {
            UserInfo userInfo = (UserInfo) authentication.getPrincipal();
            List<String> topics = topicService.listTopicsForUser(userInfo.getUsername(), userInfo.getPassword());
            return ResponseEntity.ok(topics);
        } catch (Exception e) {
            log.error("❌ Failed to list topics: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("❌ Failed to list topics: " + e.getMessage());
        }
    }

    /**
     * 토픽 상세 정보 조회 (ADMIN 또는 MANAGER 권한 필요)
     */
    @PostMapping("/topics/describe")
    @RequirePermission({Permission.ADMIN, Permission.MANAGER})
    public ResponseEntity<?> describeTopics(@RequestBody List<String> topicNames, Authentication authentication) {
        try {
            UserInfo userInfo = (UserInfo) authentication.getPrincipal();
            Map<String, TopicDescription> descriptions = topicService.describeTopics(topicNames, userInfo.getUsername(), userInfo.getPassword());
            return ResponseEntity.ok(descriptions);
        } catch (Exception e) {
            log.error("❌ Failed to describe topics: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("❌ Failed to describe topics: " + e.getMessage());
        }
    }

    // === 스키마 관리 ===

    /**
     * 새로운 Avro 스키마 등록 (ADMIN 또는 MANAGER 권한 필요)
     */
    @PostMapping("/schemas/avro")
    @RequirePermission({Permission.ADMIN, Permission.MANAGER})
    public ResponseEntity<?> registerAvroSchema(@RequestBody AvroSchemaRequest request) {
        try {
            int schemaId = schemaService.registerAvro(request.getSubject(), request.getSchema());

            return ResponseEntity.ok(Map.of(
                    "subject", request.getSubject(),
                    "id", schemaId,
                    "schema", request.getSchema()
            ));
        } catch (Exception e) {
            log.error("❌ Failed to register Avro schema for subject '{}': {}", request.getSubject(), e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("❌ Failed to register Avro schema: " + e.getMessage());
        }
    }


    /**
     * 스키마 subject 목록 조회 (ADMIN 또는 MANAGER 권한 필요)
     */
    @GetMapping("/schemas/subjects")
    @RequirePermission({Permission.ADMIN, Permission.MANAGER})
    public ResponseEntity<?> listSubjects() {
        try {
            List<String> subjects = schemaService.listSubjects();
            return ResponseEntity.ok(subjects);
        } catch (Exception e) {
            log.error("❌ Failed to list schema subjects: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("❌ Failed to list schema subjects: " + e.getMessage());
        }
    }

    /**
     * 특정 subject의 최신 스키마 조회 (ADMIN 또는 MANAGER 권한 필요)
     */
    @GetMapping("/schemas/{subject}/latest")
    @RequirePermission({Permission.ADMIN, Permission.MANAGER})
    public ResponseEntity<?> getLatestSchema(@PathVariable String subject) {
        try {
            SchemaMetadata meta = schemaService.getLatest(subject);
            return ResponseEntity.ok(Map.of(
                    "subject", subject,
                    "id", meta.getId(),
                    "version", meta.getVersion(),
                    "schema", meta.getSchema()
            ));
        } catch (Exception e) {
            log.error("❌ Failed to get latest schema for subject '{}': {}", subject, e.getMessage());
            return ResponseEntity.internalServerError().body("❌ Failed to get latest schema: " + e.getMessage());
        }
    }

    // === 컨슈머 그룹 관리 ===

    /**
     * 컨슈머 그룹 목록 조회 (ADMIN 또는 MANAGER 권한 필요)
     */
    @GetMapping("/consumer-groups")
    @RequirePermission({Permission.ADMIN, Permission.MANAGER})
    public ResponseEntity<?> listConsumerGroups(Authentication authentication) {
        try {
            UserInfo userInfo = (UserInfo) authentication.getPrincipal();
            List<String> groups = consumerGroupService.listGroups(userInfo.getUsername(), userInfo.getPassword());
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            log.error("❌ Failed to list consumer groups: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("❌ Failed to list consumer groups: " + e.getMessage());
        }
    }

    /**
     * 컨슈머 그룹 요약 정보 조회 (ADMIN 또는 MANAGER 권한 필요)
     */
    @GetMapping("/consumer-groups/summary")
    @RequirePermission({Permission.ADMIN, Permission.MANAGER})
    public ResponseEntity<?> listConsumerGroupSummaries(Authentication authentication) {
        try {
            UserInfo userInfo = (UserInfo) authentication.getPrincipal();
            List<ConsumerGroupService.GroupSummary> summaries = 
                    consumerGroupService.listSummaries(userInfo.getUsername(), userInfo.getPassword());
            return ResponseEntity.ok(summaries);
        } catch (Exception e) {
            log.error("❌ Failed to list consumer group summaries: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("❌ Failed to list consumer group summaries: " + e.getMessage());
        }
    }

    /**
     * 특정 컨슈머 그룹 삭제 (ADMIN 또는 MANAGER 권한 필요)
     */
    @DeleteMapping("/consumer-groups/{groupId}")
    @RequirePermission({Permission.ADMIN, Permission.MANAGER})
    public ResponseEntity<?> deleteConsumerGroup(@PathVariable String groupId, Authentication authentication) {
        try {
            UserInfo userInfo = (UserInfo) authentication.getPrincipal();
            consumerGroupService.deleteGroup(groupId, userInfo.getUsername(), userInfo.getPassword());
            return ResponseEntity.ok("🗑️ Deleted consumer group: " + groupId);
        } catch (Exception e) {
            log.error("❌ Failed to delete consumer group '{}': {}", groupId, e.getMessage());
            return ResponseEntity.internalServerError().body("❌ Failed to delete consumer group: " + e.getMessage());
        }
    }
}
