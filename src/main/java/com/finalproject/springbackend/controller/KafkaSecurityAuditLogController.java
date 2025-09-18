package com.finalproject.springbackend.controller;

import com.finalproject.springbackend.annotation.RequirePermission;
import com.finalproject.springbackend.dto.Permission;
import com.finalproject.springbackend.service.SseService;
import com.finalproject.springbackend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

@RestController
@RequestMapping("/api/kafka")
@RequiredArgsConstructor
public class KafkaSecurityAuditLogController {

    private static final Logger log = LoggerFactory.getLogger(KafkaSecurityAuditLogController.class);
    private final SseService sseService;
    private final AuthService authService;

    // 1. 반복적인 로그인 시도 스트리밍 (권한 기반) - certified-2time 토픽
    @RequirePermission({Permission.MONITOR})
    @GetMapping(value = "/auth_failure", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseBodyEmitter getAuthFailure(Authentication authentication) {
        try {
            String username = authentication.getName();
            // auth_failure API 호출
            
            ResponseBodyEmitter emitter = sseService.createUserCertified2TimeStream(username);
            if (emitter == null) {
                log.error("SSE Emitter 생성 실패: 사용자 {}", username);
                throw new RuntimeException("SSE Emitter 생성 실패");
            }
            
            return emitter;
        } catch (Exception e) {
            log.error("auth_failure API 오류: {}", e.getMessage(), e);
            throw new RuntimeException("auth_failure API 오류: " + e.getMessage(), e);
        }
    }

    // 2. 의심스러운 로그인 시도 스트리밍 (권한 기반) - certified-notMove 토픽
    @RequirePermission({Permission.MONITOR})
    @GetMapping(value = "/auth_suspicious", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseBodyEmitter getAuthSuspicious(Authentication authentication) {
        try {
            String username = authentication.getName();
            // auth_suspicious API 호출
            
            ResponseBodyEmitter emitter = sseService.createUserCertifiedNotMoveStream(username);
            if (emitter == null) {
                log.error("SSE Emitter 생성 실패: 사용자 {}", username);
                throw new RuntimeException("SSE Emitter 생성 실패");
            }
            
            return emitter;
        } catch (Exception e) {
            log.error("auth_suspicious API 오류: {}", e.getMessage(), e);
            throw new RuntimeException("auth_suspicious API 오류: " + e.getMessage(), e);
        }
    }

    // 3. 시스템 권한 부족 로그 스트리밍 (권한 기반) - system-level-false 토픽
    @RequirePermission({Permission.MANAGER})
    @GetMapping(value = "/auth_system", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseBodyEmitter getAuthSystem(Authentication authentication) {
        try {
            String username = authentication.getName();
            // auth_system API 호출
            
            ResponseBodyEmitter emitter = sseService.createUserSystemLevelFalseStream(username);
            if (emitter == null) {
                log.error("SSE Emitter 생성 실패: 사용자 {}", username);
                throw new RuntimeException("SSE Emitter 생성 실패");
            }
            
            
            return emitter;
        } catch (Exception e) {
            log.error("auth_system API 오류: {}", e.getMessage(), e);
            throw new RuntimeException("auth_system API 오류: " + e.getMessage(), e);
        }
    }

    // 4. 리소스 권한 부족 로그 스트리밍 (권한 기반) - resource-level-false 토픽
    @RequirePermission({Permission.MONITOR})
    @GetMapping(value = "/auth_resource", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseBodyEmitter getAuthResource(Authentication authentication) {
        String username = authentication.getName();
        // auth_resource API 호출
        
        try {
            ResponseBodyEmitter emitter = sseService.createUserResourceLevelFalseStream(username);
            if (emitter == null) {
                log.error("SSE Emitter 생성 실패: 사용자 {}", username);
                throw new RuntimeException("SSE Emitter 생성 실패");
            }
            
            return emitter;
        } catch (Exception e) {
            log.error("auth_resource API 오류: 사용자 {}, 오류: {}", username, e.getMessage(), e);
            throw new RuntimeException("auth_resource API 오류: " + e.getMessage(), e);
        }
    }

    // 로그아웃 API
    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication authentication, @RequestParam(required = false) String clientId) {
        String username = authentication.getName();
        // 사용자 로그아웃
        
        if (clientId != null && !clientId.trim().isEmpty()) {
            // 특정 클라이언트만 연결 해제
            sseService.closeClientConnection(username, clientId);
            // 클라이언트 연결 해제 완료
        } else {
            // 모든 SSE 연결 정리 (기존 방식)
            sseService.closeUserConnections(username);
            // 사용자 모든 연결 정리 완료
        }
        
        return ResponseEntity.ok().body("{\"message\": \"로그아웃 성공\"}");
    }
    

}