package com.finalproject.springbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicService {

    private final KafkaAdminFactory factory;
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    public List<String> listTopics(String username, String password) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try (AdminClient admin = factory.createAdminClient(username, password)) {
                log.info("Kafka 토픽 조회 시도 {}/{}: {}", attempt, MAX_RETRIES, username);
                
                List<String> topics = admin.listTopics()
                        .names()
                        .get(10, TimeUnit.SECONDS)  // 10초 타임아웃
                        .stream()
                        .filter(topic -> !topic.startsWith("__"))  // 내부 토픽 제외
                        .sorted()
                        .collect(Collectors.toList());
                
                log.info("Kafka 토픽 조회 성공: {} ({}개 토픽)", username, topics.size());
                return topics;
                
            } catch (Exception e) {
                lastException = e;
                log.warn("Kafka 토픽 조회 실패 {}/{}: {} - {}", attempt, MAX_RETRIES, username, e.getMessage());
                
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
        
        log.error("Kafka 토픽 조회 최종 실패: {} - {}", username, lastException.getMessage());
        throw new Exception("Kafka 연결 실패: " + lastException.getMessage(), lastException);
    }
}
