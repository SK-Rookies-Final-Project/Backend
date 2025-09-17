package com.finalproject.springbackend.aspect;

import com.finalproject.springbackend.annotation.RequirePermission;
import com.finalproject.springbackend.dto.Permission;
import com.finalproject.springbackend.service.AuthService;
import com.finalproject.springbackend.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private static final Logger log = LoggerFactory.getLogger(PermissionAspect.class);
    private final PermissionService permissionService;
    private final AuthService authService;

    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        log.info("🔍 AOP 권한 체크 시작: {}", joinPoint.getSignature().getName());
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("❌ 인증되지 않은 사용자의 API 접근 시도");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\": \"인증이 필요합니다\"}");
        }

        String username = authentication.getName();
        Permission[] requiredPermissions = requirePermission.value();
        
        log.info("👤 사용자: {}, 필요한 권한: {}", username, java.util.Arrays.toString(requiredPermissions));

        // 사용자 권한 확인
        boolean hasPermission = permissionService.hasAnyPermission(username, requiredPermissions);
        
        if (!hasPermission) {
            log.warn("사용자 {}가 필요한 권한 없이 API 접근 시도: {}", username, requiredPermissions);
            
            // SSE 엔드포인트인지 확인
            if (isSseEndpoint(joinPoint)) {
                SseEmitter emitter = new SseEmitter(0L);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("{\"error\": \"접근 권한이 없습니다\", \"required_permissions\": " + 
                                  java.util.Arrays.toString(requiredPermissions) + "}"));
                } catch (Exception e) {
                    log.error("SSE 에러 메시지 전송 실패", e);
                }
                emitter.complete();
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .contentType(MediaType.TEXT_EVENT_STREAM)
                        .body(emitter);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("{\"error\": \"접근 권한이 없습니다\", \"required_permissions\": " + 
                              java.util.Arrays.toString(requiredPermissions) + "}");
            }
        }

        log.info("✅ 사용자 {}가 권한 확인 후 API 접근: {}", username, requiredPermissions);
        
        try {
            log.info("🚀 컨트롤러 메서드 실행 시작: {}", joinPoint.getSignature().getName());
            Object result = joinPoint.proceed();
            log.info("✅ 컨트롤러 메서드 실행 완료: {}", joinPoint.getSignature().getName());
            return result;
        } catch (Exception e) {
            log.error("❌ API 실행 중 오류 발생: {}", e.getMessage(), e);
            // 원래 메서드의 반환 타입을 유지하기 위해 예외를 다시 던짐
            throw e;
        }
    }
    
    private boolean isSseEndpoint(ProceedingJoinPoint joinPoint) {
        try {
            // 메서드의 반환 타입이 ResponseEntity<SseEmitter>인지 확인
            String methodName = joinPoint.getSignature().getName();
            Class<?> targetClass = joinPoint.getTarget().getClass();
            java.lang.reflect.Method method = targetClass.getMethod(methodName, 
                (Class<?>[]) joinPoint.getArgs());
            
            return method.getReturnType().equals(ResponseEntity.class) &&
                   method.getGenericReturnType().getTypeName().contains("SseEmitter");
        } catch (Exception e) {
            log.debug("SSE 엔드포인트 확인 중 오류: {}", e.getMessage());
            return false;
        }
    }
}
