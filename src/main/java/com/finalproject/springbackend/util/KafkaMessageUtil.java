package com.finalproject.springbackend.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
public class KafkaMessageUtil {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter kstFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSXXX")
            .withZone(ZoneId.of("Asia/Seoul"));
    
    /**
     * Kafka 메시지를 파싱하여 SSE 전송용 JSON으로 변환
     * @param rawMessage 원본 메시지 (JSON 문자열)
     * @param topicName 토픽 이름 (로깅용)
     * @return 파싱된 JSON 메시지
     */
    public static String parseMessageToJson(String rawMessage, String topicName) {
        try {
            // rawMessage가 JSON 문자열인지 확인하고 파싱
            if (rawMessage.trim().startsWith("{") && rawMessage.trim().endsWith("}")) {
                JsonNode originalMessage = objectMapper.readTree(rawMessage);
                
                // 특정 토픽에 대해서는 메시지 형식을 변환
                String convertedMessage = convertToAlertFormat(originalMessage, topicName);
                
                log.debug("✅ {} 토픽 메시지 파싱 및 변환 성공", topicName);
                return convertedMessage;
            } else {
                // JSON이 아닌 경우 기본 형식으로 래핑
                log.warn("⚠️ {} 토픽에서 JSON이 아닌 메시지 수신: {}", topicName, rawMessage);
                return "{\"message\":\"" + rawMessage.replace("\"", "\\\"") + 
                       "\",\"timestamp\":" + System.currentTimeMillis() + 
                       ",\"error\":\"비정형 데이터\"}";
            }
        } catch (Exception e) {
            log.error("❌ {} 토픽 JSON 파싱 실패: {}", topicName, e.getMessage());
            // 파싱 실패 시 안전한 형식으로 반환
            return "{\"rawMessage\":\"" + rawMessage.replace("\"", "\\\"") + 
                   "\",\"timestamp\":" + System.currentTimeMillis() + 
                   ",\"error\":\"JSON 파싱 실패\"}";
        }
    }
    
    /**
     * 원본 메시지를 알림 형식으로 변환
     */
    private static String convertToAlertFormat(JsonNode originalMessage, String topicName) {
        try {
            String alertType;
            String description;
            int failureCount = 1;
            
            // 토픽별 알림 타입 설정
            switch (topicName) {
                case "certified-2time":
                    alertType = "LOGIN_FAILURE";
                    description = "2회 연속 인증 실패";
                    failureCount = 2;
                    break;
                case "certified-notMove":
                    alertType = "LOCATION_CHANGE";
                    description = "비정상적인 위치에서의 접근 시도";
                    failureCount = 1;
                    break;
                default:
                    // 기본값 - 원본 메시지 그대로 반환
                    return originalMessage.toString();
            }
            
            // 알림 메시지 형식으로 변환
            String alertMessage = String.format(
                "{\n" +
                "  \"id\": \"%s\",\n" +
                "  \"alertTimeKST\": \"%s\",\n" +
                "  \"alertType\": \"%s\",\n" +
                "  \"clientIp\": \"%s\",\n" +
                "  \"description\": \"%s\",\n" +
                "  \"failureCount\": %d\n" +
                "}",
                UUID.randomUUID().toString(),
                kstFormatter.format(Instant.now()),
                alertType,
                getClientIp(originalMessage),
                description,
                failureCount
            );
            
            return alertMessage;
            
        } catch (Exception e) {
            log.error("❌ 메시지 형식 변환 실패: {}", e.getMessage());
            return originalMessage.toString();
        }
    }
    
    /**
     * 원본 메시지에서 클라이언트 IP 추출
     */
    private static String getClientIp(JsonNode originalMessage) {
        // 다양한 필드명으로 IP 주소 시도
        String[] ipFields = {"clientIp", "client_ip", "sourceIp", "source_ip", "ip"};
        
        for (String field : ipFields) {
            JsonNode ipNode = originalMessage.get(field);
            if (ipNode != null && !ipNode.isNull()) {
                return ipNode.asText();
            }
        }
        
        // IP를 찾지 못한 경우 기본값 반환
        return "127.0.0.1";
    }
}
