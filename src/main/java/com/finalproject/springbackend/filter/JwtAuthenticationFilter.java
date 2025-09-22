package com.finalproject.springbackend.filter;

import com.finalproject.springbackend.dto.Permission;
import com.finalproject.springbackend.dto.UserInfo;
import com.finalproject.springbackend.service.AuthService;
import com.finalproject.springbackend.service.PermissionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthService authService;
    private final PermissionService permissionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String token = null;
        
        // 1. Authorization 헤더에서 토큰 추출 (기존 방식)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        
        // 2. URL 파라미터에서 토큰 추출 (새로운 방식)
        if (token == null) {
            token = request.getParameter("token");
        }
        
        if (token != null && !token.trim().isEmpty()) {
            if (authService.validateToken(token)) {
                String username = authService.getUsernameFromToken(token);
                
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // 사용자의 권한 정보를 가져와서 Spring Security 권한으로 변환
                    Collection<GrantedAuthority> authorities = getUserAuthorities(username);
                    
                    // UserInfo 객체 생성 (비밀번호는 AuthService에서 저장된 것 사용)
                    String userPassword = authService.getUserPassword(username);
                    UserInfo userInfo = UserInfo.builder()
                            .username(username)
                            .password(userPassword)
                            .build();
                    
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(userInfo, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    log.info("✅ JWT 인증 성공: {} (권한: {})", username, authorities);
                }
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private Collection<GrantedAuthority> getUserAuthorities(String username) {
        Set<Permission> userPermissions = permissionService.getUserPermissions(username);
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        
        for (Permission permission : userPermissions) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + permission.name()));
        }
        
        log.info("사용자 {} 권한 변환: {} -> {}", username, userPermissions, authorities);
        return authorities;
    }
}
