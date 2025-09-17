package com.finalproject.springbackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfo {
    
    private String username;
    private String password;
    private String region;
    private String[] allowedTopics;
    
    // 수동 getter 메서드 (Lombok이 작동하지 않을 경우를 대비)
    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public String getRegion() {
        return region;
    }
    
    public String[] getAllowedTopics() {
        return allowedTopics;
    }
    
    // Builder 패턴을 위한 정적 메서드
    public static UserInfoBuilder builder() {
        return new UserInfoBuilder();
    }
    
    public static class UserInfoBuilder {
        private String username;
        private String password;
        private String region;
        private String[] allowedTopics;
        
        public UserInfoBuilder username(String username) {
            this.username = username;
            return this;
        }
        
        public UserInfoBuilder password(String password) {
            this.password = password;
            return this;
        }
        
        public UserInfoBuilder region(String region) {
            this.region = region;
            return this;
        }
        
        public UserInfoBuilder allowedTopics(String[] allowedTopics) {
            this.allowedTopics = allowedTopics;
            return this;
        }
        
        public UserInfo build() {
            UserInfo userInfo = new UserInfo();
            userInfo.username = username;
            userInfo.password = password;
            userInfo.region = region;
            userInfo.allowedTopics = allowedTopics;
            return userInfo;
        }
    }
}
