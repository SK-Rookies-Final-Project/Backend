package com.finalproject.springbackend.db.service;

import com.finalproject.springbackend.db.entity.SystemLevelFalse;
import com.finalproject.springbackend.db.repository.SystemLevelFalseRepository;
import com.finalproject.springbackend.util.TimeZoneUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SystemLevelFalseService {

    private final SystemLevelFalseRepository repo;

    /** start, end 시간 보정 메서드 (한국 시간대 처리) */
    //start, end 시간 전체 보정
    private OffsetDateTime[] timeCorrection(
            OffsetDateTime start, OffsetDateTime end
    ){
        start = ifStartIsNull(start);
        end = ifEndIsNull(end);
        
        // 사용자 입력을 한국 시간대로 해석
        start = TimeZoneUtil.interpretAsKST(start);
        end = TimeZoneUtil.interpretAsKST(end);
        
        log.info("🕐 SystemLevel 시간 보정 완료 - 시작: {}, 종료: {}", 
            TimeZoneUtil.formatForDebug("시작", start),
            TimeZoneUtil.formatForDebug("종료", end));
        
        return ifStartTimeAfterEndTime(start, end);
    }
    //start가 end보다 더 이른 시간일 시 start=end, end=start
    private OffsetDateTime[] ifStartTimeAfterEndTime(
            OffsetDateTime start, OffsetDateTime end
    ){
        if(start.isAfter(end)){
            OffsetDateTime tmp = start;
            start = end;
            end = tmp;
        }
        return new OffsetDateTime[]{
                start, end
        };
    }
    //end가 null일 때 현재 한국시간으로 보정
    private OffsetDateTime ifEndIsNull(OffsetDateTime end) {
        if(end == null) {
            end = TimeZoneUtil.nowKST();
            return end;
        } else {
            return end;
        }
    }
    //start가 null일 때 exception message 출력
    private OffsetDateTime ifStartIsNull(OffsetDateTime start) {
        if(start == null){
            throw new IllegalArgumentException("시작시간을 넣어주세요. \n 형식: start=yyyy-MM-ddTHH-mm-ssZ");
        } else { return start; }
    }

    //principal 보정
    private String correctionOfPrincipal(String principal){
        if(principal == null || principal.isBlank()) { throw new IllegalArgumentException("principal을 넣어주세요"); }
        principal = principal.replaceAll("\\s+", "");
        if(!principal.startsWith("User:")) { principal = "User:" + principal; }
        return principal;
    }

    //resourceName 보정
    private String correctionOfResourceName(String resourceName){
        if(resourceName == null || resourceName.isBlank()) { throw new IllegalArgumentException("resourceName을 넣어주세요"); }
        return resourceName.replaceAll("\\s+", "");
    }
    //operation 보정
    private String correctionOfOperation(String operation){
        if(operation == null || operation.isBlank()) { throw new IllegalArgumentException("operation을 넣어주세요"); }
        return operation.replaceAll("\\s+", "");
    }
    //clientIp 보정
    private String correctionOfClientIp(String clientIp){
        if(clientIp == null || clientIp.isBlank()){throw new IllegalArgumentException("clientIp값을 넣어주세요"); }
        return clientIp.replaceAll("\\s+", "");
    }
    
    //모든 레코드 불러오기
    public List<SystemLevelFalse> getAll() { return repo.findAll(); }
    //모든 레코드 갯수
    public Long getCount(){ return repo.count(); }

    //start <= time <= end 시간 범위 내 모든 레코드 값 가져오기
    public List<SystemLevelFalse> getTimeOnly(
            OffsetDateTime start, OffsetDateTime end
    ) {
        OffsetDateTime[] times = timeCorrection(start, end);
        start = times[0];
        end = times[1];
        return repo.findByTimeOnly(start, end);
    }


    public Long getTimeOnlyCount(OffsetDateTime start, OffsetDateTime end){
        OffsetDateTime[] times = timeCorrection(start, end);
        start = times[0];
        end = times[1];
        return repo.findByTimeOnlyCount(start, end);
    }

    //하나의 컬럼으로 레코드 찾기
    public List<SystemLevelFalse> getPrincipal(String principal) {
        principal = correctionOfPrincipal(principal);
        return repo.findByPrincipal(principal);
    }
    public List<SystemLevelFalse> getResourceName(String resourceName) {
        resourceName = correctionOfResourceName(resourceName);
        return repo.findByResourceName(resourceName);
    }
    public List<SystemLevelFalse> getOperation(String operation) {
        operation = correctionOfOperation(operation);
        return repo.findByOperation(operation);
    }
    public List<SystemLevelFalse> getClientIp(String clientIp) {
        clientIp = correctionOfClientIp(clientIp);
        return repo.findByClientIp(clientIp);
    }

    //하나의 컬럼으로 찾은 레코드의 갯수
    public Long getPrincipalCount(String principal) {
        principal = correctionOfPrincipal(principal);
        return repo.countByPrincipal(principal);
    }
    public Long getResourceNameCount(String resourceName) {
        resourceName = correctionOfResourceName(resourceName);
        return repo.countByResourceName(resourceName);
    }
    public Long getOperationCount(String operation) {
        operation = correctionOfOperation(operation);
        return repo.countByOperation(operation);
    }
    public Long getClientIpCount(String clientIp) {
        clientIp = correctionOfClientIp(clientIp);
        return repo.countByClientIp(clientIp);
    }

    //시간 + 하나의 컬럼
    public List<SystemLevelFalse> getP(
            OffsetDateTime start, OffsetDateTime end,
            String principal
    ){
        OffsetDateTime[] times = timeCorrection(start, end);
        start = times[0];
        end = times[1];
        principal = correctionOfPrincipal(principal);

        return repo.getP(start, end, principal);
    }

    public Long getPCount(
            OffsetDateTime start, OffsetDateTime end,
            String input
    ){
        OffsetDateTime[] times = timeCorrection(start, end);
        start = times[0];
        end = times[1];
        input = correctionOfPrincipal(input);

        return repo.getPCount(start, end, input);
    }

    public List<SystemLevelFalse> getR(
            OffsetDateTime start, OffsetDateTime end,
            String input
    ){
        OffsetDateTime[] times = timeCorrection(start, end);
        start = times[0];
        end = times[1];
        input = correctionOfResourceName(input);

        return repo.getR(start, end, input);
    }

    public Long getRCount(
            OffsetDateTime start, OffsetDateTime end,
            String input
    ){
        OffsetDateTime[] times = timeCorrection(start, end);
        start = times[0];
        end = times[1];
        input = correctionOfResourceName(input);

        return repo.getRCount(start, end, input);
    }

    public List<SystemLevelFalse> getO(
            OffsetDateTime start, OffsetDateTime end,
            String input
    ){
        OffsetDateTime[] times = timeCorrection(start, end);
        start = times[0];
        end = times[1];
        input = correctionOfOperation(input);

        return repo.getO(start, end, input);
    }

    public Long getOCount(
            OffsetDateTime start, OffsetDateTime end,
            String input
    ){
        OffsetDateTime[] times = timeCorrection(start, end);
        start = times[0];
        end = times[1];
        input = correctionOfOperation(input);

        return repo.getOCount(start, end, input);
    }

    public List<SystemLevelFalse> getC(
            OffsetDateTime start, OffsetDateTime end,
            String input
    ){
        OffsetDateTime[] times = timeCorrection(start, end);
        start = times[0];
        end = times[1];
        input = correctionOfClientIp(input);

        return repo.getC(start, end, input);
    }

    public Long getCCount(
            OffsetDateTime start, OffsetDateTime end,
            String input
    ){
        OffsetDateTime[] times = timeCorrection(start, end);
        start = times[0];
        end = times[1];
        input = correctionOfClientIp(input);

        return repo.getCCount(start, end, input);
    }

    public List<SystemLevelFalse> getPR(
            OffsetDateTime start, OffsetDateTime end,
            String principal,
            String resourceName
    ){
        OffsetDateTime[] times = timeCorrection(start, end);
        start = times[0];
        end = times[1];
        principal = correctionOfPrincipal(principal);
        resourceName = correctionOfResourceName(resourceName);

        return repo.getPR(start, end, principal, resourceName);
    }
    public Long getPRCount(
            OffsetDateTime start, OffsetDateTime end,
            String principal,
            String resourceName
    ){
        OffsetDateTime[] times = timeCorrection(start, end);
        start = times[0];
        end = times[1];
        principal = correctionOfPrincipal(principal);
        resourceName = correctionOfResourceName(resourceName);

        return repo.getPRCount(start, end, principal, resourceName);
    }

    public List<SystemLevelFalse> getPO(
            OffsetDateTime start, OffsetDateTime end,
            String input1,
            String input2
    ){
        OffsetDateTime[] times = timeCorrection(start, end);
        start = times[0];
        end = times[1];
        input1 = correctionOfPrincipal(input1);
        input2 = correctionOfOperation(input2);

        return repo.getPO(start, end, input1, input2);
    }
    public Long getPOCount(
            OffsetDateTime start, OffsetDateTime end,
            String input1,
            String input2
    ){
        OffsetDateTime[] times = timeCorrection(start, end);
        start = times[0];
        end = times[1];
        input1 = correctionOfPrincipal(input1);
        input2 = correctionOfOperation(input2);

        return repo.getPOCount(start, end, input1, input2);
    }

    public List<SystemLevelFalse> getPC(
            OffsetDateTime start, OffsetDateTime end,
            String input1,
            String input2
    ){
        OffsetDateTime[] times = timeCorrection(start, end);
        start = times[0];
        end = times[1];
        input1 = correctionOfPrincipal(input1);
        input2 = correctionOfClientIp(input2);

        return repo.getPC(start, end, input1, input2);
    }
    public Long getPCCount(
            OffsetDateTime start, OffsetDateTime end,
            String input1,
            String input2
    ){
        OffsetDateTime[] times = timeCorrection(start, end);
        start = times[0];
        end = times[1];
        input1 = correctionOfPrincipal(input1);
        input2 = correctionOfClientIp(input2);

        return repo.getPCCount(start, end, input1, input2);
    }

    public List<SystemLevelFalse> getRO(
            OffsetDateTime start, OffsetDateTime end,
            String input1,
            String input2
    ){
        OffsetDateTime[] times = timeCorrection(start, end);
        start = times[0];
        end = times[1];
        input1 = correctionOfResourceName(input1);
        input2 = correctionOfOperation(input2);

        return repo.getRO(start, end, input1, input2);
    }
    public Long getROCount(
            OffsetDateTime start, OffsetDateTime end,
            String input1,
            String input2
    ){
        OffsetDateTime[] times = timeCorrection(start, end);
        start = times[0];
        end = times[1];
        input1 = correctionOfResourceName(input1);
        input2 = correctionOfOperation(input2);

        return repo.getROCount(start, end, input1, input2);
    }

    public List<SystemLevelFalse> getRC(
            OffsetDateTime start, OffsetDateTime end,
            String input1,
            String input2
    ){
        OffsetDateTime[] times = timeCorrection(start, end);
        start = times[0];
        end = times[1];
        input1 = correctionOfResourceName(input1);
        input2 = correctionOfClientIp(input2);

        return repo.getRC(start, end, input1, input2);
    }
    public Long getRCCount(
            OffsetDateTime start, OffsetDateTime end,
            String input1,
            String input2
    ){
        OffsetDateTime[] times = timeCorrection(start, end);
        start = times[0];
        end = times[1];
        input1 = correctionOfResourceName(input1);
        input2 = correctionOfClientIp(input2);

        return repo.getRCCount(start, end, input1, input2);
    }

    public List<SystemLevelFalse> getOC(
            OffsetDateTime start, OffsetDateTime end,
            String input1,
            String input2
    ){
        OffsetDateTime[] times = timeCorrection(start, end);
        start = times[0];
        end = times[1];
        input1 = correctionOfOperation(input1);
        input2 = correctionOfClientIp(input2);

        return repo.getOC(start, end, input1, input2);
    }
    public Long getOCCount(
            OffsetDateTime start, OffsetDateTime end,
            String input1,
            String input2
    ){
        OffsetDateTime[] times = timeCorrection(start, end);
        start = times[0];
        end = times[1];
        input1 = correctionOfOperation(input1);
        input2 = correctionOfClientIp(input2);

        return repo.getOCCount(start, end, input1, input2);
    }

    /**시간 + 3개 컬럼으로 조회*/
    public List<SystemLevelFalse> getPRO(
            OffsetDateTime start, OffsetDateTime end,
            String input1,
            String input2,
            String input3
    ){
        OffsetDateTime[] times = timeCorrection(start, end);
        start = times[0];
        end = times[1];
        input1 = correctionOfPrincipal(input1);
        input2 = correctionOfResourceName(input2);
        input3 = correctionOfOperation(input3);

        return repo.getPRO(start, end, input1, input2, input3);
    }
    public Long getPROCount(
            OffsetDateTime start, OffsetDateTime end,
            String input1,
            String input2,
            String input3
    ){
        OffsetDateTime[] times = timeCorrection(start, end);
        start = times[0];
        end = times[1];
        input1 = correctionOfPrincipal(input1);
        input2 = correctionOfResourceName(input2);
        input3 = correctionOfOperation(input3);

        return repo.getPROCount(start, end, input1, input2, input3);
    }

    public List<SystemLevelFalse> getPRC(
            OffsetDateTime start, OffsetDateTime end,
            String input1,
            String input2,
            String input3
    ){
        OffsetDateTime[] times = timeCorrection(start, end);
        start = times[0];
        end = times[1];
        input1 = correctionOfPrincipal(input1);
        input2 = correctionOfResourceName(input2);
        input3 = correctionOfClientIp(input3);

        return repo.getPRC(start, end, input1, input2, input3);
    }
    public Long getPRCCount(
            OffsetDateTime start, OffsetDateTime end,
            String input1,
            String input2,
            String input3
    ){
        OffsetDateTime[] times = timeCorrection(start, end);
        start = times[0];
        end = times[1];
        input1 = correctionOfPrincipal(input1);
        input2 = correctionOfResourceName(input2);
        input3 = correctionOfClientIp(input3);

        return repo.getPRCCount(start, end, input1, input2, input3);
    }

    public List<SystemLevelFalse> getPOC(
            OffsetDateTime start, OffsetDateTime end,
            String input1,
            String input2,
            String input3
    ){
        OffsetDateTime[] times = timeCorrection(start, end);
        start = times[0];
        end = times[1];
        input1 = correctionOfPrincipal(input1);
        input2 = correctionOfOperation(input2);
        input3 = correctionOfClientIp(input3);

        return repo.getPOC(start, end, input1, input2, input3);
    }
    public Long getPOCCount(
            OffsetDateTime start, OffsetDateTime end,
            String input1,
            String input2,
            String input3
    ){
        OffsetDateTime[] times = timeCorrection(start, end);
        start = times[0];
        end = times[1];
        input1 = correctionOfPrincipal(input1);
        input2 = correctionOfOperation(input2);
        input3 = correctionOfClientIp(input3);

        return repo.getPOCCount(start, end, input1, input2, input3);
    }

    public List<SystemLevelFalse> getROC(
            OffsetDateTime start, OffsetDateTime end,
            String input1,
            String input2,
            String input3
    ){
        OffsetDateTime[] times = timeCorrection(start, end);
        start = times[0];
        end = times[1];
        input1 = correctionOfResourceName(input1);
        input2 = correctionOfOperation(input2);
        input3 = correctionOfClientIp(input3);

        return repo.getROC(start, end, input1, input2, input3);
    }
    public Long getROCCount(
            OffsetDateTime start, OffsetDateTime end,
            String input1,
            String input2,
            String input3
    ){
        OffsetDateTime[] times = timeCorrection(start, end);
        start = times[0];
        end = times[1];
        input1 = correctionOfResourceName(input1);
        input2 = correctionOfOperation(input2);
        input3 = correctionOfClientIp(input3);

        return repo.getROCCount(start, end, input1, input2, input3);
    }

    public List<SystemLevelFalse> getPROC(
            OffsetDateTime start, OffsetDateTime end,
            String input1,
            String input2,
            String input3,
            String input4
    ){
        OffsetDateTime[] times = timeCorrection(start, end);
        start = times[0];
        end = times[1];
        input1 = correctionOfPrincipal(input1);
        input2 = correctionOfResourceName(input2);
        input3 = correctionOfOperation(input3);
        input4 = correctionOfClientIp(input4);

        return repo.getPROC(start, end, input1, input2, input3, input4);
    }
    public Long getPROCCount(
            OffsetDateTime start, OffsetDateTime end,
            String input1,
            String input2,
            String input3,
            String input4
    ){
        OffsetDateTime[] times = timeCorrection(start, end);
        start = times[0];
        end = times[1];
        input1 = correctionOfPrincipal(input1);
        input2 = correctionOfResourceName(input2);
        input3 = correctionOfOperation(input3);
        input4 = correctionOfClientIp(input4);

        return repo.getPROCCount(start, end, input1, input2, input3, input4);
    }

}
