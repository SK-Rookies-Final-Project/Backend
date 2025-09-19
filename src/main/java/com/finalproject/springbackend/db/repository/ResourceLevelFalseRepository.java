package com.finalproject.springbackend.db.repository;

import com.finalproject.springbackend.db.entity.ResourceLevelFalse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface ResourceLevelFalseRepository extends JpaRepository<ResourceLevelFalse, String> {

    /** 각 레코드 별로 레코드 반환 */
    List<ResourceLevelFalse> findByPrincipal(String principal);
    List<ResourceLevelFalse> findByResourceName(String resourceName);
    List<ResourceLevelFalse> findByOperation(String operation);
    List<ResourceLevelFalse> findByClientIp(String clientIp);

    /** 각 레코드(1개) 별 크기 반환 */
    long countByPrincipal(String principal);
    long countByResourceName(String resourceName);
    long countByOperation(String operation);
    long countByClientIp(String clientIp);


    /** 시간 기준으로만 찾기*/
    //시간 기준으로만 찾기 - 명시적 쿼리로 시간대 문제 해결
    @Query("""
        SELECT r FROM ResourceLevelFalse r
        WHERE r.eventTimeKST >= :start AND r.eventTimeKST <= :end
        ORDER BY r.eventTimeKST ASC
    """)
    List<ResourceLevelFalse> findByEventTimeKSTBetweenOrderByEventTimeKSTAsc(
            @Param("start") OffsetDateTime start, 
            @Param("end") OffsetDateTime end
    );

    /** 시간과 하나의 컬럼으로 찾기*/
    //시간 + principal 기준으로 레코드 반환
    @Query("""
        SELECT r FROM ResourceLevelFalse r
        WHERE r.eventTimeKST >= :start AND r.eventTimeKST <= :end
          AND r.principal = :principal
        ORDER BY r.eventTimeKST ASC
    """)
    List<ResourceLevelFalse> findByEventTimeKSTBetweenAndPrincipalOrderByEventTimeKSTAsc(
            @Param("start") OffsetDateTime start, 
            @Param("end") OffsetDateTime end,  
            @Param("principal") String principal
    );

    //시간 + resourceName 기준으로 레코드 반환
    @Query("""
        SELECT r FROM ResourceLevelFalse r
        WHERE r.eventTimeKST >= :start AND r.eventTimeKST <= :end
          AND r.resourceName = :resourceName
        ORDER BY r.eventTimeKST ASC
    """)
    List<ResourceLevelFalse> findByEventTimeKSTBetweenAndResourceNameOrderByEventTimeKSTAsc(
            @Param("start") OffsetDateTime start, 
            @Param("end") OffsetDateTime end, 
            @Param("resourceName") String resourceName
    );

    //시간 + operation 기준으로 레코드 반환
    @Query("""
        SELECT r FROM ResourceLevelFalse r
        WHERE r.eventTimeKST >= :start AND r.eventTimeKST <= :end
          AND r.operation = :operation
        ORDER BY r.eventTimeKST ASC
    """)
    List<ResourceLevelFalse> findByEventTimeKSTBetweenAndOperationOrderByEventTimeKSTAsc(
            @Param("start") OffsetDateTime start, 
            @Param("end") OffsetDateTime end, 
            @Param("operation") String operation
    );

    //시간 + clientIp 기준으로 레코드 반환
    @Query("""
        SELECT r FROM ResourceLevelFalse r
        WHERE r.eventTimeKST >= :start AND r.eventTimeKST <= :end
          AND r.clientIp = :clientIp
        ORDER BY r.eventTimeKST ASC
    """)
    List<ResourceLevelFalse> findByEventTimeKSTBetweenAndClientIpOrderByEventTimeKSTAsc(
            @Param("start") OffsetDateTime start, 
            @Param("end") OffsetDateTime end, 
            @Param("clientIp") String clientIp
    );

    /** 시간 + 2가지 컬럼으로 찾기 */
    //시간 + principal + resourceName
    @Query("""
        SELECT r FROM ResourceLevelFalse r
        WHERE (r.eventTimeKST >= :start) AND (r.eventTimeKST <= :end)
          AND (r.principal = :principal)
          AND (r.resourceName = :resourceName)
        ORDER BY r.eventTimeKST ASC
    """)
    List<ResourceLevelFalse> findByPR(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end,
            @Param("principal") String principal,
            @Param("resourceName") String resourceName
    );

    //시간 + principal + operation
    @Query("""
        SELECT r FROM ResourceLevelFalse r
        WHERE (r.eventTimeKST >= :start) AND (r.eventTimeKST <= :end)
          AND (r.principal = :principal)
          AND (r.operation = :operation)
        ORDER BY r.eventTimeKST ASC
    """)
    List<ResourceLevelFalse> findByPO(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end,
            @Param("principal") String principal,
            @Param("operation") String operation
    );

    //시간 + principal + clientIp
    @Query("""
        SELECT r FROM ResourceLevelFalse r
        WHERE (r.eventTimeKST >= :start) AND (r.eventTimeKST <= :end)
          AND (r.principal = :principal)
          AND (r.clientIp = :clientIp)
        ORDER BY r.eventTimeKST ASC
    """)
    List<ResourceLevelFalse> findByPC(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end,
            @Param("principal") String principal,
            @Param("clientIp") String clientIp
    );

    //시간 + resourceName + operation
    @Query("""
        SELECT r FROM ResourceLevelFalse r
        WHERE (r.eventTimeKST >= :start) AND (r.eventTimeKST <= :end)
          AND (r.resourceName = :resourceName)
          AND (r.operation = :operation)
        ORDER BY r.eventTimeKST ASC
    """)
    List<ResourceLevelFalse> findByRO(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end,
            @Param("resourceName") String resourceName,
            @Param("operation") String operation
    );

    //시간 + resourceName + clientIp
    @Query("""
        SELECT r FROM ResourceLevelFalse r
        WHERE (r.eventTimeKST >= :start) AND (r.eventTimeKST <= :end)
          AND (r.resourceName = :resourceName)
          AND (r.clientIp = :clientIp)
        ORDER BY r.eventTimeKST ASC
    """)
    List<ResourceLevelFalse> findByRC(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end,
            @Param("resourceName") String resourceName,
            @Param("clientIp") String clientIp
    );

    //시간 + operation + clientIp
    @Query("""
        SELECT r FROM ResourceLevelFalse r
        WHERE (r.eventTimeKST >= :start) AND (r.eventTimeKST <= :end)
          AND (r.operation = :operation)
          AND (r.clientIp = :clientIp)
        ORDER BY r.eventTimeKST ASC
    """)
    List<ResourceLevelFalse> findByOC(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end,
            @Param("operation") String operation,
            @Param("clientIp") String clientIp
    );

    /** 시간 + 3가지 컬럼으로 레코드 찾기 */
    //시간 + principal, resourceName, operation
    @Query("""
        SELECT r FROM ResourceLevelFalse r
        WHERE (r.eventTimeKST >= :start) AND (r.eventTimeKST <= :end)
          AND (r.principal = :principal)
          AND (r.resourceName = :resourceName)
          AND (r.operation = :operation)
        ORDER BY r.eventTimeKST ASC
    """)
    List<ResourceLevelFalse> findByPRO(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end,
            @Param("principal") String principal,
            @Param("resourceName") String resourceName,
            @Param("operation") String operation
    );

    //시간 + principal, resourceName, clientIp
    @Query("""
        SELECT r FROM ResourceLevelFalse r
        WHERE (r.eventTimeKST >= :start) AND (r.eventTimeKST <= :end)
          AND (r.principal = :principal)
          AND (r.resourceName = :resourceName)
          AND (r.clientIp = :clientIp)
        ORDER BY r.eventTimeKST ASC
    """)
    List<ResourceLevelFalse> findByPRC(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end,
            @Param("principal") String principal,
            @Param("resourceName") String resourceName,
            @Param("clientIp") String clientIp
    );

    //시간 + principal + operation += clientIp
    @Query("""
        SELECT r FROM ResourceLevelFalse r
        WHERE (r.eventTimeKST >= :start) AND (r.eventTimeKST <= :end)
          AND (r.principal = :principal)
          AND (r.operation = :operation)
          AND (r.clientIp = :clientIp)
        ORDER BY r.eventTimeKST ASC
    """)
    List<ResourceLevelFalse> findByPOC(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end,
            @Param("principal") String principal,
            @Param("operation") String operation,
            @Param("clientIp") String clientIp
    );



    //시간 + resourceName, operation, clientIp
    @Query("""
        SELECT r FROM ResourceLevelFalse r
        WHERE (r.eventTimeKST >= :start) AND (r.eventTimeKST <= :end)
          AND (r.resourceName = :resourceName)
          AND (r.operation = :operation)
          AND (r.clientIp = :clientIp)
        ORDER BY r.eventTimeKST ASC
    """)
    List<ResourceLevelFalse> findByROC(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end,
            @Param("resourceName") String resourceName,
            @Param("operation") String operation,
            @Param("clientIp") String clientIp
    );

    /** 시간 + 4가지 컬럼으로 레코드 찾기 */
    //시간 + principal + resourceName, operation, clientIp
    @Query("""
        SELECT r FROM ResourceLevelFalse r
        WHERE (r.eventTimeKST >= :start) AND (r.eventTimeKST <= :end)
          AND (r.principal = :principal)
          AND (r.resourceName = :resourceName)
          AND (r.operation = :operation)
          AND (r.clientIp = :clientIp)
        ORDER BY r.eventTimeKST ASC
    """)
    List<ResourceLevelFalse> findByPROC(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end,
            @Param("principal") String principal,
            @Param("resourceName") String resourceName,
            @Param("operation") String operation,
            @Param("clientIp") String clientIp
    );
}

//principal
//resourceName
//operation
//clientIp