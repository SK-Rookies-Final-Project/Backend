package com.finalproject.springbackend.controller;

import com.finalproject.springbackend.dto.LoginRequestDTO;
import com.finalproject.springbackend.dto.LoginResponseDTO;
import com.finalproject.springbackend.service.AuthService;
import com.finalproject.springbackend.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;
    private final PermissionService permissionService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        log.info("로그인 시도: {}", loginRequest.getUsername());

        LoginResponseDTO response = authService.authenticate(loginRequest);

        if (response.isSuccess()) {
            log.info("로그인 성공: {}", loginRequest.getUsername());
            return ResponseEntity.ok(response);
        } else {
            log.warn("로그인 실패: {}", loginRequest.getUsername());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader,
                                    Authentication authentication) {
        try {
            String username = authentication != null ? authentication.getName() : "알 수 없음";
            log.info("로그아웃 시도: {}", username);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                // 토큰 무효화 처리
                authService.revokeToken(token);
                log.info("로그아웃 성공: {}", username);

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "로그아웃되었습니다."
                ));
            } else {
                log.warn("로그아웃 실패: 유효하지 않은 토큰 헤더");
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "유효하지 않은 인증 헤더입니다."
                ));
            }
        } catch (Exception e) {
            log.error("로그아웃 처리 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "로그아웃 처리 중 오류가 발생했습니다."
            ));
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            boolean isValid = authService.validateToken(token);

            if (isValid) {
                String username = authService.getUsernameFromToken(token);
                return ResponseEntity.ok().body("{\"valid\": true, \"username\": \"" + username + "\"}");
            }
        }

        return ResponseEntity.badRequest().body("{\"valid\": false}");
    }

    @GetMapping("/permissions")
    public ResponseEntity<?> getUserPermissions(Authentication authentication) {
        String username = authentication.getName();
        List<String> permissions = permissionService.getPermissionCodes(username);
        String userPermission = permissionService.getUserPermission(username) != null ?
                permissionService.getUserPermission(username).getDescription() : "권한 없음";

        return ResponseEntity.ok(Map.of(
                "username", username,
                "permission", userPermission,
                "permissions", permissions
        ));
    }
}