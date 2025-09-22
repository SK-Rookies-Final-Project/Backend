package com.finalproject.springbackend.aspect;

import com.finalproject.springbackend.annotation.RequirePermission;
import com.finalproject.springbackend.dto.Permission;
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

import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Aspect
@Component
public class PermissionAspect {

    private static final Logger log = LoggerFactory.getLogger(PermissionAspect.class);

    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        // AOP 권한 체크 시작
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("❌ 인증되지 않은 사용자의 API 접근 시도");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\": \"인증이 필요합니다\"}");
        }

        String username = authentication.getName();
        Permission[] requiredPermissions = requirePermission.value();
        
        // 사용자 권한 확인

        // Spring Security 권한 확인
        boolean hasPermission = checkSpringSecurityPermissions(authentication, requiredPermissions);
        
        if (!hasPermission) {
            log.warn("❌ 권한 부족 - 사용자: {}, 필요한 권한: {}, 현재 권한: {}", 
                username, java.util.Arrays.toString(requiredPermissions), 
                authentication.getAuthorities());
            
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

        // 사용자 권한 확인 후 API 접근
        
        try {
            // 컨트롤러 메서드 실행 시작
            Object result = joinPoint.proceed();
            // 컨트롤러 메서드 실행 완료
            return result;
        } catch (Exception e) {
            log.error("❌ API 실행 중 오류 발생: {}", e.getMessage(), e);
            // 원래 메서드의 반환 타입을 유지하기 위해 예외를 다시 던짐
            throw e;
        }
    }
    
    private boolean checkSpringSecurityPermissions(Authentication authentication, Permission[] requiredPermissions) {
        // Spring Security의 authorities에서 권한 확인
        Collection<? extends org.springframework.security.core.GrantedAuthority> authorities = 
            authentication.getAuthorities();
        
        // 현재 사용자 권한 로그 출력
        List<String> userAuthorities = authorities.stream().map(auth -> auth.getAuthority()).toList();
        log.info("현재 사용자 권한: {}", userAuthorities);
        log.info("필요한 권한: {}", java.util.Arrays.toString(requiredPermissions));
        
        for (Permission requiredPermission : requiredPermissions) {
            String requiredRole = "ROLE_" + requiredPermission.name();
            
            // ADMIN 권한이 있으면 모든 권한 허용
            if (authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
                log.info("✅ ADMIN 권한으로 모든 접근 허용");
                return true;
            }
            
            // MANAGER 권한이 있으면 MONITOR 권한도 허용
            if (requiredPermission == Permission.MONITOR && 
                authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_MANAGER"))) {
                log.info("✅ MANAGER 권한으로 MONITOR 접근 허용");
                return true;
            }
            
            // 정확한 권한 매칭
            if (authorities.stream().anyMatch(auth -> auth.getAuthority().equals(requiredRole))) {
                log.info("✅ 권한 매칭 성공: {}", requiredRole);
                return true;
            }
        }
        
        log.warn("❌ 권한 매칭 실패. 사용자 권한: {}, 필요한 권한: {}", 
            userAuthorities,
            java.util.Arrays.toString(requiredPermissions));
        return false;
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
