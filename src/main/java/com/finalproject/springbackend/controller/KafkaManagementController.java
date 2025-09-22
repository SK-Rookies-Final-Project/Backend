package com.finalproject.springbackend.controller;

import com.finalproject.springbackend.annotation.RequirePermission;
import com.finalproject.springbackend.dto.*;
import com.finalproject.springbackend.service.KafkaManagementService;
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
    private final KafkaManagementService kafkaManagementService;

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


    // === Config 관리 ===

    /**
     * 클러스터 설정 조회 (MANAGER 권한 필요)
     */
    @GetMapping("/configs/cluster")
    @RequirePermission({Permission.MANAGER})
    public ResponseEntity<?> getClusterConfigs(Authentication authentication) {
        try {
            UserInfo userInfo = (UserInfo) authentication.getPrincipal();
            ConfigInfoDTO configs = kafkaManagementService.getClusterConfigs(userInfo.getUsername(), userInfo.getPassword());
            return ResponseEntity.ok(configs);
        } catch (Exception e) {
            log.error("❌ Failed to get cluster configs: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("❌ Failed to get cluster configs: " + e.getMessage());
        }
    }

    /**
     * 토픽 설정 조회 (MANAGER 권한 필요)
     */
    @GetMapping("/configs/topics/{topicName}")
    @RequirePermission({Permission.MANAGER})
    public ResponseEntity<?> getTopicConfigs(@PathVariable String topicName, Authentication authentication) {
        try {
            UserInfo userInfo = (UserInfo) authentication.getPrincipal();
            ConfigInfoDTO configs = kafkaManagementService.getTopicConfigs(topicName, userInfo.getUsername(), userInfo.getPassword());
            return ResponseEntity.ok(configs);
        } catch (Exception e) {
            log.error("❌ Failed to get topic configs for '{}': {}", topicName, e.getMessage());
            return ResponseEntity.internalServerError().body("❌ Failed to get topic configs: " + e.getMessage());
        }
    }

    /**
     * 설정 업데이트 (MANAGER 권한 필요)
     */
    @PutMapping("/configs")
    @RequirePermission({Permission.MANAGER})
    public ResponseEntity<?> updateConfigs(@RequestBody ConfigInfoDTO.ConfigUpdateRequest request, Authentication authentication) {
        try {
            UserInfo userInfo = (UserInfo) authentication.getPrincipal();
            kafkaManagementService.updateConfigs(request, userInfo.getUsername(), userInfo.getPassword());
            return ResponseEntity.ok("✅ Updated configs for " + request.getResourceType() + ": " + request.getResourceName());
        } catch (Exception e) {
            log.error("❌ Failed to update configs: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("❌ Failed to update configs: " + e.getMessage());
        }
    }

    // === 클러스터 정보 ===

    /**
     * 클러스터 정보 조회 (MANAGER 권한 필요)
     */
    @GetMapping("/cluster/info")
    @RequirePermission({Permission.MANAGER})
    public ResponseEntity<?> getClusterInfo(Authentication authentication) {
        try {
            UserInfo userInfo = (UserInfo) authentication.getPrincipal();
            ClusterInfoDTO clusterInfo = kafkaManagementService.getClusterInfo(userInfo.getUsername(), userInfo.getPassword());
            return ResponseEntity.ok(clusterInfo);
        } catch (Exception e) {
            log.error("❌ Failed to get cluster info: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("❌ Failed to get cluster info: " + e.getMessage());
        }
    }

    // === 파티션 관리 ===

    /**
     * 파티션 정보 조회 (MANAGER 권한 필요)
     */
    @GetMapping("/partitions/{topicName}")
    @RequirePermission({Permission.MANAGER})
    public ResponseEntity<?> getPartitionInfo(@PathVariable String topicName, Authentication authentication) {
        try {
            UserInfo userInfo = (UserInfo) authentication.getPrincipal();
            List<PartitionInfoDTO> partitions = kafkaManagementService.getPartitionInfo(topicName, userInfo.getUsername(), userInfo.getPassword());
            return ResponseEntity.ok(partitions);
        } catch (Exception e) {
            log.error("❌ Failed to get partition info for topic '{}': {}", topicName, e.getMessage());
            return ResponseEntity.internalServerError().body("❌ Failed to get partition info: " + e.getMessage());
        }
    }

    /**
     * 파티션 재할당 (MANAGER 권한 필요)
     */
    @PostMapping("/partitions/reassign")
    @RequirePermission({Permission.MANAGER})
    public ResponseEntity<?> reassignPartitions(@RequestBody List<PartitionInfoDTO.PartitionReassignmentRequest> requests, Authentication authentication) {
        try {
            UserInfo userInfo = (UserInfo) authentication.getPrincipal();
            kafkaManagementService.reassignPartitions(requests, userInfo.getUsername(), userInfo.getPassword());
            return ResponseEntity.ok("🔄 Reassigned " + requests.size() + " partitions");
        } catch (Exception e) {
            log.error("❌ Failed to reassign partitions: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("❌ Failed to reassign partitions: " + e.getMessage());
        }
    }

}
