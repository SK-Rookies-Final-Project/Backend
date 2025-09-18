package com.finalproject.springbackend.db.repository;

import com.finalproject.springbackend.db.entity.SystemLevelFalse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface SystemLevelFalseRepository extends JpaRepository<SystemLevelFalse, String> {

    @Query("""
        SELECT s FROM SystemLevelFalse s
        WHERE (s.eventTimeKST >= :start) AND (s.eventTimeKST <= :end)
        ORDER BY s.eventTimeKST ASC
    """)
    List<SystemLevelFalse> findByTimeOnly(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );
    @Query("""
        SELECT COUNT(s) FROM SystemLevelFalse s
        WHERE (s.eventTimeKST >= :start) AND (s.eventTimeKST <= :end)
    """)
    Long findByTimeOnlyCount(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );

    /**각 필드 별 레코드 반환*/
    List<SystemLevelFalse> findByPrincipal(String principal);
    List<SystemLevelFalse> findByResourceName(String resourceName);
    List<SystemLevelFalse> findByOperation(String operation);
    List<SystemLevelFalse> findByClientIp(String clientIp);

    /**각 필드 별 레코드 갯수*/
    Long countByPrincipal(String principal);
    Long countByResourceName(String resourceName);
    Long countByOperation(String operation);
    Long countByClientIp(String clientIp);

    /**시간 + 하나의 컬럼 레코드 및 갯수*/
    //start + end + principal
    @Query("""
        SELECT s FROM SystemLevelFalse s
        WHERE (s.eventTimeKST >= :start) AND (s.eventTimeKST <= :end)
          AND (s.principal = :principal)
        ORDER BY s.eventTimeKST ASC
    """)
    List<SystemLevelFalse> getP(
            @Param(value = "start") OffsetDateTime start, @Param(value = "end") OffsetDateTime end,
            @Param("principal") String principal
    );
    //start + end + principal 갯수
    @Query("""
        SELECT COUNT(s) FROM SystemLevelFalse s
        WHERE (s.eventTimeKST >= :start) AND (s.eventTimeKST <= :end)
          AND (s.principal = :principal)
    """)
    Long getPCount(
            @Param(value = "start") OffsetDateTime start, @Param(value = "end") OffsetDateTime end,
            @Param("principal") String principal
    );

    //start + end + resource_name
    @Query("""
        SELECT s FROM SystemLevelFalse s
        WHERE (s.eventTimeKST >= :start) AND (s.eventTimeKST <= :end)
          AND (s.resourceName = :resourceName)
        ORDER BY s.eventTimeKST ASC
    """)
    List<SystemLevelFalse> getR(
            @Param(value = "start") OffsetDateTime start, @Param(value = "end") OffsetDateTime end,
            @Param("resourceName") String resourceName
    );
    //start + end + resource_name 갯수
    @Query("""
        SELECT COUNT(s) FROM SystemLevelFalse s
        WHERE (s.eventTimeKST >= :start) AND (s.eventTimeKST <= :end)
          AND (s.resourceName = :resourceName)
    """)
    Long getRCount(
            @Param(value = "start") OffsetDateTime start, @Param(value = "end") OffsetDateTime end,
            @Param("resourceName") String resourceName
    );

    //start + end + operation
    @Query("""
        SELECT s FROM SystemLevelFalse s
        WHERE (s.eventTimeKST >= :start) AND (s.eventTimeKST <= :end)
          AND (s.operation = :operation)
        ORDER BY s.eventTimeKST ASC
    """)
    List<SystemLevelFalse> getO(
            @Param(value = "start") OffsetDateTime start, @Param(value = "end") OffsetDateTime end,
            @Param("operation") String operation
    );
    //start + end + operation 갯수
    @Query("""
        SELECT COUNT(s) FROM SystemLevelFalse s
        WHERE (s.eventTimeKST >= :start) AND (s.eventTimeKST <= :end)
          AND (s.operation = :operation)
    """)
    Long getOCount(
            @Param(value = "start") OffsetDateTime start, @Param(value = "end") OffsetDateTime end,
            @Param("operation") String operation
    );

    //start + end + client_ip
    @Query("""
        SELECT s FROM SystemLevelFalse s
        WHERE (s.eventTimeKST >= :start) AND (s.eventTimeKST <= :end)
          AND (s.clientIp = :clientIp)
        ORDER BY s.eventTimeKST ASC
    """)
    List<SystemLevelFalse> getC(
            @Param(value = "start") OffsetDateTime start, @Param(value = "end") OffsetDateTime end,
            @Param("clientIp") String clientIp
    );
    //start + end + client_ip 갯수
    @Query("""
        SELECT COUNT(s) FROM SystemLevelFalse s
        WHERE (s.eventTimeKST >= :start) AND (s.eventTimeKST <= :end)
          AND (s.clientIp = :clientIp)
    """)
    Long getCCount(
            @Param(value = "start") OffsetDateTime start, @Param(value = "end") OffsetDateTime end,
            @Param("clientIp") String clientIp
    );

    /**start + end + 2개의 컬럼*/
    //start + end + principal, resource_name
    @Query("""
        SELECT s FROM SystemLevelFalse s
        WHERE (s.eventTimeKST >= :start) AND (s.eventTimeKST <= :end)
          AND (s.principal = :principal)
          AND (s.resourceName = :resourceName)
        ORDER BY s.eventTimeKST ASC
    """)
    List<SystemLevelFalse> getPR(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end,
            @Param(value = "principal") String principal,
            @Param(value = "resourceName") String resourceName
    );
    @Query("""
        SELECT COUNT(s) FROM SystemLevelFalse s
        WHERE (s.eventTimeKST >= :start) AND (s.eventTimeKST <= :end)
          AND (s.principal = :principal)
          AND (s.resourceName = :resourceName)
    """)
    Long getPRCount(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end,
            @Param(value = "principal") String principal,
            @Param(value = "resourceName") String resourceName
    );

    //start + end + principal, operation
    @Query("""
        SELECT s FROM SystemLevelFalse s
        WHERE (s.eventTimeKST >= :start) AND (s.eventTimeKST <= :end)
          AND (s.principal = :principal)
          AND (s.operation = :operation)
        ORDER BY s.eventTimeKST ASC
    """)
    List<SystemLevelFalse> getPO(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end,
            @Param(value = "principal") String principal,
            @Param(value = "operation") String operation
    );
    @Query("""
        SELECT COUNT(s) FROM SystemLevelFalse s
        WHERE (s.eventTimeKST >= :start) AND (s.eventTimeKST <= :end)
          AND (s.principal = :principal)
          AND (s.operation = :operation)
    """)
    Long getPOCount(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end,
            @Param(value = "principal") String principal,
            @Param(value = "operation") String operation
    );

    //start + end + principal, clientIp
    @Query("""
        SELECT s FROM SystemLevelFalse s
        WHERE (s.eventTimeKST >= :start) AND (s.eventTimeKST <= :end)
          AND (s.principal = :principal)
          AND (s.clientIp = :clientIp)
        ORDER BY s.eventTimeKST ASC
    """)
    List<SystemLevelFalse> getPC(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end,
            @Param(value = "principal") String principal,
            @Param(value = "clientIp") String clientIp
    );
    @Query("""
        SELECT COUNT(s) FROM SystemLevelFalse s
        WHERE (s.eventTimeKST >= :start) AND (s.eventTimeKST <= :end)
          AND (s.principal = :principal)
          AND (s.clientIp = :clientIp)
    """)
    Long getPCCount(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end,
            @Param(value = "principal") String principal,
            @Param(value = "clientIp") String clientIp
    );

    //start + end + resource_name, operation
    @Query("""
        SELECT s FROM SystemLevelFalse s
        WHERE (s.eventTimeKST >= :start) AND (s.eventTimeKST <= :end)
          AND (s.resourceName = :resourceName)
          AND (s.operation = :operation)
        ORDER BY s.eventTimeKST ASC
    """)
    List<SystemLevelFalse> getRO(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end,
            @Param(value = "resourceName") String resourceName,
            @Param(value = "operation") String operation
    );
    @Query("""
        SELECT COUNT(s) FROM SystemLevelFalse s
        WHERE (s.eventTimeKST >= :start) AND (s.eventTimeKST <= :end)
          AND (s.resourceName = :resourceName)
          AND (s.operation = :operation)
    """)
    Long getROCount(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end,
            @Param(value = "resourceName") String resourceName,
            @Param(value = "operation") String operation
    );

    //start + end + resource_name, client_ip
    @Query("""
        SELECT s FROM SystemLevelFalse s
        WHERE (s.eventTimeKST >= :start) AND (s.eventTimeKST <= :end)
          AND (s.resourceName = :resourceName)
          AND (s.clientIp = :clientIp)
        ORDER BY s.eventTimeKST ASC
    """)
    List<SystemLevelFalse> getRC(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end,
            @Param(value = "resourceName") String resourceName,
            @Param(value = "clientIp") String clientIp
    );
    @Query("""
        SELECT COUNT(s) FROM SystemLevelFalse s
        WHERE (s.eventTimeKST >= :start) AND (s.eventTimeKST <= :end)
          AND (s.resourceName = :resourceName)
          AND (s.clientIp = :clientIp)
    """)
    Long getRCCount(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end,
            @Param(value = "resourceName") String resourceName,
            @Param(value = "clientIp") String clientIp
    );

    //start + end + operation, client_ip
    @Query("""
        SELECT s FROM SystemLevelFalse s
        WHERE (s.eventTimeKST >= :start) AND (s.eventTimeKST <= :end)
          AND (s.operation = :operation)
          AND (s.clientIp = :clientIp)
        ORDER BY s.eventTimeKST ASC
    """)
    List<SystemLevelFalse> getOC(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end,
            @Param(value = "operation") String operation,
            @Param(value = "clientIp") String clientIp
    );
    @Query("""
        SELECT COUNT(s) FROM SystemLevelFalse s
        WHERE (s.eventTimeKST >= :start) AND (s.eventTimeKST <= :end)
          AND (s.operation = :operation)
          AND (s.clientIp = :clientIp)
    """)
    Long getOCCount(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end,
            @Param(value = "operation") String operation,
            @Param(value = "clientIp") String clientIp
    );


    /**시간 + 3가지 컬럼*/
    @Query("""
        SELECT s FROM SystemLevelFalse s
        WHERE (s.eventTimeKST >= :start) AND (s.eventTimeKST <= :end)
          AND (s.principal = :principal)
          AND (s.resourceName = :resourceName)
          AND (s.operation = :operation)
        ORDER BY s.eventTimeKST ASC
    """)
    List<SystemLevelFalse> getPRO(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end,
            @Param(value = "principal") String principal,
            @Param(value = "resourceName") String resourceName,
            @Param(value = "operation") String operation
    );

    @Query("""
        SELECT COUNT(s) FROM SystemLevelFalse s
        WHERE (s.eventTimeKST >= :start) AND (s.eventTimeKST <= :end)
          AND (s.principal = :principal)
          AND (s.resourceName = :resourceName)
          AND (s.operation = :operation)
    """)
    Long getPROCount(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end,
            @Param(value = "principal") String principal,
            @Param(value = "resourceName") String resourceName,
            @Param(value = "operation") String operation
    );

    @Query("""
        SELECT s FROM SystemLevelFalse s
        WHERE (s.eventTimeKST >= :start) AND (s.eventTimeKST <= :end)
          AND (s.principal = :principal)
          AND (s.resourceName = :resourceName)
          AND (s.clientIp = :clientIp)
        ORDER BY s.eventTimeKST ASC
    """)
    List<SystemLevelFalse> getPRC(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end,
            @Param(value = "principal") String principal,
            @Param(value = "resourceName") String resourceName,
            @Param(value = "clientIp") String clientIp
    );
    @Query("""
        SELECT COUNT(s) FROM SystemLevelFalse s
        WHERE (s.eventTimeKST >= :start) AND (s.eventTimeKST <= :end)
          AND (s.principal = :principal)
          AND (s.resourceName = :resourceName)
          AND (s.clientIp = :clientIp)
    """)
    Long getPRCCount(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end,
            @Param(value = "principal") String principal,
            @Param(value = "resourceName") String resourceName,
            @Param(value = "clientIp") String clientIp
    );

    @Query("""
        SELECT s FROM SystemLevelFalse s
        WHERE (s.eventTimeKST >= :start) AND (s.eventTimeKST <= :end)
          AND (s.principal = :principal)
          AND (s.operation = :operation)
          AND (s.clientIp = :clientIp)
        ORDER BY s.eventTimeKST ASC
    """)
    List<SystemLevelFalse> getPOC(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end,
            @Param(value = "principal") String principal,
            @Param(value = "operation") String operation,
            @Param(value = "clientIp") String clientIp
    );
    @Query("""
        SELECT COUNT(s) FROM SystemLevelFalse s
        WHERE (s.eventTimeKST >= :start) AND (s.eventTimeKST <= :end)
          AND (s.principal = :principal)
          AND (s.operation = :operation)
          AND (s.clientIp = :clientIp)
    """)
    Long getPOCCount(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end,
            @Param(value = "principal") String principal,
            @Param(value = "operation") String operation,
            @Param(value = "clientIp") String clientIp
    );

    @Query("""
        SELECT s FROM SystemLevelFalse s
        WHERE (s.eventTimeKST >= :start) AND (s.eventTimeKST <= :end)
          AND (s.resourceName = :resourceName)
          AND (s.operation = :operation)
          AND (s.clientIp = :clientIp)
        ORDER BY s.eventTimeKST ASC
    """)
    List<SystemLevelFalse> getROC(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end,
            @Param(value = "resourceName") String resourceName,
            @Param(value = "operation") String operation,
            @Param(value = "clientIp") String clientIp
    );
    @Query("""
        SELECT COUNT(s) FROM SystemLevelFalse s
        WHERE (s.eventTimeKST >= :start) AND (s.eventTimeKST <= :end)
          AND (s.resourceName = :resourceName)
          AND (s.operation = :operation)
          AND (s.clientIp = :clientIp)
    """)
    Long getROCCount(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end,
            @Param(value = "resourceName") String resourceName,
            @Param(value = "operation") String operation,
            @Param(value = "clientIp") String clientIp
    );


    /**시간 + 4개의 컬럼으로 찾기*/
    @Query("""
        SELECT s FROM SystemLevelFalse s
        WHERE (s.eventTimeKST >= :start) AND (s.eventTimeKST <= :end)
          AND (s.principal = :principal)
          AND (s.resourceName = :resourceName)
          AND (s.operation = :operation)
          AND (s.clientIp = :clientIp)
        ORDER BY s.eventTimeKST ASC
    """)
    List<SystemLevelFalse> getPROC(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end,
            @Param(value = "principal") String principal,
            @Param(value = "resourceName") String resourceName,
            @Param(value = "operation") String operation,
            @Param(value = "clientIp") String clientIp
    );
    @Query("""
        SELECT COUNT(s) FROM SystemLevelFalse s
        WHERE (s.eventTimeKST >= :start) AND (s.eventTimeKST <= :end)
          AND (s.principal = :principal)
          AND (s.resourceName = :resourceName)
          AND (s.operation = :operation)
          AND (s.clientIp = :clientIp)
    """)
    Long getPROCCount(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end,
            @Param(value = "principal") String principal,
            @Param(value = "resourceName") String resourceName,
            @Param(value = "operation") String operation,
            @Param(value = "clientIp") String clientIp
    );

}
