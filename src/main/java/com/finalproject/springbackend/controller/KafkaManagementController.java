package com.finalproject.springbackend.controller;

import com.finalproject.springbackend.annotation.RequirePermission;
import com.finalproject.springbackend.dto.*;
import com.finalproject.springbackend.service.TopicService;
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
     * 토픽 삭제 (ADMIN 또는 MANAGER 권한 필요)
     */
    @DeleteMapping("/topics/{topicName}")
    @RequirePermission({Permission.ADMIN, Permission.MANAGER})
    public ResponseEntity<?> deleteTopic(@PathVariable String topicName, Authentication authentication) {
        try {
            UserInfo userInfo = (UserInfo) authentication.getPrincipal();
            topicService.deleteTopic(topicName, userInfo.getUsername(), userInfo.getPassword());
            return ResponseEntity.ok("🗑️ Deleted topic: " + topicName);
        } catch (Exception e) {
            log.error("❌ Failed to delete topic '{}': {}", topicName, e.getMessage());
            return ResponseEntity.internalServerError().body("❌ Failed to delete topic: " + e.getMessage());
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

}
