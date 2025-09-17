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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
    public ResponseEntity<SseEmitter> getAuthFailure(Authentication authentication) {
        try {
            String username = authentication.getName();
            log.info("사용자 {}가 auth_failure API 호출 (certified-2time 토픽)", username);
            
            SseEmitter emitter = sseService.createUserCertified2TimeStream(username);
            if (emitter == null) {
                log.error("SSE Emitter 생성 실패: 사용자 {}", username);
                return ResponseEntity.badRequest().build();
            }
            
            return ResponseEntity.ok(emitter);
        } catch (Exception e) {
            log.error("auth_failure API 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 2. 의심스러운 로그인 시도 스트리밍 (권한 기반) - certified-notMove 토픽
    @RequirePermission({Permission.MONITOR})
    @GetMapping(value = "/auth_suspicious", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> getAuthSuspicious(Authentication authentication) {
        try {
            String username = authentication.getName();
            log.info("사용자 {}가 auth_suspicious API 호출 (certified-notMove 토픽)", username);
            
            SseEmitter emitter = sseService.createUserCertifiedNotMoveStream(username);
            if (emitter == null) {
                log.error("SSE Emitter 생성 실패: 사용자 {}", username);
                return ResponseEntity.badRequest().build();
            }
            
            return ResponseEntity.ok(emitter);
        } catch (Exception e) {
            log.error("auth_suspicious API 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 3. 시스템 권한 부족 로그 스트리밍 (권한 기반) - system-level-false 토픽
    @RequirePermission({Permission.MANAGER})
    @GetMapping(value = "/auth_system", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> getAuthSystem(Authentication authentication) {
        try {
            String username = authentication.getName();
            log.info("사용자 {}가 auth_system API 호출 (system-level-false 토픽)", username);
            
            SseEmitter emitter = sseService.createUserSystemLevelFalseStream(username);
            if (emitter == null) {
                log.error("SSE Emitter 생성 실패: 사용자 {}", username);
                return ResponseEntity.badRequest().build();
            }
            
            return ResponseEntity.ok(emitter);
        } catch (Exception e) {
            log.error("auth_system API 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 4. 리소스 권한 부족 로그 스트리밍 (권한 기반) - resource-level-false 토픽
    @RequirePermission({Permission.MONITOR})
    @GetMapping(value = "/auth_resource", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> getAuthResource(Authentication authentication) {
        String username = authentication.getName();
        log.info("사용자 {}가 auth_resource API 호출 (resource-level-false 토픽)", username);
        
        try {
            SseEmitter emitter = sseService.createUserResourceLevelFalseStream(username);
            if (emitter == null) {
                log.error("SSE Emitter 생성 실패: 사용자 {}", username);
                return ResponseEntity.badRequest().build();
            }
            
            log.info("SSE 연결 응답 반환: 사용자 {}, 토픽: resource-level-false", username);
            return ResponseEntity.ok(emitter);
        } catch (Exception e) {
            log.error("auth_resource API 오류: 사용자 {}, 오류: {}", username, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 로그아웃 API
    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication authentication) {
        String username = authentication.getName();
        log.info("사용자 {} 로그아웃", username);
        
        // 사용자의 모든 SSE 연결 정리
        sseService.closeUserConnections(username);
        
        return ResponseEntity.ok().body("{\"message\": \"로그아웃 성공\"}");
    }
    

}