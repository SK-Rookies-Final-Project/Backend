package com.finalproject.springbackend.service;

import com.finalproject.springbackend.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.config.ConfigResource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaManagementService {

    private final KafkaAdminFactory factory;


    // === Config 관리 ===

    /**
     * 클러스터 설정 조회
     */
    public ConfigInfoDTO getClusterConfigs(String username, String password) throws Exception {
        try (AdminClient admin = factory.createAdminClient(username, password)) {
            ConfigResource clusterResource = new ConfigResource(ConfigResource.Type.BROKER, "");
            Map<ConfigResource, Config> configs = admin.describeConfigs(Collections.singleton(clusterResource))
                    .all()
                    .get(10, TimeUnit.SECONDS);

            Config clusterConfig = configs.get(clusterResource);
            Map<String, String> configMap = new HashMap<>();
            for (ConfigEntry entry : clusterConfig.entries()) {
                configMap.put(entry.name(), entry.value());
            }

            return ConfigInfoDTO.builder()
                    .resourceType("CLUSTER")
                    .resourceName("")
                    .configs(configMap)
                    .build();
        } catch (Exception e) {
            log.error("❌ Failed to get cluster configs for user {}: {}", username, e.getMessage());
            throw e;
        }
    }

    /**
     * 토픽 설정 조회
     */
    public ConfigInfoDTO getTopicConfigs(String topicName, String username, String password) throws Exception {
        try (AdminClient admin = factory.createAdminClient(username, password)) {
            ConfigResource topicResource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);
            Map<ConfigResource, Config> configs = admin.describeConfigs(Collections.singleton(topicResource))
                    .all()
                    .get(10, TimeUnit.SECONDS);

            Config topicConfig = configs.get(topicResource);
            Map<String, String> configMap = new HashMap<>();
            for (ConfigEntry entry : topicConfig.entries()) {
                configMap.put(entry.name(), entry.value());
            }

            return ConfigInfoDTO.builder()
                    .resourceType("TOPIC")
                    .resourceName(topicName)
                    .configs(configMap)
                    .build();
        } catch (Exception e) {
            log.error("❌ Failed to get topic configs for topic '{}' and user {}: {}", topicName, username, e.getMessage());
            throw e;
        }
    }

    /**
     * 설정 업데이트
     */
    public void updateConfigs(ConfigInfoDTO.ConfigUpdateRequest request, String username, String password) throws Exception {
        try (AdminClient admin = factory.createAdminClient(username, password)) {
            ConfigResource.Type resourceType;
            if ("CLUSTER".equals(request.getResourceType()) || "BROKER".equals(request.getResourceType())) {
                resourceType = ConfigResource.Type.BROKER;
            } else if ("TOPIC".equals(request.getResourceType())) {
                resourceType = ConfigResource.Type.TOPIC;
            } else {
                throw new IllegalArgumentException("Unsupported resource type: " + request.getResourceType());
            }
            ConfigResource resource = new ConfigResource(resourceType, request.getResourceName());

            Map<String, String> configMap = request.getConfigs();
            List<ConfigEntry> configEntries = new ArrayList<>();
            for (Map.Entry<String, String> entry : configMap.entrySet()) {
                configEntries.add(new ConfigEntry(entry.getKey(), entry.getValue()));
            }

            Config config = new Config(configEntries);
            Map<ConfigResource, Config> configs = new HashMap<>();
            configs.put(resource, config);

            admin.alterConfigs(configs).all().get();
            log.info("✅ Updated configs for {}: {}", request.getResourceType(), request.getResourceName());
        } catch (Exception e) {
            log.error("❌ Failed to update configs: {}", e.getMessage());
            throw e;
        }
    }

    // === 클러스터 정보 ===

    /**
     * 클러스터 정보 조회
     */
    public ClusterInfoDTO getClusterInfo(String username, String password) throws Exception {
        try (AdminClient admin = factory.createAdminClient(username, password)) {
            // 클러스터 ID 조회
            String clusterId = admin.describeCluster().clusterId().get(10, TimeUnit.SECONDS);

            // 브로커 정보 조회
            Collection<Node> nodes = admin.describeCluster().nodes().get(10, TimeUnit.SECONDS);
            List<ClusterInfoDTO.BrokerInfo> brokers = nodes.stream()
                    .map(node -> ClusterInfoDTO.BrokerInfo.builder()
                            .id(node.id())
                            .host(node.host())
                            .port(node.port())
                            .rack(node.rack())
                            .isController(false) // TODO: Determine controller
                            .build())
                    .collect(Collectors.toList());

            // 토픽 정보 조회
            Set<String> topicNames = admin.listTopics().names().get(10, TimeUnit.SECONDS);
            int totalTopics = topicNames.size();
            int totalPartitions = 0; // TODO: Calculate total partitions

            return ClusterInfoDTO.builder()
                    .clusterId(clusterId)
                    .brokers(brokers)
                    .clusterConfigs(new HashMap<>())
                    .totalPartitions(totalPartitions)
                    .totalTopics(totalTopics)
                    .build();
        } catch (Exception e) {
            log.error("❌ Failed to get cluster info for user {}: {}", username, e.getMessage());
            throw e;
        }
    }

    // === 파티션 관리 ===

    /**
     * 파티션 정보 조회
     */
    public List<PartitionInfoDTO> getPartitionInfo(String topicName, String username, String password) throws Exception {
        try (AdminClient admin = factory.createAdminClient(username, password)) {
            Map<String, TopicDescription> descriptions = admin.describeTopics(Collections.singleton(topicName))
                    .allTopicNames()
                    .get(10, TimeUnit.SECONDS);

            TopicDescription description = descriptions.get(topicName);
            if (description == null) {
                throw new Exception("Topic not found: " + topicName);
            }

            List<PartitionInfoDTO> partitionInfos = new ArrayList<>();
            for (TopicPartitionInfo partition : description.partitions()) {
                List<Integer> replicas = new ArrayList<>();
                for (Node replica : partition.replicas()) {
                    replicas.add(replica.id());
                }
                
                List<Integer> isr = new ArrayList<>();
                for (Node node : partition.isr()) {
                    isr.add(node.id());
                }
                
                partitionInfos.add(PartitionInfoDTO.builder()
                        .topicName(topicName)
                        .partition(partition.partition())
                        .leader(partition.leader() != null ? partition.leader().id() : -1)
                        .replicas(replicas)
                        .isr(isr)
                        .offline(partition.leader() == null)
                        .size(0) // TODO: Calculate partition size
                        .offset(0) // TODO: Calculate partition offset
                        .build());
            }
            return partitionInfos;
        } catch (Exception e) {
            log.error("❌ Failed to get partition info for topic '{}' and user {}: {}", topicName, username, e.getMessage());
            throw e;
        }
    }

    /**
     * 파티션 재할당
     */
    public void reassignPartitions(List<PartitionInfoDTO.PartitionReassignmentRequest> requests, String username, String password) throws Exception {
        try (AdminClient admin = factory.createAdminClient(username, password)) {
            Map<TopicPartition, Optional<NewPartitionReassignment>> reassignments = new HashMap<>();
            
            for (PartitionInfoDTO.PartitionReassignmentRequest request : requests) {
                TopicPartition tp = new TopicPartition(request.getTopicName(), request.getPartition());
                NewPartitionReassignment reassignment = new NewPartitionReassignment(request.getReplicas());
                reassignments.put(tp, Optional.of(reassignment));
            }

            admin.alterPartitionReassignments(reassignments).all().get();
            log.info("🔄 Reassigned {} partitions", requests.size());
        } catch (Exception e) {
            log.error("❌ Failed to reassign partitions: {}", e.getMessage());
            throw e;
        }
    }
}
