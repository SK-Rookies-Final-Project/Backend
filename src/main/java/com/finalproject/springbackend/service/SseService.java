package com.finalproject.springbackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseService {
    
    private static final Logger log = LoggerFactory.getLogger(SseService.class);
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private AuthService authService;
    
    // 사용자별 SSE 연결 관리 (username -> clientId -> SseEmitter)
    private final Map<String, Map<String, SseEmitter>> userCertified2TimeEmitters = new ConcurrentHashMap<>(); 
    private final Map<String, Map<String, SseEmitter>> userSystemLevelFalseEmitters = new ConcurrentHashMap<>();
    private final Map<String, Map<String, SseEmitter>> userResourceLevelFalseEmitters = new ConcurrentHashMap<>();
    private final Map<String, Map<String, SseEmitter>> userCertifiedNotMoveEmitters = new ConcurrentHashMap<>();
    
    // 기존 방식 유지 (하위 호환성을 위해)
    private final Map<String, SseEmitter> certified2TimeEmitters = new ConcurrentHashMap<>(); 
    private final Map<String, SseEmitter> systemLevelFalseEmitters = new ConcurrentHashMap<>();
    private final Map<String, SseEmitter> resourceLevelFalseEmitters = new ConcurrentHashMap<>();
    private final Map<String, SseEmitter> certifiedNotMoveEmitters = new ConcurrentHashMap<>();


    public SseEmitter createSystemLevelFalseStream() {
        log.info("SSE 스트림 생성: system-level-false 토픽");
        return createSseConnection(systemLevelFalseEmitters, "system-level-false");
    }
    
    public SseEmitter createResourceLevelFalseStream() {
        log.info("SSE 스트림 생성: resource-level-false 토픽");
        return createSseConnection(resourceLevelFalseEmitters, "resource-level-false");
    }
    
    public SseEmitter createCertifiedNotMoveStream() {
        log.info("SSE 스트림 생성: certified-notMove 토픽");
        return createSseConnection(certifiedNotMoveEmitters, "certified-notMove");
    }
    
    public SseEmitter createCertified2TimeStream() {
        log.info("SSE 스트림 생성: certified-2time 토픽");
        return createSseConnection(certified2TimeEmitters, "certified-2time");
    }
    
    // 사용자별 SSE 연결 생성 메서드들
    public SseEmitter createUserSystemLevelFalseStream(String username) {
        log.info("사용자별 SSE 스트림 생성: system-level-false 토픽, 사용자: {}", username);
        // Consumer 시작 (실제 사용자 비밀번호 사용)
        String password = authService.getUserPassword(username);
        if (password != null) {
            SystemLevelFalseConsumer consumer = applicationContext.getBean(SystemLevelFalseConsumer.class);
            consumer.startConsumerForUser(username, password);
        } else {
            log.warn("사용자 {}의 비밀번호를 찾을 수 없습니다", username);
        }
        return createUserSseConnection(userSystemLevelFalseEmitters, username, "system-level-false");
    }
    
    public SseEmitter createUserResourceLevelFalseStream(String username) {
        log.info("사용자별 SSE 스트림 생성: resource-level-false 토픽, 사용자: {}", username);
        // Consumer 시작 (실제 사용자 비밀번호 사용)
        String password = authService.getUserPassword(username);
        if (password != null) {
            ResourceLevelFalseConsumer consumer = applicationContext.getBean(ResourceLevelFalseConsumer.class);
            consumer.startConsumerForUser(username, password);
        } else {
            log.warn("사용자 {}의 비밀번호를 찾을 수 없습니다", username);
        }
        return createUserSseConnection(userResourceLevelFalseEmitters, username, "resource-level-false");
    }
    
    public SseEmitter createUserCertifiedNotMoveStream(String username) {
        log.info("사용자별 SSE 스트림 생성: certified-notMove 토픽, 사용자: {}", username);
        // Consumer 시작 (실제 사용자 비밀번호 사용)
        String password = authService.getUserPassword(username);
        if (password != null) {
            CertifiedNotMoveConsumer consumer = applicationContext.getBean(CertifiedNotMoveConsumer.class);
            consumer.startConsumerForUser(username, password);
        } else {
            log.warn("사용자 {}의 비밀번호를 찾을 수 없습니다", username);
        }
        return createUserSseConnection(userCertifiedNotMoveEmitters, username, "certified-notMove");
    }
    
    public SseEmitter createUserCertified2TimeStream(String username) {
        log.info("사용자별 SSE 스트림 생성: certified-2time 토픽, 사용자: {}", username);
        // Consumer 시작 (실제 사용자 비밀번호 사용)
        String password = authService.getUserPassword(username);
        if (password != null) {
            Certified2TimeConsumer consumer = applicationContext.getBean(Certified2TimeConsumer.class);
            consumer.startConsumerForUser(username, password);
        } else {
            log.warn("사용자 {}의 비밀번호를 찾을 수 없습니다", username);
        }
        return createUserSseConnection(userCertified2TimeEmitters, username, "certified-2time");
    }

    private SseEmitter createSseConnection(Map<String, SseEmitter> emitterMap, String topicName) {
        String clientId = java.util.UUID.randomUUID().toString();
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitterMap.put(clientId, emitter);
        
        log.info("SSE 연결 생성 완료: {} 토픽, Client ID: {}, 활성 연결 수: {}", 
                topicName, clientId, emitterMap.size());
        
        emitter.onCompletion(() -> {
            emitterMap.remove(clientId);
            log.info("SSE 연결 완료: {} 토픽, 남은 연결 수: {}", topicName, emitterMap.size());
        });
        
        emitter.onTimeout(() -> {
            emitterMap.remove(clientId);
            log.warn("SSE 연결 타임아웃: {} 토픽, 남은 연결 수: {}", topicName, emitterMap.size());
        });
        
        emitter.onError((throwable) -> {
            emitterMap.remove(clientId);
            log.error("SSE 연결 오류: {} 토픽, 오류: {}, 남은 연결 수: {}", 
                    topicName, throwable.getMessage(), emitterMap.size());
        });
        
        return emitter;
    }
    
    private SseEmitter createUserSseConnection(Map<String, Map<String, SseEmitter>> userEmitterMap, String username, String topicName) {
        // 기존 연결이 있다면 모두 정리
        closeUserConnections(userEmitterMap, username, topicName);
        
        // 사용자별 연결 맵이 없으면 생성
        userEmitterMap.computeIfAbsent(username, k -> new ConcurrentHashMap<>());
        Map<String, SseEmitter> userEmitters = userEmitterMap.get(username);
        
        String clientId = java.util.UUID.randomUUID().toString();
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        userEmitters.put(clientId, emitter);
        
        log.info("사용자별 SSE 연결 생성 완료: {} 토픽, 사용자: {}, Client ID: {}, 활성 연결 수: {}", 
                topicName, username, clientId, userEmitters.size());
        
        emitter.onCompletion(() -> {
            userEmitters.remove(clientId);
            if (userEmitters.isEmpty()) {
                userEmitterMap.remove(username);
            }
            log.info("사용자별 SSE 연결 완료: {} 토픽, 사용자: {}, 남은 연결 수: {}", 
                    topicName, username, userEmitters.size());
        });
        
        emitter.onTimeout(() -> {
            userEmitters.remove(clientId);
            if (userEmitters.isEmpty()) {
                userEmitterMap.remove(username);
            }
            log.warn("사용자별 SSE 연결 타임아웃: {} 토픽, 사용자: {}, 남은 연결 수: {}", 
                    topicName, username, userEmitters.size());
        });
        
        emitter.onError((throwable) -> {
            userEmitters.remove(clientId);
            if (userEmitters.isEmpty()) {
                userEmitterMap.remove(username);
            }
            // IOException은 연결 중단으로 간주하여 WARN 레벨로 처리
            if (throwable instanceof IOException) {
                log.warn("사용자별 SSE 연결 중단: {} 토픽, 사용자: {}, Client ID: {}, 오류: {}, 남은 연결 수: {}", 
                        topicName, username, clientId, throwable.getMessage(), userEmitters.size());
            } else {
                log.error("사용자별 SSE 연결 오류: {} 토픽, 사용자: {}, Client ID: {}, 오류: {}, 남은 연결 수: {}", 
                        topicName, username, clientId, throwable.getMessage(), userEmitters.size());
            }
        });
        
        return emitter;
    }
    
    // 사용자의 모든 SSE 연결 정리
    public void closeUserConnections(String username) {
        closeUserConnections(userCertified2TimeEmitters, username, "certified-2time");
        closeUserConnections(userSystemLevelFalseEmitters, username, "system-level-false");
        closeUserConnections(userResourceLevelFalseEmitters, username, "resource-level-false");
        closeUserConnections(userCertifiedNotMoveEmitters, username, "certified-notMove");
        log.info("사용자 {}의 모든 SSE 연결 정리 완료", username);
    }
    
    private void closeUserConnections(Map<String, Map<String, SseEmitter>> userEmitterMap, String username, String topicName) {
        Map<String, SseEmitter> userEmitters = userEmitterMap.get(username);
        if (userEmitters != null && !userEmitters.isEmpty()) {
            log.info("사용자 {}의 기존 {} 연결 {}개 정리 중", username, topicName, userEmitters.size());
            userEmitters.forEach((clientId, emitter) -> {
                try {
                    emitter.complete();
                } catch (Exception e) {
                    log.warn("SSE 연결 정리 중 오류: {}", e.getMessage());
                }
            });
            userEmitters.clear();
            userEmitterMap.remove(username);
        }
    }
    
    public Map<String, SseEmitter> getCertified2TimeEmitters() {
        return certified2TimeEmitters;
    }
    
    public Map<String, SseEmitter> getSystemLevelFalseEmitters() {
        return systemLevelFalseEmitters;
    }
    
    public Map<String, SseEmitter> getResourceLevelFalseEmitters() {
        return resourceLevelFalseEmitters;
    }
    
    public Map<String, SseEmitter> getCertifiedNotMoveEmitters() {
        return certifiedNotMoveEmitters;
    }
    
    // 사용자별 SSE 연결 getter 메서드들
    public Map<String, SseEmitter> getUserCertified2TimeEmitters(String username) {
        return userCertified2TimeEmitters.getOrDefault(username, new ConcurrentHashMap<>());
    }
    
    public Map<String, SseEmitter> getUserSystemLevelFalseEmitters(String username) {
        return userSystemLevelFalseEmitters.getOrDefault(username, new ConcurrentHashMap<>());
    }
    
    public Map<String, SseEmitter> getUserResourceLevelFalseEmitters(String username) {
        return userResourceLevelFalseEmitters.getOrDefault(username, new ConcurrentHashMap<>());
    }
    
    public Map<String, SseEmitter> getUserCertifiedNotMoveEmitters(String username) {
        return userCertifiedNotMoveEmitters.getOrDefault(username, new ConcurrentHashMap<>());
    }
    
    // 모든 사용자별 SSE 연결 정보 조회
    public Map<String, Map<String, SseEmitter>> getAllUserCertified2TimeEmitters() {
        return userCertified2TimeEmitters;
    }
    
    public Map<String, Map<String, SseEmitter>> getAllUserSystemLevelFalseEmitters() {
        return userSystemLevelFalseEmitters;
    }
    
    public Map<String, Map<String, SseEmitter>> getAllUserResourceLevelFalseEmitters() {
        return userResourceLevelFalseEmitters;
    }
    
    public Map<String, Map<String, SseEmitter>> getAllUserCertifiedNotMoveEmitters() {
        return userCertifiedNotMoveEmitters;
    }
    
    // 연결 상태 체크 및 정리 메서드
    public void cleanupInactiveConnections() {
        cleanupUserConnections(userCertified2TimeEmitters, "certified-2time");
        cleanupUserConnections(userSystemLevelFalseEmitters, "system-level-false");
        cleanupUserConnections(userResourceLevelFalseEmitters, "resource-level-false");
        cleanupUserConnections(userCertifiedNotMoveEmitters, "certified-notMove");
    }
    
    // 주기적으로 비활성 연결 정리 (10분마다 실행)
    @Scheduled(fixedRate = 600000) // 10분 = 600,000ms
    public void scheduledCleanup() {
        try {
            log.debug("SSE 연결 상태 체크 및 정리 시작");
            cleanupInactiveConnections();
            log.debug("SSE 연결 상태 체크 및 정리 완료");
        } catch (Exception e) {
            log.error("SSE 연결 정리 중 오류 발생: {}", e.getMessage(), e);
        }
    }
    
    private void cleanupUserConnections(Map<String, Map<String, SseEmitter>> userEmitterMap, String topicName) {
        userEmitterMap.forEach((username, userEmitters) -> {
            if (userEmitters != null && !userEmitters.isEmpty()) {
                // ConcurrentModificationException 방지를 위해 복사본 생성
                Map<String, SseEmitter> emittersCopy = new ConcurrentHashMap<>(userEmitters);
                emittersCopy.forEach((clientId, emitter) -> {
                    try {
                        // 연결 상태 체크를 위해 heartbeat 전송 (ping 대신)
                        emitter.send(SseEmitter.event()
                                .name("heartbeat")
                                .data("{\"type\":\"heartbeat\",\"timestamp\":" + System.currentTimeMillis() + "}", 
                                      MediaType.APPLICATION_JSON));
                    } catch (Exception e) {
                        log.debug("비활성 연결 감지 및 정리: {} 토픽, 사용자: {}, Client ID: {}, 오류: {}", 
                                topicName, username, clientId, e.getMessage());
                        userEmitters.remove(clientId);
                    }
                });
                
                // 빈 사용자 맵 정리
                if (userEmitters.isEmpty()) {
                    userEmitterMap.remove(username);
                    log.debug("사용자 {}의 {} 토픽 연결이 모두 정리됨", username, topicName);
                }
            }
        });
    }

}