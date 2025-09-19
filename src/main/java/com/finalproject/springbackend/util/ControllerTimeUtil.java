package com.finalproject.springbackend.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 컨트롤러에서 시간 파라미터 처리를 위한 유틸리티 클래스
 */
@Slf4j
public class ControllerTimeUtil {
    
    /**
     * 시간 파라미터를 파싱하고 서비스 호출 후 DTO 변환하는 헬퍼 메서드
     * 
     * @param start 시작 시간 문자열
     * @param end 종료 시간 문자열 (nullable)
     * @param serviceCall 서비스 호출 함수
     * @param dtoConverter DTO 변환 함수
     * @return ResponseEntity
     */
    public static <T, R> ResponseEntity<List<R>> handleTimeRangeQuery(
            String start, 
            String end,
            BiFunction<OffsetDateTime, OffsetDateTime, List<T>> serviceCall,
            Function<T, R> dtoConverter) {
        
        try {
            OffsetDateTime startTime = TimeZoneUtil.parseFromFrontend(start);
            OffsetDateTime endTime = end != null ? TimeZoneUtil.parseFromFrontend(end) : null;
            
            log.debug("🕐 Time range query: {} to {}", 
                     TimeZoneUtil.formatForDebug("start", startTime),
                     TimeZoneUtil.formatForDebug("end", endTime));
            
            List<T> entities = serviceCall.apply(startTime, endTime);
            List<R> responseDTOs = entities.stream()
                    .map(dtoConverter)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(responseDTOs);
        } catch (IllegalArgumentException e) {
            log.error("❌ Invalid date format: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 시간 파라미터를 파싱하고 카운트 서비스 호출하는 헬퍼 메서드
     * 
     * @param start 시작 시간 문자열
     * @param end 종료 시간 문자열 (nullable)
     * @param serviceCall 서비스 호출 함수
     * @return ResponseEntity
     */
    public static ResponseEntity<Long> handleTimeRangeCountQuery(
            String start, 
            String end,
            BiFunction<OffsetDateTime, OffsetDateTime, Long> serviceCall) {
        
        try {
            OffsetDateTime startTime = TimeZoneUtil.parseFromFrontend(start);
            OffsetDateTime endTime = end != null ? TimeZoneUtil.parseFromFrontend(end) : null;
            
            Long count = serviceCall.apply(startTime, endTime);
            return ResponseEntity.ok(count);
        } catch (IllegalArgumentException e) {
            log.error("❌ Invalid date format: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
