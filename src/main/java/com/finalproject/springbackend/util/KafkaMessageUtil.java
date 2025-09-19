package com.finalproject.springbackend.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KafkaMessageUtil {
    
    /**
     * Kafka 메시지를 파싱하여 SSE 전송용 JSON으로 변환
     * rawMessage의 JSON 내용만 파싱해서 반환
     * @param rawMessage 원본 메시지 (JSON 문자열)
     * @param topicName 토픽 이름 (로깅용)
     * @return 파싱된 JSON 메시지 (data 부분만)
     */
    public static String parseMessageToJson(String rawMessage, String topicName) {
        try {
            // rawMessage가 JSON 문자열인지 확인하고 파싱
            if (rawMessage.trim().startsWith("{") && rawMessage.trim().endsWith("}")) {
                // JSON 문자열을 파싱해서 그대로 반환 (data 부분만)
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                
                // JSON 유효성 검증을 위해 파싱 시도
                objectMapper.readTree(rawMessage);
                
                // 유효한 JSON이면 그대로 반환
                log.debug("✅ {} 토픽 메시지 파싱 성공", topicName);
                return rawMessage;
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
}
