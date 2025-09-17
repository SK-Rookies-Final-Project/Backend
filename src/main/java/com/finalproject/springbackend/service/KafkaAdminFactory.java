package com.finalproject.springbackend.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Slf4j
@Component
public class KafkaAdminFactory {

    @Value("${OHIO_KAFKA_BOOTSTRAP_SERVERS}")
    private String bootstrap;

    public AdminClient createAdminClient(String username, String password) {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        props.put("security.protocol", "SASL_PLAINTEXT");
        props.put("sasl.mechanism", "SCRAM-SHA-512");
        props.put("sasl.jaas.config",
                "org.apache.kafka.common.security.scram.ScramLoginModule required " +
                        "username=\"" + username + "\" password=\"" + password + "\";");
        
        // 타임아웃 설정 추가
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 10000);
        props.put(AdminClientConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, 30000);
        props.put(AdminClientConfig.RETRIES_CONFIG, 3);
        props.put(AdminClientConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
        
        log.info("Creating AdminClient to {} for user {} with timeout settings", bootstrap, username);
        return AdminClient.create(props);
    }
}
