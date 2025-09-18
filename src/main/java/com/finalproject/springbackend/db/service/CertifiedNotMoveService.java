package com.finalproject.springbackend.db.service;

import com.finalproject.springbackend.db.entity.CertifiedNotMove;
import com.finalproject.springbackend.db.repository.CertifiedNotMoveRepository;
import com.finalproject.springbackend.db.repository.projection.AlertTypeCount;
import com.finalproject.springbackend.db.repository.projection.IpCount;
import com.finalproject.springbackend.util.TimeZoneUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class CertifiedNotMoveService {

    private final CertifiedNotMoveRepository repo;

    /**보정 (한국 시간대 처리)*/
    //시간 보정
    private OffsetDateTime[] CorrectionOfTimes(OffsetDateTime start, OffsetDateTime end){
        //start가 null일 때 예외 처리
        if(start == null) { throw new IllegalArgumentException("시작 시간을 넣어주세요"); }

        //end가 null일 때 현재 한국 시간으로 보정
        if(end == null){ end = TimeZoneUtil.nowKST(); }

        // 사용자 입력을 한국 시간대로 해석
        start = TimeZoneUtil.interpretAsKST(start);
        end = TimeZoneUtil.interpretAsKST(end);

        log.info("🕐 CertifiedNotMove 시간 보정 완료 - 시작: {}, 종료: {}", 
            TimeZoneUtil.formatForDebug("시작", start),
            TimeZoneUtil.formatForDebug("종료", end));

        //start 시간이 end 시간 이후일 때 변경
        if(start.isAfter(end)){ OffsetDateTime tmp; tmp = start; start = end; end = tmp; }

        return new OffsetDateTime[] { start, end };
    }

    //clientIp 보정
    private String CorrectionOfClientIp(String clientIp){
        if(clientIp == null || clientIp.isBlank() || clientIp.isEmpty()) {
            throw new IllegalArgumentException("클라이언트 주소를 넣어주세요");
        }
        return clientIp.replaceAll("\\s+", "");
    }

    //alertType 보정
    private String CorrectionOfAlertType(String alertType) {
        if(alertType == null || alertType.isBlank() || alertType.isEmpty()){
            throw new IllegalArgumentException("비인가 접근 유형을 넣어주세요");
        }
        return alertType.replaceAll("\\s+", "");
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**모든 레코드 반환*/
    public List<CertifiedNotMove> getAll(){
        return repo.findAll();
    }
    public Long getAllCount(){
        return repo.count();
    }

    /**하나의 컬럼으로만 레코드 및 갯수 출력*/
    public List<CertifiedNotMove> getOnlyClientIp(String clientIp){
        clientIp = CorrectionOfClientIp(clientIp);
        List<CertifiedNotMove> cnmList = repo.findByClientIpOrderByAlertTypeAscFailureCountDescAlertTimeKSTAsc(clientIp);
        return cnmList;
    }

    public List<CertifiedNotMove> getOnlyAlertType(String alertType) {
        alertType = CorrectionOfAlertType(alertType);

        List<CertifiedNotMove> cnmList = repo.findByAlertTypeOrderByFailureCountDescAlertTimeKSTAsc(alertType);
        return cnmList;
    }

    public Long getOnlyClientIpCount(String clientIp) {
        clientIp = CorrectionOfClientIp(clientIp);

        Long count = repo.countByClientIp(clientIp);

        return count;
    }

    public Long getOnlyAlertTypeCount(String alertType){
        alertType = CorrectionOfAlertType(alertType);

        Long count = repo.countByAlertType(alertType);

        return count;
    }

    public List<CertifiedNotMove> getTimeOnly(OffsetDateTime start, OffsetDateTime end) {
        OffsetDateTime[] times = CorrectionOfTimes(start, end);
        start = times[0];
        end = times[1];
        List<CertifiedNotMove> cnmList = repo.findByTimesOnly(start, end);

        return cnmList;
    }

    public Long getTimeOnlyCount(OffsetDateTime start, OffsetDateTime end) {
        OffsetDateTime[] times = CorrectionOfTimes(start, end);
        start = times[0]; end = times[1];

        Long count = repo.countByTimesOnly(start, end);
        return count;
    }

    public List<CertifiedNotMove> getC(
            OffsetDateTime start, OffsetDateTime end, String clientIp
    ){
        OffsetDateTime[] times = CorrectionOfTimes(start, end); start = times[0]; end = times[1];

        clientIp = CorrectionOfClientIp(clientIp);
        return repo.findByC(start, end, clientIp);
    }

    public Long getCCount(OffsetDateTime start, OffsetDateTime end, String clientIp) {
        OffsetDateTime[] times = CorrectionOfTimes(start, end);
        return repo.countByC(times[0], times[1], CorrectionOfClientIp(clientIp));
    }

    public List<CertifiedNotMove> getA(OffsetDateTime start, OffsetDateTime end, String alertType) {
        OffsetDateTime[] times = CorrectionOfTimes(start, end);
        return repo.findByA(times[0], times[1], CorrectionOfAlertType(alertType));
    }

    public Long getACount(OffsetDateTime start, OffsetDateTime end, String alertType) {
        OffsetDateTime[] times = CorrectionOfTimes(start, end);
        return repo.countByA(times[0], times[1], CorrectionOfAlertType(alertType));
    }

    public List<IpCount> getIpCount(OffsetDateTime start, OffsetDateTime end) {
        OffsetDateTime[] times = CorrectionOfTimes(start, end);
        start = times[0]; end = times[1];

        return repo.clientIpCount(start, end);
    }

    public List<IpCount> getIpCountAll(){
        return repo.clientIpCountAll();
    }

    public List<AlertTypeCount> getAlertTypeCount(OffsetDateTime start, OffsetDateTime end) {
        OffsetDateTime[] times = CorrectionOfTimes(start, end);
        start = times[0]; end = times[1];

        return repo.alertTypeCount(start, end);
    }

    public List<AlertTypeCount> getAlertTypeCountAll(){
        return repo.alertTypeCountAll();
    }

}
