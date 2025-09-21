package com.finalproject.springbackend.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Slf4j
@Component
public class KafkaAdminFactory {

    @Value("${OHIO_KAFKA_BOOTSTRAP_SERVERS}")
    private String bootstrap;
    
    @jakarta.annotation.PostConstruct
    public void init() {
        log.info("KafkaAdminFactory 초기화 완료 - Bootstrap: {}", bootstrap);
    }
    
    /**
     * 사용자 계정을 사용하여 AdminClient 생성 (SCRAM 인증 검증용)
     * @param username Kafka SCRAM 사용자명
     * @param password Kafka SCRAM 비밀번호
     * @return AdminClient 인스턴스
     */
    public AdminClient createAdminClient(String username, String password) {
        Properties props = createBaseProperties(username, password);
        
        // AdminClient 전용 설정
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 10000);
        props.put(AdminClientConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, 30000);
        props.put(AdminClientConfig.RETRIES_CONFIG, 3);
        props.put(AdminClientConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
        
        log.debug("Creating Kafka AdminClient for user: {}", username);
        
        try {
            AdminClient client = AdminClient.create(props);
            log.debug("✅ Kafka AdminClient 생성 성공 for user: {}", username);
            return client;
        } catch (Exception e) {
            log.error("❌ Failed to create Kafka AdminClient for user {}: {}", username, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 사용자 계정을 사용하여 Consumer 생성
     * @param username Kafka SCRAM 사용자명
     * @param password Kafka SCRAM 비밀번호
     * @param consumerGroupId Consumer Group ID
     * @return Consumer 인스턴스
     */
    public Consumer<String, byte[]> createConsumer(String username, String password, String consumerGroupId) {
        Properties props = createBaseProperties(username, password);
        
        // Consumer 전용 설정
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        
        log.debug("Creating Kafka Consumer for user: {} with group: {}", username, consumerGroupId);
        
        try {
            Consumer<String, byte[]> consumer = new KafkaConsumer<>(props);
            log.debug("✅ Kafka Consumer 생성 성공 for user: {}", username);
            return consumer;
        } catch (Exception e) {
            log.error("❌ Failed to create Kafka Consumer for user {}: {}", username, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Kafka 연결을 위한 기본 Properties 생성
     * @param username Kafka SCRAM 사용자명
     * @param password Kafka SCRAM 비밀번호
     * @return 기본 Properties
     */
    private Properties createBaseProperties(String username, String password) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        props.put("security.protocol", "SASL_PLAINTEXT");
        props.put("sasl.mechanism", "SCRAM-SHA-512");
        props.put("sasl.jaas.config",
                "org.apache.kafka.common.security.scram.ScramLoginModule required " +
                        "username=\"" + username + "\" password=\"" + password + "\";");
        return props;
    }
}
