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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
public class CertifiedNotMoveConsumer {

    private static final Logger log = LoggerFactory.getLogger(CertifiedNotMoveConsumer.class);
    private final SseService sseService;
    
    @Value("${OHIO_KAFKA_BOOTSTRAP_SERVERS}")
    private String bootstrapServers;

    @Value("${CONSUMER_GROUP_ID}")
    private String consumerGroupId;

    @Value("${KAFKA_TOPIC_CERTIFIED_NOT_MOVE}")
    private String topicName;
    
    private final Map<String, Consumer<String, String>> userConsumers = new ConcurrentHashMap<>();
    private final Map<String, ExecutorService> userExecutors = new ConcurrentHashMap<>();
    public void startConsumerForUser(String username, String password) {
        if (userConsumers.containsKey(username)) {
            log.info("사용자 {}의 CertifiedNotMoveConsumer 이미 실행 중", username);
            return;
        }
        
        log.info("사용자 {}의 CertifiedNotMoveConsumer 시작", username);
        
        try {
            Consumer<String, String> consumer = createConsumer(username, password);
            userConsumers.put(username, consumer);
            
            ExecutorService executor = Executors.newSingleThreadExecutor();
            userExecutors.put(username, executor);
            
            executor.submit(() -> {
                try {
                    consumer.subscribe(Collections.singletonList(topicName));
                    log.info("사용자 {}가 토픽 {} 구독 시작", username, topicName);
                    
                    while (!Thread.currentThread().isInterrupted()) {
                        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                        for (ConsumerRecord<String, String> record : records) {
                            String message = record.value();
                            log.info("의심스러운 로그인 시도 로그 수신: {}", message);
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
        Consumer<String, String> consumer = userConsumers.remove(username);
        ExecutorService executor = userExecutors.remove(username);
        
        if (consumer != null) {
            log.info("사용자 {}의 CertifiedNotMoveConsumer 중지", username);
            consumer.close();
        }
        
        if (executor != null) {
            executor.shutdown();
        }
    }

    private Consumer<String, String> createConsumer(String username, String password) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId + "-" + username);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put("security.protocol", "SASL_PLAINTEXT");
        props.put("sasl.mechanism", "SCRAM-SHA-512");
        props.put("sasl.jaas.config",
                "org.apache.kafka.common.security.scram.ScramLoginModule required " +
                        "username=\"" + username + "\" password=\"" + password + "\";");
        
        return new KafkaConsumer<>(props);
    }
    
    private void sendMessageToClients(String message) {
        // 기존 방식 (하위 호환성)
        Map<String, SseEmitter> emitters = sseService.getCertifiedNotMoveEmitters();
        emitters.forEach((clientId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("auth_suspicious")
                        .data(message, MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                log.error("SSE 전송 오류: {}", e.getMessage());
                emitters.remove(clientId);
            }
        });
        
        // 사용자별 SSE 연결에도 전송
        Map<String, Map<String, SseEmitter>> allUserEmitters = sseService.getAllUserCertifiedNotMoveEmitters();
        allUserEmitters.forEach((username, userEmitters) -> {
            // ConcurrentModificationException 방지를 위해 복사본 생성
            Map<String, SseEmitter> emittersCopy = new ConcurrentHashMap<>(userEmitters);
            emittersCopy.forEach((clientId, emitter) -> {
                try {
                    emitter.send(SseEmitter.event()
                            .name("auth_suspicious")
                            .data(message, MediaType.APPLICATION_JSON));
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