package com.finalproject.springbackend.util;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 시간대 처리 유틸리티 클래스
 * 한국 시간(KST) 입력을 올바르게 처리하기 위한 유틸리티
 */
public class TimeZoneUtil {
    
    // 한국 시간대 (UTC+9)
    public static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");
    public static final ZoneOffset KST_OFFSET = ZoneOffset.of("+09:00");
    
    /**
     * 사용자 입력 시간을 한국 시간대로 해석하여 OffsetDateTime으로 변환
     * 프론트엔드에서 "2025-08-19T08:03" 형태로 입력된 시간을
     * 한국 시간으로 해석하여 "2025-08-19T08:03+09:00"로 변환
     * 
     * @param inputDateTime 사용자 입력 OffsetDateTime (UTC로 변환된 상태)
     * @return 한국 시간대로 해석된 OffsetDateTime
     */
    public static OffsetDateTime interpretAsKST(OffsetDateTime inputDateTime) {
        if (inputDateTime == null) {
            return null;
        }
        
        // 입력된 시간의 LocalDateTime 부분을 한국 시간대로 해석
        return inputDateTime.toLocalDateTime().atOffset(KST_OFFSET);
    }
    
    /**
     * 현재 한국 시간을 반환
     * 
     * @return 현재 한국 시간 OffsetDateTime
     */
    public static OffsetDateTime nowKST() {
        return OffsetDateTime.now(KST_ZONE);
    }
    
    /**
     * UTC 시간을 한국 시간으로 변환
     * 
     * @param utcDateTime UTC 시간
     * @return 한국 시간으로 변환된 OffsetDateTime
     */
    public static OffsetDateTime utcToKST(OffsetDateTime utcDateTime) {
        if (utcDateTime == null) {
            return null;
        }
        return utcDateTime.atZoneSameInstant(KST_ZONE).toOffsetDateTime();
    }
    
    /**
     * 한국 시간을 UTC로 변환
     * 
     * @param kstDateTime 한국 시간
     * @return UTC로 변환된 OffsetDateTime
     */
    public static OffsetDateTime kstToUTC(OffsetDateTime kstDateTime) {
        if (kstDateTime == null) {
            return null;
        }
        return kstDateTime.atZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime();
    }
    
    /**
     * 시간대 정보가 없는 문자열을 한국 시간으로 파싱
     * "2025-08-19T08:03" -> "2025-08-19T08:03+09:00"
     * 
     * @param dateTimeString 시간대 정보가 없는 날짜시간 문자열
     * @return 한국 시간대가 적용된 OffsetDateTime
     */
    public static OffsetDateTime parseAsKST(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }
        
        // ISO_LOCAL_DATE_TIME 형식으로 파싱 후 한국 시간대 적용
        return OffsetDateTime.parse(dateTimeString + "+09:00");
    }
    
    /**
     * 한국 시간을 프론트엔드용 문자열로 포맷팅
     * "2025-02-03 오전 04:09:02" 형태로 변환
     * 
     * @param dateTime 변환할 시간 (KST 기준)
     * @return 포맷된 한국어 시간 문자열
     */
    public static String formatToKoreanString(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        
        // 한국 시간대로 변환
        OffsetDateTime kstDateTime = dateTime.atZoneSameInstant(KST_ZONE).toOffsetDateTime();
        
        // 한국어 오전/오후 포맷터
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd a hh:mm:ss", java.util.Locale.KOREAN);
        return kstDateTime.format(formatter);
    }
    
    /**
     * 프론트엔드에서 받은 ISO 문자열을 한국 시간으로 해석
     * "2025-08-18T23:00:00.000Z" -> 한국 시간 기준으로 해석하여 OffsetDateTime 반환
     * 
     * @param isoString ISO 형태의 시간 문자열
     * @return 한국 시간으로 해석된 OffsetDateTime
     */
    public static OffsetDateTime parseFromFrontend(String isoString) {
        if (isoString == null || isoString.trim().isEmpty()) {
            return null;
        }
        
        try {
            // ISO 문자열을 파싱하여 UTC 시간으로 받음
            OffsetDateTime utcTime = OffsetDateTime.parse(isoString);
            
            // UTC 시간을 한국 시간으로 변환
            return utcTime.atZoneSameInstant(KST_ZONE).toOffsetDateTime();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format: " + isoString, e);
        }
    }
    
    /**
     * 디버깅을 위한 시간 정보 출력
     * 
     * @param label 라벨
     * @param dateTime 시간
     * @return 포맷된 문자열
     */
    public static String formatForDebug(String label, OffsetDateTime dateTime) {
        if (dateTime == null) {
            return label + ": null";
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss XXX");
        return String.format("%s: %s (UTC: %s)", 
            label, 
            dateTime.format(formatter), 
            dateTime.toInstant().toString()
        );
    }
}
