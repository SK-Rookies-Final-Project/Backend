package com.finalproject.springbackend.db.repository;

import com.finalproject.springbackend.db.entity.CertifiedNotMove;
import com.finalproject.springbackend.db.repository.projection.AlertTypeCount;
import com.finalproject.springbackend.db.repository.projection.IpCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface CertifiedNotMoveRepository extends JpaRepository<CertifiedNotMove, String>{

    List<CertifiedNotMove> findByClientIpOrderByAlertTypeAscFailureCountDescAlertTimeKSTAsc(String clientIp);
    List<CertifiedNotMove> findByAlertTypeOrderByFailureCountDescAlertTimeKSTAsc(String alertType);

    long countByClientIp(String clientIp);
    long countByAlertType(String alertType);

    //시간으로만 찾기 - 명시적 시간 비교로 시간대 문제 해결
    @Query("""
        SELECT cnm FROM CertifiedNotMove cnm
        WHERE cnm.alertTimeKST >= :start AND cnm.alertTimeKST <= :end
        ORDER BY cnm.failureCount DESC, cnm.alertType ASC, cnm.alertTimeKST ASC
    """)
    List<CertifiedNotMove> findByTimesOnly(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end
    );

    @Query("""
        SELECT COUNT(cnm) FROM CertifiedNotMove cnm
        WHERE cnm.alertTimeKST >= :start AND cnm.alertTimeKST <= :end
    """)
    long countByTimesOnly(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end
    );

    //시간 + clientIp 기준으로 찾기
    @Query("""
        SELECT cnm FROM CertifiedNotMove cnm
        WHERE cnm.alertTimeKST >= :start AND cnm.alertTimeKST <= :end
          AND cnm.clientIp = :clientIp
        ORDER BY cnm.failureCount DESC, cnm.alertType ASC, cnm.alertTimeKST ASC
    """)
    List<CertifiedNotMove> findByC(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end,
            @Param(value = "clientIp") String clientIp
    );

    @Query("""
        SELECT COUNT(cnm) FROM CertifiedNotMove cnm
        WHERE cnm.alertTimeKST >= :start AND cnm.alertTimeKST <= :end
          AND cnm.clientIp = :clientIp
    """)
    long countByC(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end,
            @Param(value = "clientIp") String clientIp
    );

    //시간 + alertType
    @Query("""
        SELECT cnm FROM CertifiedNotMove cnm
        WHERE cnm.alertTimeKST >= :start AND cnm.alertTimeKST <= :end
          AND cnm.alertType = :alertType
        ORDER BY cnm.failureCount DESC, cnm.alertType ASC, cnm.alertTimeKST ASC
    """)
    List<CertifiedNotMove> findByA(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end,
            @Param(value = "alertType") String alertType
    );

    @Query("""
        SELECT COUNT(cnm) FROM CertifiedNotMove cnm
        WHERE cnm.alertTimeKST >= :start AND cnm.alertTimeKST <= :end
          AND cnm.alertType = :alertType
    """)
    long countByA(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end,
            @Param(value = "alertType") String alertType
    );
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**비인가 접근 한 clientIp 갯수*/
    @Query("""
        SELECT cnm.clientIp AS clientIp, COUNT(cnm) AS count 
        FROM CertifiedNotMove AS cnm
        WHERE cnm.alertTimeKST >= :start AND cnm.alertTimeKST <= :end
        GROUP BY cnm.clientIp
        ORDER BY COUNT(cnm) DESC
    """)
    List<IpCount> clientIpCount(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end
    );

    @Query("""
        SELECT cnm.clientIp AS clientIp, COUNT(cnm) as count
        FROM CertifiedNotMove AS cnm
        GROUP BY cnm.clientIp
        ORDER BY COUNT(cnm) DESC
    """)
    List<IpCount> clientIpCountAll();

    /**비인가 접근 시도 유형 수*/
    @Query("""
        SELECT cnm.alertType AS alertType, COUNT(cnm) AS count
        FROM CertifiedNotMove cnm
        WHERE cnm.alertTimeKST >= :start AND cnm.alertTimeKST <= :end
        GROUP BY cnm.alertType
        ORDER BY COUNT(cnm) DESC
    """)
    List<AlertTypeCount> alertTypeCount(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end
    );

    @Query("""
        SELECT cnm.alertType AS alertType, COUNT(cnm) as count
        FROM CertifiedNotMove cnm
        GROUP BY cnm.alertType
        ORDER BY COUNT(cnm) DESC
    """)
    List<AlertTypeCount> alertTypeCountAll();
}

