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
public class CertifiedNotMoveConsumer {

    private static final Logger log = LoggerFactory.getLogger(CertifiedNotMoveConsumer.class);
    private final SseService sseService;
    private final KafkaAdminFactory kafkaFactory;

    @Value("${CONSUMER_GROUP_ID}")
    private String consumerGroupId;

    @Value("${KAFKA_TOPIC_CERTIFIED_NOT_MOVE}")
    private String topicName;
    
    private final Map<String, Consumer<String, byte[]>> userConsumers = new ConcurrentHashMap<>();
    private final Map<String, ExecutorService> userExecutors = new ConcurrentHashMap<>();
    public void startConsumerForUser(String username, String password) {
        if (userConsumers.containsKey(username)) {
            // Consumer 이미 실행 중
            return;
        }
        
        // CertifiedNotMoveConsumer 시작
        
        try {
            Consumer<String, byte[]> consumer = createConsumer(username, password);
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
                            // 의심스러운 로그인 시도 로그 수신
                            
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
            // CertifiedNotMoveConsumer 중지
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
        Map<String, ResponseBodyEmitter> emitters = sseService.getCertifiedNotMoveEmitters();
        emitters.forEach((clientId, emitter) -> {
            try {
                emitter.send(jsonMessage, MediaType.TEXT_EVENT_STREAM);
            } catch (IOException e) {
                log.error("SSE 전송 오류: {}", e.getMessage());
                emitters.remove(clientId);
            }
        });
        
        // 사용자별 SSE 연결에도 전송
        Map<String, Map<String, ResponseBodyEmitter>> allUserEmitters = sseService.getAllUserCertifiedNotMoveEmitters();
        allUserEmitters.forEach((username, userEmitters) -> {
            // ConcurrentModificationException 방지를 위해 복사본 생성
            Map<String, ResponseBodyEmitter> emittersCopy = new ConcurrentHashMap<>(userEmitters);
            emittersCopy.forEach((clientId, emitter) -> {
                try {
                    // SSE 메시지 전송 (JSON 형식으로 래핑된 메시지 전송)
                    emitter.send(jsonMessage, MediaType.TEXT_EVENT_STREAM);
                } catch (IOException e) {
                    log.warn("SSE 전송 실패 (연결 중단): 사용자 {}, 오류: {}", username, e.getMessage());
                    // 연결이 중단된 경우 제거
                    userEmitters.remove(clientId);
                } catch (Exception e) {
                    log.error("SSE 전송 오류: 사용자 {}, 오류: {}", username, e.getMessage());
                    userEmitters.remove(clientId);
                }
            });
        });
    }
    
    /**
     * rawMessage를 JSON 형식으로 래핑하여 반환
     * @param rawMessage 원본 메시지
     * @return JSON 형식으로 래핑된 메시지
     */
    private String wrapMessageAsJson(String rawMessage) {
        return KafkaMessageUtil.parseMessageToJson(rawMessage, "certified-notMove");
    }
    
}