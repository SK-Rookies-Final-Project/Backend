package com.finalproject.springbackend.dto;

import com.finalproject.springbackend.db.entity.ResourceLevelFalse;
import com.finalproject.springbackend.util.TimeZoneUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceLevelFalseResponseDTO {
    
    private String id;
    private String eventTimeKST;      // "2025-02-03 오전 04:09:02" 형태
    private String processTimeKST;    // "2025-02-03 오전 04:09:02" 형태
    private String principal;
    private String clientIp;
    private String methodName;
    private boolean granted;
    private String resourceType;
    private String resourceName;
    private String operation;
    
    /**
     * Entity를 DTO로 변환하는 정적 팩토리 메서드
     */
    public static ResourceLevelFalseResponseDTO from(ResourceLevelFalse entity) {
        if (entity == null) {
            return null;
        }
        
        return ResourceLevelFalseResponseDTO.builder()
                .id(entity.getId())
                .eventTimeKST(TimeZoneUtil.formatToKoreanString(entity.getEventTimeKST()))
                .processTimeKST(TimeZoneUtil.formatToKoreanString(entity.getProcessTimeKST()))
                .principal(entity.getPrincipal())
                .clientIp(entity.getClientIp())
                .methodName(entity.getMethodName())
                .granted(entity.isGranted())
                .resourceType(entity.getResourceType())
                .resourceName(entity.getResourceName())
                .operation(entity.getOperation())
                .build();
    }
}
