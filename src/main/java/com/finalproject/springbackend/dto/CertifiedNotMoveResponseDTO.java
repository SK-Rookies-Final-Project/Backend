package com.finalproject.springbackend.dto;

import com.finalproject.springbackend.db.entity.CertifiedNotMove;
import com.finalproject.springbackend.util.TimeZoneUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertifiedNotMoveResponseDTO {
    
    private String id;
    private String clientIp;
    private String alertTimeKST;  // "2025-02-03 오전 04:09:02" 형태
    private String alertType;
    private String description;
    private Long failureCount;
    
    /**
     * Entity를 DTO로 변환하는 정적 팩토리 메서드
     */
    public static CertifiedNotMoveResponseDTO from(CertifiedNotMove entity) {
        if (entity == null) {
            return null;
        }
        
        return CertifiedNotMoveResponseDTO.builder()
                .id(entity.getId())
                .clientIp(entity.getClientIp())
                .alertTimeKST(TimeZoneUtil.formatToKoreanString(entity.getAlertTimeKST()))
                .alertType(entity.getAlertType())
                .description(entity.getDescription())
                .failureCount(entity.getFailureCount())
                .build();
    }
}
