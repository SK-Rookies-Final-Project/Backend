package com.finalproject.springbackend.service;

import com.finalproject.springbackend.dto.LoginRequestDTO;
import com.finalproject.springbackend.dto.LoginResponseDTO;
import com.finalproject.springbackend.dto.UserInfo;
import com.finalproject.springbackend.dto.Permission;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final TopicService topicService;
    private final PermissionService permissionService;

    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();
    private final Map<String, String> userPasswords = new ConcurrentHashMap<>();

    @Value("${jwt.secret:mySecretKey}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    public LoginResponseDTO authenticate(LoginRequestDTO loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        // 인증 요청 처리

        try {
            List<String> allowedTopics = topicService.listTopics(username, password);
            String token = generateToken(username);
            Permission userPermission = permissionService.getUserPermission(username);
            String permissionDescription = userPermission != null ? userPermission.getDescription() : "권한 없음";

            // 로그인 성공 - 사용자 비밀번호 저장 (Consumer 시작용)
            userPasswords.put(username, password);
            log.debug("사용자 {}의 비밀번호 저장 완료", username);
            
            return LoginResponseDTO.builder()
                    .success(true)
                    .token(token)
                    .username(username)
                    .message("로그인 성공")
                    .allowedTopics(allowedTopics.toArray(new String[0]))
                    .permission(permissionDescription)
                    .build();
        } catch (Exception e) {
            String errorMessage = determineErrorMessage(e);
            log.error("로그인 실패: {} - {}", username, e.getMessage(), e);
            return LoginResponseDTO.builder()
                    .success(false)
                    .message(errorMessage)
                    .build();
        }
    }

    private String determineErrorMessage(Exception e) {
        String message = e.getMessage();
        if (message == null) {
            return "인증에 실패했습니다. 계정 정보를 확인해주세요";
        }
        
        if (message.contains("timeout") || message.contains("timed out")) {
            return "서버 연결 시간이 초과되었습니다. 잠시 후 다시 시도해주세요";
        } else if (message.contains("connection") || message.contains("refused")) {
            return "서버에 연결할 수 없습니다. 네트워크 상태를 확인해주세요";
        } else if (message.contains("authentication") || message.contains("unauthorized")) {
            return "인증에 실패했습니다. 사용자명과 비밀번호를 확인해주세요";
        } else if (message.contains("Kafka")) {
            return "시스템 연결에 문제가 있습니다. 잠시 후 다시 시도해주세요";
        } else {
            return "인증에 실패했습니다. 계정 정보를 확인해주세요";
        }
    }

    public boolean validateToken(String token) {
        try {
            if (blacklistedTokens.contains(token)) {
                return false;
            }

            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();

            if (claims.getExpiration().before(new Date())) {
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            if (blacklistedTokens.contains(token)) {
                return null;
            }

            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public void revokeToken(String token) {
        try {
            if (token != null && !token.trim().isEmpty()) {
                blacklistedTokens.add(token);
                String username = getUsernameFromToken(token);
                // 토큰 무효화 완료
                
                // 로그아웃 시 사용자 비밀번호 제거 (SSE Consumer 중지 후)
                if (username != null) {
                    // SseService에서 Consumer 중지 후 비밀번호 제거하도록 수정
                    // 사용자 토큰 무효화 완료
                }
                
                cleanupExpiredTokens();
            }
        } catch (Exception e) {
            log.error("토큰 무효화 중 오류 발생: {}", e.getMessage());
        }
    }
    
    // 로그아웃 시 사용자 비밀번호 제거 (SseService에서 호출)
    public void removeUserPassword(String username) {
        if (username != null) {
            userPasswords.remove(username);
            // 사용자 비밀번호 정보 제거
        }
    }

    private void cleanupExpiredTokens() {
        try {
            Set<String> expiredTokens = ConcurrentHashMap.newKeySet();

            for (String token : blacklistedTokens) {
                try {
                    SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
                    Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();

                    if (claims.getExpiration().before(new Date())) {
                        expiredTokens.add(token);
                    }
                } catch (Exception e) {
                    expiredTokens.add(token);
                }
            }

            blacklistedTokens.removeAll(expiredTokens);
        } catch (Exception e) {
            log.warn("만료된 토큰 정리 중 오류 발생: {}", e.getMessage());
        }
    }

    public int getBlacklistedTokenCount() {
        return blacklistedTokens.size();
    }

    public String getUserPassword(String username) {
        return userPasswords.get(username);
    }

    private String generateToken(String username) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }
}