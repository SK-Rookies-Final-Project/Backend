package com.finalproject.springbackend.dto;

public enum Permission {
    // 모든 API 접근 권한
    ADMIN("admin", "모든 API 접근 권한"),
    
    // 인증/인가 모니터링 권한
    MANAGER("manager", "시스템 관리 권한"),
    MONITOR("monitor", "인증/인가 모니터링 권한");
    
    private final String code;
    private final String description;

    Permission(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
