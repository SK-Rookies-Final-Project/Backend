package com.finalproject.springbackend.service;

import com.finalproject.springbackend.util.KafkaMessageUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class ResourceLevelFalseConsumer {

    private static final Logger log = LoggerFactory.getLogger(ResourceLevelFalseConsumer.class);
    private final SseService sseService;
    private final KafkaAdminFactory kafkaFactory;

    @Value("${CONSUMER_GROUP_ID}")
    private String consumerGroupId;

    @Value("${KAFKA_TOPIC_RESOURCE_LEVEL_FALSE}")
    private String topicName;
    
    private final Map<String, Consumer<String, byte[]>> userConsumers = new ConcurrentHashMap<>();
    private final Map<String, ExecutorService> userExecutors = new ConcurrentHashMap<>();
    public void startConsumerForUser(String username, String password) {
        if (userConsumers.containsKey(username)) {
            log.info("🔄 사용자 {} Consumer 이미 실행 중 - 기존 Consumer 사용", username);
            return;
        }
        
        log.info("🚀 사용자 {} ResourceLevelFalse Consumer 새로 시작", username);
        
        try {
            Consumer<String, byte[]> consumer = createConsumer(username, password);
            userConsumers.put(username, consumer);
            
            ExecutorService executor = Executors.newSingleThreadExecutor();
            userExecutors.put(username, executor);
            
            executor.submit(() -> {
                try {
                    consumer.subscribe(Collections.singletonList(topicName));
                    log.info("🎯 사용자 {} Consumer가 토픽 '{}' 구독 시작", username, topicName);
                    
                    while (!Thread.currentThread().isInterrupted()) {
                        ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(1000));
                        
                        if (!records.isEmpty()) {
                            log.info("📨 [데이터 수신] 사용자: {}, 토픽: {}, 메시지 수: {} 개", 
                                    username, topicName, records.count());
                        }
                        
                        for (ConsumerRecord<String, byte[]> record : records) {
                            byte[] rawBytes = record.value();
                            // 리소스 권한 부족 로그 수신
                            
                            // 바이트 배열을 안전하게 문자열로 변환
                            String message;
                            try {
                                message = new String(rawBytes, "UTF-8");
                                log.info("📄 수신된 메시지 내용: {}", message);
                            } catch (Exception e) {
                                log.error("바이트 배열을 문자열로 변환 실패: {}", e.getMessage());
                                message = "{\"error\": \"메시지 변환 실패\", \"rawBytes\": \"" + 
                                         java.util.Base64.getEncoder().encodeToString(rawBytes) + "\"}";
                            }
                            
                            // SSE로 rawMessage만 전송
                            sendMessageToClients(message);
                        }
                    }
                } catch (Exception e) {
                    log.error("사용자 {}의 Consumer 실행 중 오류: {}", username, e.getMessage());
                } finally {
                    consumer.close();
                }
            });
            
        } catch (Exception e) {
            log.error("사용자 {}의 Consumer 생성 실패: {}", username, e.getMessage());
        }
    }

    public void stopConsumerForUser(String username) {
        Consumer<String, byte[]> consumer = userConsumers.remove(username);
        ExecutorService executor = userExecutors.remove(username);
        
        if (consumer != null) {
            // ResourceLevelFalseConsumer 중지
            consumer.close();
        }
        
        if (executor != null) {
            executor.shutdown();
        }
    }

    private Consumer<String, byte[]> createConsumer(String username, String password) {
        return kafkaFactory.createConsumer(username, password, consumerGroupId);
    }
    
    private void sendMessageToClients(String rawMessage) {
        // rawMessage를 JSON 형식으로 래핑하여 전송
        String jsonMessage = wrapMessageAsJson(rawMessage);
        
        // 기존 방식 (하위 호환성)
        Map<String, ResponseBodyEmitter> emitters = sseService.getResourceLevelFalseEmitters();
        log.info("🔍 기존 방식 SSE emitter 수: {}", emitters.size());
        emitters.forEach((clientId, emitter) -> {
            try {
                emitter.send(jsonMessage, MediaType.TEXT_EVENT_STREAM);
                log.info("✅ 기존 방식 SSE 전송 성공: Client ID {}, 전송 데이터: {}", clientId, jsonMessage);
            } catch (IOException e) {
                log.error("❌ SSE 전송 오류: {}", e.getMessage());
                emitters.remove(clientId);
            }
        });
        
        // 사용자별 SSE 연결에도 전송
        Map<String, Map<String, ResponseBodyEmitter>> allUserEmitters = sseService.getAllUserResourceLevelFalseEmitters();
        log.info("🔍 사용자별 SSE emitter 현황: 총 {} 명의 사용자", allUserEmitters.size());
        
        allUserEmitters.forEach((username, userEmitters) -> {
            log.info("🔍 사용자 {} - emitter 수: {}", username, userEmitters.size());
            // ConcurrentModificationException 방지를 위해 복사본 생성
            Map<String, ResponseBodyEmitter> emittersCopy = new ConcurrentHashMap<>(userEmitters);
            emittersCopy.forEach((clientId, emitter) -> {
                try {
                    // SSE 메시지 전송 (JSON 형식으로 래핑된 메시지 전송)
                    emitter.send(jsonMessage, MediaType.TEXT_EVENT_STREAM);
                    log.info("✅ 사용자별 SSE 전송 성공: 사용자 {}, Client ID {}, 전송 데이터: {}", username, clientId, jsonMessage);
                } catch (IOException e) {
                    log.warn("❌ SSE 전송 실패 (연결 중단): 사용자 {}, 오류: {}", username, e.getMessage());
                    // 연결이 중단된 경우 제거
                    userEmitters.remove(clientId);
                } catch (Exception e) {
                    log.error("❌ SSE 전송 오류: 사용자 {}, 오류: {}", username, e.getMessage());
                    userEmitters.remove(clientId);
                }
            });
        });
        
        // SSE emitter가 없을 경우 경고
        if (emitters.isEmpty() && allUserEmitters.isEmpty()) {
            log.warn("⚠️ [데이터 전송 실패] 활성화된 SSE 연결이 없습니다! 메시지가 전송되지 않았습니다.");
        } else {
            int totalConnections = emitters.size() + allUserEmitters.values().stream().mapToInt(Map::size).sum();
            log.info("📡 [데이터 전송 완료] 총 {} 개의 SSE 연결에 메시지 전송 완료", totalConnections);
        }
    }
    
    /**
     * rawMessage를 JSON 형식으로 래핑하여 반환
     * @param rawMessage 원본 메시지
     * @return JSON 형식으로 래핑된 메시지
     */
    private String wrapMessageAsJson(String rawMessage) {
        return KafkaMessageUtil.parseMessageToJson(rawMessage, "resource-level-false");
    }
    
}