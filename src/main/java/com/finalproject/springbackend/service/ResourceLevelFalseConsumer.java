package com.finalproject.springbackend.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class ResourceLevelFalseConsumer {

    private static final Logger log = LoggerFactory.getLogger(ResourceLevelFalseConsumer.class);
    private final SseService sseService;
    
    @Value("${OHIO_KAFKA_BOOTSTRAP_SERVERS}")
    private String bootstrapServers;

    @Value("${CONSUMER_GROUP_ID}")
    private String consumerGroupId;

    @Value("${KAFKA_TOPIC_RESOURCE_LEVEL_FALSE}")
    private String topicName;
    
    private final Map<String, Consumer<String, byte[]>> userConsumers = new ConcurrentHashMap<>();
    private final Map<String, ExecutorService> userExecutors = new ConcurrentHashMap<>();
    public void startConsumerForUser(String username, String password) {
        if (userConsumers.containsKey(username)) {
            // Consumer 이미 실행 중
            return;
        }
        
        // ResourceLevelFalseConsumer 시작
        
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
                            // 리소스 권한 부족 로그 수신
                            
                            // 바이트 배열을 안전하게 문자열로 변환
                            String message;
                            try {
                                message = new String(rawBytes, "UTF-8");
                                log.debug("변환된 메시지: {}", message);
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
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId + "-" + username);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put("security.protocol", "SASL_PLAINTEXT");
        props.put("sasl.mechanism", "SCRAM-SHA-512");
        props.put("sasl.jaas.config",
                "org.apache.kafka.common.security.scram.ScramLoginModule required " +
                        "username=\"" + username + "\" password=\"" + password + "\";");
        
        return new KafkaConsumer<>(props);
    }
    
    private void sendMessageToClients(String rawMessage) {
        // rawMessage만 전송 (JSON 래핑 없이)
        
        // 기존 방식 (하위 호환성)
        Map<String, ResponseBodyEmitter> emitters = sseService.getResourceLevelFalseEmitters();
        emitters.forEach((clientId, emitter) -> {
            try {
                emitter.send(rawMessage, MediaType.TEXT_EVENT_STREAM);
            } catch (IOException e) {
                log.error("SSE 전송 오류: {}", e.getMessage());
                emitters.remove(clientId);
            }
        });
        
        // 사용자별 SSE 연결에도 전송
        Map<String, Map<String, ResponseBodyEmitter>> allUserEmitters = sseService.getAllUserResourceLevelFalseEmitters();
        allUserEmitters.forEach((username, userEmitters) -> {
            // ConcurrentModificationException 방지를 위해 복사본 생성
            Map<String, ResponseBodyEmitter> emittersCopy = new ConcurrentHashMap<>(userEmitters);
            emittersCopy.forEach((clientId, emitter) -> {
                try {
                    // SSE 메시지 전송 (rawMessage만 전송)
                    emitter.send(rawMessage, MediaType.TEXT_EVENT_STREAM);
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
    
}