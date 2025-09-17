package com.finalproject.springbackend.service;

import com.finalproject.springbackend.dto.Permission;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PermissionService {
    
    private static final Logger log = LoggerFactory.getLogger(PermissionService.class);

    // 사용자별 권한 매핑 (실제 환경에서는 DB에서 조회)
    private final Map<String, Permission> userPermissions = new HashMap<>();

    @PostConstruct
    public void initUserPermissions() {
        // 예시 사용자 권한 설정 (실제 환경에서는 DB에서 조회)
        userPermissions.put("admin", Permission.ADMIN);
        userPermissions.put("sd", Permission.MANAGER);
        userPermissions.put("urd", Permission.MONITOR);
        
        log.info("사용자 권한 초기화 완료: {}", userPermissions);
    }

    public boolean hasPermission(String username, Permission permission) {
        Permission userPermission = userPermissions.getOrDefault(username, null);
        
        if (userPermission == null) {
            log.debug("권한 체크: {} - {} = false (사용자 권한 없음)", username, permission.getCode());
            return false;
        }
        
        // ADMIN 권한이 있으면 모든 권한 허용
        if (userPermission == Permission.ADMIN) {
            log.debug("권한 체크: {} - {} = true (ADMIN 권한)", username, permission.getCode());
            return true;
        }
        
        // MANAGER 권한이 있으면 MONITOR 권한도 허용
        if (userPermission == Permission.MANAGER && permission == Permission.MONITOR) {
            log.debug("권한 체크: {} - {} = true (MANAGER 권한으로 MONITOR 접근)", username, permission.getCode());
            return true;
        }
        
        // 동일한 권한이면 허용
        boolean hasPermission = userPermission == permission;
        log.debug("권한 체크: {} (권한: {}) - {} = {}", username, userPermission.getCode(), permission.getCode(), hasPermission);
        return hasPermission;
    }

    public boolean hasAnyPermission(String username, Permission... permissions) {
        for (Permission permission : permissions) {
            if (hasPermission(username, permission)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAllPermissions(String username, Permission... permissions) {
        for (Permission permission : permissions) {
            if (!hasPermission(username, permission)) {
                return false;
            }
        }
        return true;
    }

    public Set<Permission> getUserPermissions(String username) {
        Permission userPermission = userPermissions.get(username);
        if (userPermission == null) {
            return new HashSet<>();
        }
        
        Set<Permission> permissions = new HashSet<>();
        permissions.add(userPermission);
        
        // ADMIN 권한이 있으면 모든 권한 추가
        if (userPermission == Permission.ADMIN) {
            permissions.addAll(Arrays.asList(Permission.values()));
        }
        // MANAGER 권한이 있으면 MONITOR 권한도 추가
        else if (userPermission == Permission.MANAGER) {
            permissions.add(Permission.MONITOR);
        }
        
        return permissions;
    }

    public List<String> getPermissionCodes(String username) {
        return getUserPermissions(username).stream()
                .map(Permission::getCode)
                .sorted()
                .toList();
    }

    public Permission getUserPermission(String username) {
        return userPermissions.getOrDefault(username, null);
    }

    public void setUserPermission(String username, Permission permission) {
        userPermissions.put(username, permission);
        log.info("사용자 권한 변경: {} -> {}", username, permission.getDescription());
    }
}

