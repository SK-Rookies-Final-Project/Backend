package com.finalproject.springbackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {
    
    private String token;
    private String username;
    private String message;
    private boolean success;
    private String[] allowedTopics;
    private String permission;
    
    // 수동 getter 메서드 (Lombok이 작동하지 않을 경우를 대비)
    public boolean isSuccess() {
        return success;
    }
    
    // Builder 패턴을 위한 정적 메서드
    public static LoginResponseDTOBuilder builder() {
        return new LoginResponseDTOBuilder();
    }
    
    public static class LoginResponseDTOBuilder {
        private String token;
        private String username;
        private String message;
        private boolean success;
        private String[] allowedTopics;
        private String permission;
        
        public LoginResponseDTOBuilder token(String token) {
            this.token = token;
            return this;
        }
        
        public LoginResponseDTOBuilder username(String username) {
            this.username = username;
            return this;
        }
        
        public LoginResponseDTOBuilder permission(String permission) {
            this.permission = permission;
            return this;
        }
        
        public LoginResponseDTOBuilder message(String message) {
            this.message = message;
            return this;
        }
        
        public LoginResponseDTOBuilder success(boolean success) {
            this.success = success;
            return this;
        }
        
        public LoginResponseDTOBuilder allowedTopics(String[] allowedTopics) {
            this.allowedTopics = allowedTopics;
            return this;
        }
        
        public LoginResponseDTO build() {
            LoginResponseDTO dto = new LoginResponseDTO();
            dto.token = token;
            dto.username = username;
            dto.message = message;
            dto.success = success;
            dto.allowedTopics = allowedTopics;
            dto.permission = permission;
            return dto;
        }
    }
}
