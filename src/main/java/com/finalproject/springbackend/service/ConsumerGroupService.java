package com.finalproject.springbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.requests.ListOffsetsRequest;
import org.springframework.stereotype.Service;

import com.finalproject.springbackend.dto.*;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumerGroupService {

    private final KafkaAdminFactory factory;

    /** 내부/시스템 그룹 제외 규칙 (필요시 수정) */
    private boolean isInternal(String groupId) {
        return groupId.startsWith("_") || groupId.startsWith("console-consumer-");
    }

    /**
     * 컨슈머 그룹 ID 리스트 조회 (사용자 계정 사용)
     */
    public List<String> listGroups(String username, String password) throws ExecutionException, InterruptedException {
        try (AdminClient admin = factory.createAdminClient(username, password)) {
            var listings = admin.listConsumerGroups().all().get();
            List<String> groups = listings.stream()
                    .map(ConsumerGroupListing::groupId)
                    .filter(g -> !isInternal(g))
                    .sorted()
                    .collect(Collectors.toList());
            log.debug("📋 User {} listed {} consumer groups", username, groups.size());
            return groups;
        } catch (Exception e) {
            log.error("❌ Failed to list consumer groups for user {}: {}", username, e.getMessage());
            throw e;
        }
    }

    /**
     * 컨슈머 그룹 요약 정보: 상태/총 Lag/멤버 수/구독 토픽 (사용자 계정 사용)
     */
    public List<GroupSummary> listSummaries(String username, String password) throws Exception {
        try (AdminClient admin = factory.createAdminClient(username, password)) {
            // 그룹 수집
            var listings = admin.listConsumerGroups().all().get();
            var groupIds = listings.stream()
                    .map(ConsumerGroupListing::groupId)
                    .filter(g -> !isInternal(g))
                    .toList();
            if (groupIds.isEmpty()) return List.of();

            // 상세 설명
            var descMap = admin.describeConsumerGroups(groupIds).all().get();

            // 각 그룹의 커밋 오프셋
            Map<String, Map<TopicPartition, OffsetAndMetadata>> committedByGroup = new HashMap<>();
            for (String g : groupIds) {
                try {
                    committedByGroup.put(
                            g,
                            admin.listConsumerGroupOffsets(g).partitionsToOffsetAndMetadata().get()
                    );
                } catch (Exception e) {
                    log.warn("⚠️ Failed to get offsets for group '{}': {}", g, e.getMessage());
                    committedByGroup.put(g, Map.of()); // 권한/예외 시 빈 맵
                }
            }

            // 최신 오프셋 조회 대상 합치기
            Set<TopicPartition> allTps = committedByGroup.values().stream()
                    .flatMap(m -> m.keySet().stream())
                    .collect(Collectors.toSet());

            Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> latest = Map.of();
            if (!allTps.isEmpty()) {
                try {
                    latest = admin.listOffsets(
                            allTps.stream().collect(Collectors.toMap(tp -> tp,
                                    tp -> org.apache.kafka.clients.admin.OffsetSpec.latest()))
                    ).all().get();
                } catch (Exception e) {
                    log.warn("⚠️ Failed to get latest offsets: {}", e.getMessage());
                }
            }

            // 요약 계산
            List<GroupSummary> result = new ArrayList<>();
            for (String g : groupIds) {
                var desc = descMap.get(g);
                var committed = committedByGroup.getOrDefault(g, Map.of());

                long totalLag = 0L;
                Set<String> topics = new TreeSet<>();
                for (var entry : committed.entrySet()) {
                    var tp = entry.getKey();
                    long committedOffset = entry.getValue().offset();
                    long latestOffset = latest.getOrDefault(tp,
                            new ListOffsetsResult.ListOffsetsResultInfo(ListOffsetsRequest.EARLIEST_TIMESTAMP, -1L, Optional.empty())
                    ).offset();

                    if (committedOffset >= 0 && latestOffset >= 0) {
                        totalLag += Math.max(latestOffset - committedOffset, 0);
                    }
                    topics.add(tp.topic());
                }

                result.add(new GroupSummary(
                        g,
                        desc.state().toString(),
                        (long) desc.members().size(),
                        totalLag,
                        new ArrayList<>(topics)
                ));
            }
            // groupId 기준 정렬
            result.sort(Comparator.comparing(GroupSummary::groupId));
            log.debug("📊 User {} generated summaries for {} consumer groups", username, result.size());
            return result;
        } catch (Exception e) {
            log.error("❌ Failed to list consumer group summaries for user {}: {}", username, e.getMessage());
            throw e;
        }
    }

    /**
     * 특정 컨슈머 그룹 삭제 (사용자 계정 사용)
     */
    public void deleteGroup(String groupId, String username, String password) throws ExecutionException, InterruptedException {
        try (AdminClient admin = factory.createAdminClient(username, password)) {
            admin.deleteConsumerGroups(Collections.singletonList(groupId)).all().get();
            log.info("🗑️ Consumer group deleted: {} by user: {}", groupId, username);
        } catch (Exception e) {
            log.error("❌ Failed to delete consumer group '{}' by user {}: {}", groupId, username, e.getMessage());
            throw e;
        }
    }

    /**
     * 특정 그룹의 상세 정보(멤버 리스트 + 파티션별 오프셋/lag)
     */
    public GroupDetail getGroupDetail(String groupId, String username, String password) throws Exception {
        try (AdminClient admin = factory.createAdminClient(username, password)) {
            // 1) 그룹 설명 (members 포함)
            var descMap = admin.describeConsumerGroups(Collections.singletonList(groupId)).all().get();
            var desc = descMap.get(groupId);

            // 2) 그룹의 커밋 오프셋
            Map<TopicPartition, OffsetAndMetadata> committed = Map.of();
            try {
                committed = admin.listConsumerGroupOffsets(groupId).partitionsToOffsetAndMetadata().get();
            } catch (Exception e) {
                log.warn("⚠️ Failed to get offsets for group '{}': {}", groupId, e.getMessage());
            }

            // 3) 최신 오프셋 조회 대상
            Set<TopicPartition> allTps = committed.keySet();
            Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> latest = new HashMap<>();

            if (!allTps.isEmpty()) {
                try {
                    latest.putAll(admin.listOffsets(
                            allTps.stream().collect(Collectors.toMap(tp -> tp,
                                    tp -> OffsetSpec.latest()))
                    ).all().get());
                } catch (Exception e) {
                    log.warn("⚠️ Failed to get latest offsets for group '{}': {}", groupId, e.getMessage());
                }
            }

            if (!allTps.isEmpty()) {
                try {
                    latest = admin.listOffsets(
                            allTps.stream().collect(Collectors.toMap(tp -> tp,
                                    tp -> OffsetSpec.latest()))
                    ).all().get();
                } catch (Exception e) {
                    log.warn("⚠️ Failed to get latest offsets for group '{}': {}", groupId, e.getMessage());
                }
            }

            // 4) 멤버 -> assigned partition별 정보 구성
            Map<TopicPartition, OffsetAndMetadata> finalCommitted = committed;
            Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> finalLatest = latest;
            List<MemberInfo> members = desc.members().stream().map(m -> {
                List<AssignedPartition> assignedList = m.assignment().topicPartitions().stream().map(tp -> {
                    long committedOffset = finalCommitted.getOrDefault(tp, new OffsetAndMetadata(-1L)).offset();

                    long latestOffset = -1L;
                    if (finalLatest.containsKey(tp)) {
                        latestOffset = finalLatest.get(tp).offset();
                    }

                    long lag = -1L;
                    if (committedOffset >= 0 && latestOffset >= 0) {
                        lag = Math.max(latestOffset - committedOffset, 0);
                    }

                    return new AssignedPartition(
                            tp.topic(),
                            tp.partition(),
                            committedOffset,
                            latestOffset,
                            lag
                    );
                }).sorted(
                        Comparator.comparing(AssignedPartition::getTopic)
                                .thenComparing(AssignedPartition::getPartition)
                ).collect(Collectors.toList());

                return new MemberInfo(m.consumerId(), m.clientId(), m.host(), assignedList);
            }).collect(Collectors.toList());

            // 5) 그룹 totalLag 계산 (committed 기준)
            Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> finalLatest1 = latest;
            long totalLag = committed.entrySet().stream().mapToLong(e -> {
                var tp = e.getKey();
                long committedOffset = e.getValue().offset();
                long latestOffset = finalLatest1.getOrDefault(tp, new ListOffsetsResult.ListOffsetsResultInfo(ListOffsetsRequest.EARLIEST_TIMESTAMP, -1L, Optional.empty())).offset();
                if (committedOffset >= 0 && latestOffset >= 0) return Math.max(latestOffset - committedOffset, 0);
                return 0L;
            }).sum();

            // 6) DTO 반환
            GroupDetail groupDetail = new GroupDetail(); // DTO 분리에 따라 생성자 대신 setter 사용
            groupDetail.setGroupId(groupId);
            groupDetail.setState(desc.state().toString());
            groupDetail.setCoordinator(desc.coordinator() != null ? desc.coordinator().host() + ":" + desc.coordinator().port() : null);
            groupDetail.setMembers(members);
            groupDetail.setTotalLag(totalLag);

            return groupDetail;
        } catch (Exception e) {
            log.error("❌ Failed to get group detail for {} by {}: {}", groupId, username, e.getMessage());
            throw e;
        }
    }


    /** 요약 DTO */
    public record GroupSummary(
            String groupId,
            String state,
            Long members,
            Long totalLag,
            List<String> topics
    ) {}
}
