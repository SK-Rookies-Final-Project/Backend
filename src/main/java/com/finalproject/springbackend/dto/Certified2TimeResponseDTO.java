package com.finalproject.springbackend.dto;

import com.finalproject.springbackend.db.entity.Certified2Time;
import com.finalproject.springbackend.util.TimeZoneUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Certified2TimeResponseDTO {
    
    private String id;
    private String clientIp;
    private String alertTimeKST;  // "2025-02-03 오전 04:09:02" 형태
    private String alertType;
    private String description;
    private Long failureCount;
    
    /**
     * Entity를 DTO로 변환하는 정적 팩토리 메서드
     */
    public static Certified2TimeResponseDTO from(Certified2Time entity) {
        if (entity == null) {
            return null;
        }
        
        return Certified2TimeResponseDTO.builder()
                .id(entity.getId())
                .clientIp(entity.getClientIp())
                .alertTimeKST(TimeZoneUtil.formatToKoreanString(entity.getAlertTimeKST()))
                .alertType(entity.getAlertType())
                .description(entity.getDescription())
                .failureCount(entity.getFailureCount())
                .build();
    }
}
