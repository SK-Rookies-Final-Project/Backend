package com.finalproject.springbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigInfoDTO {
    private String resourceType;
    private String resourceName;
    private Map<String, String> configs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfigUpdateRequest {
        private String resourceType;
        private String resourceName;
        private Map<String, String> configs;
    }
}
