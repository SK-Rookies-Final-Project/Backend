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
public class Certified2TimeConsumer {

    private static final Logger log = LoggerFactory.getLogger(Certified2TimeConsumer.class);
    private final SseService sseService;
    private final KafkaAdminFactory kafkaFactory;


    @Value("${KAFKA_TOPIC_CERTIFIED_2TIME}")
    private String topicName;
    
    private final Map<String, Consumer<String, byte[]>> userConsumers = new ConcurrentHashMap<>();
    private final Map<String, ExecutorService> userExecutors = new ConcurrentHashMap<>();
    public void startConsumerForUser(String username, String password) {
        if (userConsumers.containsKey(username)) {
            // Consumer 이미 실행 중
            return;
        }
        
        // Certified2TimeConsumer 시작 - 고유한 UUID 기반 그룹 ID 사용
        
        try {
            // 고유한 Consumer Group ID 생성 (UUID 기반)
            String uniqueGroupId = kafkaFactory.generateUniqueConsumerGroupId(username, topicName);
            Consumer<String, byte[]> consumer = kafkaFactory.createConsumer(username, password, uniqueGroupId);
            userConsumers.put(username, consumer);
            
            ExecutorService executor = Executors.newSingleThreadExecutor();
            userExecutors.put(username, executor);
            
            executor.submit(() -> {
                try {
                    consumer.subscribe(Collections.singletonList(topicName));
                    // 토픽 구독 시작
                    
                    while (!Thread.currentThread().isInterrupted()) {
                        ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(1000));
                        for (ConsumerRecord<String, byte[]> record : records) {
                            byte[] rawBytes = record.value();
                            // 반복적인 로그인 시도 로그 수신
                            
                            // 바이트 배열을 안전하게 문자열로 변환
                            String message;
                            try {
                                message = new String(rawBytes, "UTF-8");
                            } catch (Exception e) {
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
            // Certified2TimeConsumer 중지
            consumer.close();
        }
        
        if (executor != null) {
            executor.shutdown();
        }
    }

    
    private void sendMessageToClients(String rawMessage) {
        // rawMessage를 JSON 형식으로 래핑하여 전송
        String jsonMessage = wrapMessageAsJson(rawMessage);
        
        // 기존 방식 (하위 호환성)
        Map<String, ResponseBodyEmitter> emitters = sseService.getCertified2TimeEmitters();
        
        emitters.forEach((clientId, emitter) -> {
            try {
                emitter.send(jsonMessage, MediaType.TEXT_EVENT_STREAM);
                log.info("📤 [auth_failure] SSE 메시지 전송 완료: {}", jsonMessage);
            } catch (IOException e) {
                emitters.remove(clientId);
            }
        });
        
        // 사용자별 SSE 연결에도 전송
        Map<String, Map<String, ResponseBodyEmitter>> allUserEmitters = sseService.getAllUserCertified2TimeEmitters();
        
        allUserEmitters.forEach((username, userEmitters) -> {
            // ConcurrentModificationException 방지를 위해 복사본 생성
            Map<String, ResponseBodyEmitter> emittersCopy = new ConcurrentHashMap<>(userEmitters);
            emittersCopy.forEach((clientId, emitter) -> {
                try {
                    // SSE 메시지 전송 (JSON 형식으로 래핑된 메시지 전송)
                    emitter.send(jsonMessage, MediaType.TEXT_EVENT_STREAM);
                    log.info("📤 [auth_failure] SSE 메시지 전송 완료: {}", jsonMessage);
                } catch (IOException e) {
                    // 연결이 중단된 경우 제거
                    userEmitters.remove(clientId);
                } catch (Exception e) {
                    userEmitters.remove(clientId);
                }
            });
        });
    }
    
    /**
     * rawMessage를 JSON 객체로 파싱하여 반환
     * @param rawMessage 원본 메시지 (JSON 문자열)
     * @return 파싱된 JSON 메시지
     */
    private String wrapMessageAsJson(String rawMessage) {
        return KafkaMessageUtil.parseMessageToJson(rawMessage, "certified-2time");
    }
    
}