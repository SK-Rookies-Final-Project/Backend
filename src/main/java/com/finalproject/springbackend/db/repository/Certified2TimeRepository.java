package com.finalproject.springbackend.db.repository;

import com.finalproject.springbackend.db.entity.Certified2Time;
import com.finalproject.springbackend.db.repository.projection.AlertTypeCount;
import com.finalproject.springbackend.db.repository.projection.IpCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface Certified2TimeRepository extends JpaRepository<Certified2Time, String>{

    List<Certified2Time> findByClientIpOrderByAlertTypeAscFailureCountDescAlertTimeKSTAsc(String clientIp);
    List<Certified2Time> findByAlertTypeOrderByFailureCountDescAlertTimeKSTAsc(String alertType);


    long countByClientIp(String clientIp);
    long countByAlertType(String alertType);

    //시간으로만 찾기 - 명시적 시간 비교로 시간대 문제 해결
    @Query("""
        SELECT c2 FROM Certified2Time c2
        WHERE c2.alertTimeKST >= :start AND c2.alertTimeKST <= :end
        ORDER BY c2.failureCount DESC, c2.alertType ASC, c2.alertTimeKST ASC
    """)
    List<Certified2Time> findByTimesOnly(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end
    );

    @Query("""
        SELECT COUNT(c2) FROM Certified2Time c2
        WHERE c2.alertTimeKST >= :start AND c2.alertTimeKST <= :end
    """)
    long countByTimesOnly(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end
    );

    //시간 + clientIp 기준으로 찾기
    @Query("""
        SELECT c2 FROM Certified2Time c2
        WHERE c2.alertTimeKST >= :start AND c2.alertTimeKST <= :end
          AND c2.clientIp = :clientIp
        ORDER BY c2.failureCount DESC, c2.alertType ASC, c2.alertTimeKST ASC
    """)
    List<Certified2Time> findByC(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end,
            @Param(value = "clientIp") String clientIp
    );

    @Query("""
        SELECT COUNT(c2) FROM Certified2Time c2
        WHERE c2.alertTimeKST >= :start AND c2.alertTimeKST <= :end
          AND c2.clientIp = :clientIp
    """)
    long countByC(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end,
            @Param(value = "clientIp") String clientIp
    );

    //시간 + alertType
    @Query("""
        SELECT c2 FROM Certified2Time c2
        WHERE c2.alertTimeKST >= :start AND c2.alertTimeKST <= :end
          AND c2.alertType = :alertType
        ORDER BY c2.failureCount DESC, c2.alertType ASC, c2.alertTimeKST ASC
    """)
    List<Certified2Time> findByA(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end,
            @Param(value = "alertType") String alertType
    );

    @Query("""
        SELECT COUNT(c2) FROM Certified2Time c2
        WHERE c2.alertTimeKST >= :start AND c2.alertTimeKST <= :end
          AND c2.alertType = :alertType
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
        SELECT c2.clientIp AS clientIp, COUNT(c2) AS count 
        FROM Certified2Time c2
        WHERE c2.alertTimeKST >= :start AND c2.alertTimeKST <= :end
        GROUP BY c2.clientIp
        ORDER BY COUNT(c2) DESC
    """)
    List<IpCount> clientIpCount(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end
    );

    @Query("""
        SELECT c2.clientIp AS clientIp, COUNT(c2) as count
        FROM Certified2Time AS c2
        GROUP BY c2.clientIp
        ORDER BY COUNT(c2) DESC
    """)
    List<IpCount> clientIpCountAll();

    /**비인가 접근 시도 유형 수*/
    @Query("""
        SELECT c2.alertType AS alertType, COUNT(c2) AS count
        FROM Certified2Time c2
        WHERE c2.alertTimeKST >= :start AND c2.alertTimeKST <= :end
        GROUP BY c2.alertType
        ORDER BY COUNT(c2) DESC
    """)
    List<AlertTypeCount> alertTypeCount(
            @Param(value = "start") OffsetDateTime start,
            @Param(value = "end") OffsetDateTime end
    );

    @Query("""
        SELECT c2.alertType AS alertType, COUNT(c2) as count
        FROM Certified2Time c2
        GROUP BY c2.alertType
        ORDER BY COUNT(c2) DESC
    """)
    List<AlertTypeCount> alertTypeCountAll();
}
