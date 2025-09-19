package com.finalproject.springbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.errors.TopicExistsException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicService {

    private final KafkaAdminFactory factory;
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    /**
     * 토픽 생성 (사용자 계정 사용)
     */
    public void createTopic(String name, int partitions, short replicationFactor, String username, String password) throws Exception {
        try (AdminClient admin = factory.createAdminClient(username, password)) {
            NewTopic topic = new NewTopic(name, partitions, replicationFactor);
            try {
                admin.createTopics(Collections.singleton(topic)).all().get();
                log.info("✅ Created topic: {} by user: {}", name, username);
            } catch (Exception e) {
                if (e.getCause() instanceof TopicExistsException) {
                    log.warn("⚠️ Topic already exists: {}", name);
                } else {
                    log.error("❌ Failed to create topic '{}' by user {}: {}", name, username, e.getMessage());
                    throw e;
                }
            }
        }
    }

    /**
     * 토픽 목록 조회 (사용자 계정 사용) - 로그인 인증용
     */
    public List<String> listTopicsForUser(String username, String password) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try (AdminClient admin = factory.createAdminClient(username, password)) {
                List<String> topics = admin.listTopics()
                        .names()
                        .get(10, TimeUnit.SECONDS)  // 10초 타임아웃
                        .stream()
                        .filter(topic -> !topic.startsWith("__"))  // 내부 토픽 제외
                        .sorted()
                        .collect(Collectors.toList());
                
                log.debug("📋 User {} can access {} topics", username, topics.size());
                return topics;
                
            } catch (Exception e) {
                lastException = e;
                log.warn("Kafka 토픽 조회 실패 {}/{} for user {}: {}", attempt, MAX_RETRIES, username, e.getMessage());
                
                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempt);  // 지수 백오프
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new Exception("토픽 조회 중 인터럽트 발생", ie);
                    }
                }
            }
        }
        
        log.error("Kafka 토픽 조회 최종 실패 for user {}: {}", username, lastException.getMessage());
        throw new Exception("Kafka 연결 실패 (사용자: " + username + "): " + lastException.getMessage(), lastException);
    }


    /**
     * 토픽 상세 정보 조회 (사용자 계정 사용)
     */
    public Map<String, TopicDescription> describeTopics(List<String> topicNames, String username, String password) throws Exception {
        try (AdminClient admin = factory.createAdminClient(username, password)) {
            Map<String, TopicDescription> descriptions = admin.describeTopics(topicNames).allTopicNames().get();
            log.debug("📄 User {} described {} topics", username, descriptions.size());
            return descriptions;
        } catch (Exception e) {
            log.error("❌ Failed to describe topics for user {}: {}", username, e.getMessage());
            throw e;
        }
    }
}
