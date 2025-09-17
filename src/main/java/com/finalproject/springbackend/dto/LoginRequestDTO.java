package com.finalproject.springbackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {
    
    @NotBlank(message = "사용자 ID는 필수입니다")
    private String username;
    
    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;
    
    // 수동 getter 메서드 (Lombok이 작동하지 않을 경우를 대비)
    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }
}
