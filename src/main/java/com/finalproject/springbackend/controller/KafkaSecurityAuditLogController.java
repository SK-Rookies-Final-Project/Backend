package com.finalproject.springbackend.controller;

import com.finalproject.springbackend.annotation.RequirePermission;
import com.finalproject.springbackend.dto.Permission;
import com.finalproject.springbackend.dto.UserInfo;
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
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class KafkaSecurityAuditLogController {

    private static final Logger log = LoggerFactory.getLogger(KafkaSecurityAuditLogController.class);
    private final SseService sseService;
    private final AuthService authService;

    // 1. 반복적인 로그인 시도 스트리밍 (권한 기반) - certified-2time 토픽
    @RequirePermission({Permission.MONITOR})
    @GetMapping(value = "/auth_failure", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseBodyEmitter getAuthFailure(Authentication authentication) {
        UserInfo userInfo;
        
        // 인증 정보가 있으면 사용, 없으면 기본 admin 사용자 생성
        if (authentication != null && authentication.getPrincipal() instanceof UserInfo) {
            userInfo = (UserInfo) authentication.getPrincipal();
        } else {
            userInfo = UserInfo.builder()
                    .username("admin")
                    .password("admin123")
                    .region("default")
                    .build();
            log.info("🔧 [1/4] 인증 정보 없음 - 기본 admin 사용자로 설정");
        }
        
        try {
            log.info("🔴 [1/4] 반복적인 로그인 시도 스트리밍 시작 - 사용자: {}", userInfo.getUsername());
            log.info("🌐 [프론트엔드 연결] 클라이언트에서 /api/auth/auth_failure 엔드포인트 요청");
            
            ResponseBodyEmitter emitter = sseService.createUserCertified2TimeStream(userInfo);
            if (emitter == null) {
                log.error("❌ [1/4] SSE Emitter 생성 실패: 사용자 {}", userInfo.getUsername());
                throw new RuntimeException("SSE Emitter 생성 실패");
            }
            
            log.info("✅ [1/4] 반복적인 로그인 시도 스트리밍 연결 완료 - 사용자: {}", userInfo.getUsername());
            return emitter;
        } catch (Exception e) {
            log.error("❌ [1/4] auth_failure API 오류: {}", e.getMessage(), e);
            throw new RuntimeException("auth_failure API 오류: " + e.getMessage(), e);
        }
    }

    // 2. 의심스러운 로그인 시도 스트리밍 (권한 기반) - certified-notMove 토픽
    @RequirePermission({Permission.MONITOR})
    @GetMapping(value = "/auth_suspicious", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseBodyEmitter getAuthSuspicious(Authentication authentication) {
        UserInfo userInfo;
        
        // 인증 정보가 있으면 사용, 없으면 기본 admin 사용자 생성
        if (authentication != null && authentication.getPrincipal() instanceof UserInfo) {
            userInfo = (UserInfo) authentication.getPrincipal();
        } else {
            userInfo = UserInfo.builder()
                    .username("admin")
                    .password("admin123")
                    .region("default")
                    .build();
            log.info("🔧 [2/4] 인증 정보 없음 - 기본 admin 사용자로 설정");
        }
        
        try {
            log.info("🟠 [2/4] 의심스러운 로그인 시도 스트리밍 시작 - 사용자: {}", userInfo.getUsername());
            log.info("🌐 [프론트엔드 연결] 클라이언트에서 /api/auth/auth_suspicious 엔드포인트 요청");
            
            ResponseBodyEmitter emitter = sseService.createUserCertifiedNotMoveStream(userInfo);
            if (emitter == null) {
                log.error("❌ [2/4] SSE Emitter 생성 실패: 사용자 {}", userInfo.getUsername());
                throw new RuntimeException("SSE Emitter 생성 실패");
            }
            
            log.info("✅ [2/4] 의심스러운 로그인 시도 스트리밍 연결 완료 - 사용자: {}", userInfo.getUsername());
            return emitter;
        } catch (Exception e) {
            log.error("❌ [2/4] auth_suspicious API 오류: {}", e.getMessage(), e);
            throw new RuntimeException("auth_suspicious API 오류: " + e.getMessage(), e);
        }
    }

    // 3. 시스템 권한 부족 로그 스트리밍 (권한 기반) - system-level-false 토픽
    @RequirePermission({Permission.MANAGER})
    @GetMapping(value = "/auth_system", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseBodyEmitter getAuthSystem(Authentication authentication) {
        UserInfo userInfo;
        
        // 인증 정보가 있으면 사용, 없으면 기본 admin 사용자 생성
        if (authentication != null && authentication.getPrincipal() instanceof UserInfo) {
            userInfo = (UserInfo) authentication.getPrincipal();
        } else {
            userInfo = UserInfo.builder()
                    .username("admin")
                    .password("admin123")
                    .region("default")
                    .build();
            log.info("🔧 [3/4] 인증 정보 없음 - 기본 admin 사용자로 설정");
        }
        
        try {
            log.info("🟡 [3/4] 시스템 권한 부족 로그 스트리밍 시작 - 사용자: {}", userInfo.getUsername());
            log.info("🌐 [프론트엔드 연결] 클라이언트에서 /api/auth/auth_system 엔드포인트 요청");
            
            ResponseBodyEmitter emitter = sseService.createUserSystemLevelFalseStream(userInfo);
            if (emitter == null) {
                log.error("❌ [3/4] SSE Emitter 생성 실패: 사용자 {}", userInfo.getUsername());
                throw new RuntimeException("SSE Emitter 생성 실패");
            }
            
            log.info("✅ [3/4] 시스템 권한 부족 로그 스트리밍 연결 완료 - 사용자: {}", userInfo.getUsername());
            return emitter;
        } catch (Exception e) {
            log.error("❌ [3/4] auth_system API 오류: {}", e.getMessage(), e);
            throw new RuntimeException("auth_system API 오류: " + e.getMessage(), e);
        }
    }

    // 4. 리소스 권한 부족 로그 스트리밍 (권한 기반) - resource-level-false 토픽
    @RequirePermission({Permission.MONITOR})
    @GetMapping(value = "/auth_resource", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseBodyEmitter getAuthResource(Authentication authentication, 
                                             @RequestParam(required = false) String token) {
        UserInfo userInfo;
        
        // 인증 정보가 있으면 사용, 없으면 기본 admin 사용자 생성
        if (authentication != null && authentication.getPrincipal() instanceof UserInfo) {
            userInfo = (UserInfo) authentication.getPrincipal();
        } else {
            // 기본 admin 사용자 정보 생성 (프론트엔드 테스트용)
            userInfo = UserInfo.builder()
                    .username("admin")
                    .password("admin123")
                    .region("default")
                    .build();
            log.info("🔧 [4/4] 인증 정보 없음 - 기본 admin 사용자로 설정");
        }
        
        try {
            log.info("🔵 [4/4] 리소스 권한 부족 로그 스트리밍 시작 - 사용자: {}", userInfo.getUsername());
            log.info("🌐 [프론트엔드 연결] 클라이언트에서 /api/auth/auth_resource 엔드포인트 요청");
            
            ResponseBodyEmitter emitter = sseService.createUserResourceLevelFalseStream(userInfo);
            if (emitter == null) {
                log.error("❌ [4/4] SSE Emitter 생성 실패: 사용자 {}", userInfo.getUsername());
                throw new RuntimeException("SSE Emitter 생성 실패");
            }
            
            log.info("✅ [4/4] 리소스 권한 부족 로그 스트리밍 연결 완료 - 사용자: {}", userInfo.getUsername());
            return emitter;
        } catch (Exception e) {
            log.error("❌ [4/4] auth_resource API 오류: 사용자 {}, 오류: {}", userInfo.getUsername(), e.getMessage(), e);
            throw new RuntimeException("auth_resource API 오류: " + e.getMessage(), e);
        }
    }

    // SSE 연결 해제 API
    @PostMapping("/disconnect")
    public ResponseEntity<?> disconnect(Authentication authentication, @RequestParam(required = false) String clientId) {
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