package com.finalproject.springbackend.db.service;

import com.finalproject.springbackend.db.entity.ResourceLevelFalse;
import com.finalproject.springbackend.db.repository.ResourceLevelFalseRepository;
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
public class ResourceLevelFalseService {

    private final ResourceLevelFalseRepository repo;
    /*
     * boolean str.isBlank() : str.length()==0 || str에 오직 모든 whitespace만 있으면 true
     * String str.strip() : 앞 뒤 whiteSpace 제거
     */

    /** 전체 레코드 가져오기 */
    @Transactional(readOnly = true)
    public List<ResourceLevelFalse> getAll(){
        return repo.findAll();
    }

    /** 전체 레코드 갯수 */
    @Transactional(readOnly = true)
    public Long getCount(){
        return repo.count();
    }

    /** 각각의 컬럼 별 리스트 및 갯수 조회 */
    public List<ResourceLevelFalse> getPrincipal(String principal){
        principal = correctionOfPrincipal(principal);
        List<ResourceLevelFalse> principalList = repo.findByPrincipal(principal);
        if (principalList.isEmpty()){
            throw new IllegalArgumentException(principal+" 유저는 비인가 접근 기록이 존재하지 않습니다. ");
        }

        return principalList;
    }
    public long getPrincipalCount(String principal){
        principal = correctionOfPrincipal(principal);
        return repo.countByPrincipal(principal);
    }

    public List<ResourceLevelFalse> getResourceName(String resourceName){
        resourceName = correctionOfResourceName(resourceName);
        List<ResourceLevelFalse> resourceNameList = repo.findByResourceName(resourceName);
        if(resourceNameList.isEmpty()) {
            throw new IllegalArgumentException(resourceName + " 리소스로 비인가 접근 기록이 존재하지 않습니다. ");
        }
        return resourceNameList;
    }
    public long getResourceNameCount(String resourceName){
        resourceName = correctionOfResourceName(resourceName);
        return repo.countByResourceName(resourceName);
    }

    public List<ResourceLevelFalse> getOperation(String operation){
        operation = correctionOfOperation(operation);
        List<ResourceLevelFalse> operationList = repo.findByOperation(operation);
        if(operationList.isEmpty()){
            throw new IllegalArgumentException(operation + " 권한으로 비인가 접근 기록이 존재하지 않습니다");
        }
        return operationList;
    }
    public long getOperationCount(String operation) {
        operation = correctionOfOperation(operation);
        return repo.countByOperation(operation);
    }

    public List<ResourceLevelFalse> getClientIp(String clientIp){
        clientIp = correctionOfClientIp(clientIp);
        List<ResourceLevelFalse> clientIpList = repo.findByClientIp(clientIp);
        if(clientIpList.isEmpty()){
            throw new IllegalArgumentException(clientIp + "에서 비인가 접근 기록이 존재하지 않습니다. ");
        }
        return clientIpList;
    }
    public long getClientIpCount(String clientIp){
        clientIp = correctionOfClientIp(clientIp);
        return repo.countByClientIp(clientIp);
    }


    /**
     * 파라미터 보정 메서드
     */
    //start, end 전체 보정 (한국 시간대 처리)
    private OffsetDateTime[] timesCorrection(OffsetDateTime start, OffsetDateTime end){
        start = ifStartIsNull(start);
        end = ifEndIsNull(end);
        
        // 사용자 입력을 한국 시간대로 해석
        start = TimeZoneUtil.interpretAsKST(start);
        end = TimeZoneUtil.interpretAsKST(end);
        
        log.info("🕐 시간 보정 완료 - 시작: {}, 종료: {}", 
            TimeZoneUtil.formatForDebug("시작", start),
            TimeZoneUtil.formatForDebug("종료", end));
        
        return ifStartTimeAfterEndTime(start, end);
    }
    //start보다 end 시간이 더 이후일 때
    private OffsetDateTime[] ifStartTimeAfterEndTime(OffsetDateTime start, OffsetDateTime end){
        if (start.isAfter(end)){
            OffsetDateTime tmp = start;
            start = end;
            end = tmp;
        }
        return new OffsetDateTime[]{start, end};
    }

    //null 값일 경우 보정 (한국 시간 기준)
    private OffsetDateTime ifEndIsNull(OffsetDateTime time){
        if(time==null){
            time = TimeZoneUtil.nowKST();
            return time;
        }
        else {
            return time;
        }
    }


    /**null 값일 경우 예외처리*/
    //start 가 null일경우
    private OffsetDateTime ifStartIsNull(OffsetDateTime time){
        if(time==null){
            throw new IllegalArgumentException(
                    "start 시간을 넣어주세요. \n" +
                    "형식: yyyy-MM-ddTHH:mm:ssZ"
            );
        } else {
            return time;
        }
    }

    //principal 값 보정
    private String correctionOfPrincipal(String principal){

        if(principal == null || principal.isBlank()) {
            throw new IllegalArgumentException("principal을 넣어주세요");
        }
        principal = principal.replaceAll("\\s+", "");
        if(!principal.startsWith("User:")) {
            principal = "User:"+principal;
        }
        return principal;

    }

    //resourceName 값 보정
    private String correctionOfResourceName(String resourceName) {


        if(resourceName == null || resourceName.isBlank()){
            throw new IllegalArgumentException("resourceName을 넣어주세요");
        }
        return resourceName.replaceAll("\\s+","");

    }

    private String correctionOfOperation(String operation){

        if(operation ==null || operation.isBlank()){
            throw new IllegalArgumentException("operation을 넣어주세요");
        }
        return operation.replaceAll("\\s+","");
    }

    private String correctionOfClientIp(String clientIp){
        if(clientIp == null || clientIp.isBlank()){
            throw new IllegalArgumentException("clientIp값을 넣어주세요");
        }
        return clientIp.replaceAll("\\s+", "");
    }



    /**
     * 레코드 조회 메서드
     */

    /** 시간으로만 찾기 */
    //시간 기준으로만 찾기
    public List<ResourceLevelFalse> getTimesOnly (
            OffsetDateTime start,
            OffsetDateTime end
    ){

        OffsetDateTime[] times = timesCorrection(start, end);
        start = times[0];
        end = times[1];

        log.info("🕐 시간 범위 조회 - 시작: {}, 종료: {}", start, end);
        log.info("🕐 시간 범위 조회 - 시작 UTC: {}, 종료 UTC: {}", start.toInstant(), end.toInstant());

        List<ResourceLevelFalse> result = repo.findByEventTimeKSTBetweenOrderByEventTimeKSTAsc(start, end);
        
        if (!result.isEmpty()) {
            log.info("📊 조회 결과 - 총 {}개 레코드", result.size());
            log.info("📊 첫 번째 레코드 시간: {}", result.get(0).getEventTimeKST());
            log.info("📊 마지막 레코드 시간: {}", result.get(result.size() - 1).getEventTimeKST());
        }

        return result;
    }
    //시간 기준 레코드 갯수
    public int getTimesOnlyCount(
            OffsetDateTime start,
            OffsetDateTime end
    ){
        return getTimesOnly(start, end).size();
    }
    /** 시간 + 1개의 컬럼으로 레코드 조회*/
    //시간 + principal 컬럼으로 레코드 찾기
    public List<ResourceLevelFalse> getTimeAndPrincipal(
            OffsetDateTime start,
            OffsetDateTime end,
            String principal
    ){
        OffsetDateTime[] times = timesCorrection(start, end);
        start = times[0];
        end = times[1];
        principal = correctionOfPrincipal(principal);

        return repo.findByEventTimeKSTBetweenAndPrincipalOrderByEventTimeKSTAsc(start, end, principal);
    }
    public int getTimeAndPrincipalCount(
            OffsetDateTime start,
            OffsetDateTime end,
            String principal
    ){
        return getTimeAndPrincipal(start, end, principal).size();
    }

    //시간 + resource_name 컬럼으로 레코드 찾기
    public List<ResourceLevelFalse> getTimeAndResourceName(
            OffsetDateTime start,
            OffsetDateTime end,
            String resourceName
    ) {
        OffsetDateTime[] times = timesCorrection(start, end);
        start = times[0];
        end = times[1];
        resourceName = correctionOfResourceName(resourceName);

        return repo.findByEventTimeKSTBetweenAndResourceNameOrderByEventTimeKSTAsc(
                start, end, resourceName
        );
    }
    //갯수
    public int getTimeAndResourceNameCount(
            OffsetDateTime start,
            OffsetDateTime end,
            String resourceName
    ){
        return getTimeAndResourceName(start, end, resourceName).size();
    }

    //시간 + operation
    public List<ResourceLevelFalse> getTimeAndOperation(
            OffsetDateTime start, OffsetDateTime end, String operation
    ){
        OffsetDateTime[] times = timesCorrection(start, end);
        start = times[0];
        end = times[1];

        operation = correctionOfOperation(operation);

        return repo.findByEventTimeKSTBetweenAndOperationOrderByEventTimeKSTAsc(start, end, operation);
    }
    public int getTimeAndOperationCount(
            OffsetDateTime start, OffsetDateTime end, String operation
    ){
        return getTimeAndOperation(start, end, operation).size();
    }

    //시간 + client_ip
    public List<ResourceLevelFalse> getTimeAndClientIp(
            OffsetDateTime start, OffsetDateTime end, String clientIp
    ){
        OffsetDateTime[] times = timesCorrection(start, end);
        start = times[0];
        end = times[1];

        clientIp = correctionOfClientIp(clientIp);

        return repo.findByEventTimeKSTBetweenAndClientIpOrderByEventTimeKSTAsc(start, end, clientIp);

    }
    public int getTimeAndClientIpCount(
            OffsetDateTime start, OffsetDateTime end, String clientIp
    ){
        return getTimeAndClientIp(start, end, clientIp).size();
    }

    /**시간 + 2가지 컬럼으로 찾기*/
    //시간 + principal, resource_name
    public List<ResourceLevelFalse> getTimeAndPR(
            OffsetDateTime start, OffsetDateTime end,
            String principal, String resourceName
    ){
        OffsetDateTime[] times = timesCorrection(start, end);
        start = times[0];
        end = times[1];

        principal = correctionOfPrincipal(principal);
        resourceName = correctionOfResourceName(resourceName);

        return repo.findByPR(start, end, principal, resourceName);
    }
    public int getTimeAndPRCount(
            OffsetDateTime start, OffsetDateTime end,
            String principal, String resourceName
    ){
        return getTimeAndPR(start, end, principal, resourceName).size();
    }

    //시간 + principal, operation
    public List<ResourceLevelFalse> getTimeAndPO(
            OffsetDateTime start, OffsetDateTime end,
            String principal, String operation
    ){
        OffsetDateTime[] times = timesCorrection(start, end);
        start = times[0];
        end = times[1];

        principal = correctionOfPrincipal(principal);
        operation = correctionOfOperation(operation);

        return repo.findByPO(start, end, principal, operation);
    }
    public int getTimeAndPOCount(
            OffsetDateTime start, OffsetDateTime end,
            String principal, String operation
    ){
        return getTimeAndPO(start, end, principal, operation).size();
    }

    //시간 + principal, clientIp
    public List<ResourceLevelFalse> getTimeAndPC(
            OffsetDateTime start, OffsetDateTime end,
            String principal, String clientIp
    ){
        OffsetDateTime[] times = timesCorrection(start, end);
        start = times[0];
        end = times[1];

        principal = correctionOfPrincipal(principal);
        clientIp = correctionOfClientIp(clientIp);

        return repo.findByPC(start, end, principal, clientIp);
    }
    public int getTimeAndPCCount(
            OffsetDateTime start, OffsetDateTime end,
            String principal, String clientIp
    ){
        return getTimeAndPC(start, end, principal, clientIp).size();
    }
    
    
    //시간 + resource_name, operation
    public List<ResourceLevelFalse> getTimeAndRO(
            OffsetDateTime start, OffsetDateTime end,
            String resourceName, String operation
    ){
        OffsetDateTime[] times = timesCorrection(start, end);
        start = times[0];
        end = times[1];

        resourceName = correctionOfResourceName(resourceName);
        operation = correctionOfOperation(operation);

        return repo.findByRO(start, end, resourceName, operation);
    }
    public int getTimeAndROCount(
            OffsetDateTime start, OffsetDateTime end,
            String resourceName, String operation
    ){
        return getTimeAndRO(start, end, resourceName, operation).size();
    }
    
    //시간 + resource_name + client_ip 으로 조회
    public List<ResourceLevelFalse> getTimeAndRC (
            OffsetDateTime start, OffsetDateTime end,
            String resourceName, String clientIp
    ){
        OffsetDateTime[] times = timesCorrection(start, end);
        start = times[0];
        end = times[1];

        resourceName = correctionOfResourceName(resourceName);
        clientIp = correctionOfClientIp(clientIp);
        
        return repo.findByRC(start, end, resourceName, clientIp);
    }
    public int getTimeAndRCCount(
            OffsetDateTime start, OffsetDateTime end,
            String resourceName, String clientIp
    ){
        return getTimeAndRC(start, end, resourceName, clientIp).size();
    }

    //시간 + operation, client_ip
    public List<ResourceLevelFalse> getTimeAndOC (
            OffsetDateTime start, OffsetDateTime end,
            String operation, String clientIp
    ) {
        OffsetDateTime[] times = timesCorrection(start, end);
        start = times[0];
        end = times[1];

        operation = correctionOfOperation(operation);
        clientIp = correctionOfClientIp(clientIp);

        return repo.findByOC(start, end, operation, clientIp);
    }
    public int getTimeAndOCCount(
            OffsetDateTime start, OffsetDateTime end,
            String operation, String clientIp
    ){
        return getTimeAndOC(start, end, operation, clientIp).size();
    }

    /**시간 + 3가지 컬럼으로 조회*/
    //시간 + principal, resource_name, operation
    public List<ResourceLevelFalse> getTimeAndPRO(
            OffsetDateTime start, OffsetDateTime end,
            String principal, String resourceName, String operation
    ) {
        OffsetDateTime[] times = timesCorrection(start, end);
        start = times[0];
        end = times[1];

        principal = correctionOfPrincipal(principal);
        resourceName = correctionOfResourceName(resourceName);
        operation = correctionOfOperation(operation);

        return repo.findByPRO(start, end, principal, resourceName, operation);
    }
    public int getTimeAndPROCount(
            OffsetDateTime start, OffsetDateTime end,
            String principal, String resourceName, String operation
    ){
        return getTimeAndPRO(start, end, principal, resourceName, operation).size();
    }

    //시간 + principal + resource_name + client_ip
    public List<ResourceLevelFalse> getTimeAndPRC(
            OffsetDateTime start, OffsetDateTime end,
            String principal, String resourceName, String clientIp
    ) {
        OffsetDateTime[] times = timesCorrection(start, end);
        start = times[0];
        end = times[1];

        principal = correctionOfPrincipal(principal);
        resourceName = correctionOfResourceName(resourceName);
        clientIp = correctionOfClientIp(clientIp);

        return repo.findByPRC(start, end, principal, resourceName, clientIp);
    }
    public int getTimeAndPRCCount(
            OffsetDateTime start, OffsetDateTime end,
            String principal, String resourceName, String clientIp
    ){
        return getTimeAndPRC(start, end, principal, resourceName, clientIp).size();
    }

    //시간 + principal + operation + client_ip
    public List<ResourceLevelFalse> getTimeAndPOC(
            OffsetDateTime start, OffsetDateTime end,
            String principal, String operation, String clientIp
    ) {
        OffsetDateTime[] times = timesCorrection(start, end);
        start = times[0];
        end = times[1];

        principal = correctionOfPrincipal(principal);
        operation = correctionOfOperation(operation);
        clientIp = correctionOfClientIp(clientIp);

        return repo.findByPOC(start, end, principal, operation, clientIp);
    }
    public int getTimeAndPOCCount(
            OffsetDateTime start, OffsetDateTime end,
            String principal, String operation, String clientIp
    ){
        return getTimeAndPOC(start, end, principal, operation, clientIp).size();
    }

    //시간 + resource_name + operation + client_ip
    public List<ResourceLevelFalse> getTimeAndROC(
            OffsetDateTime start, OffsetDateTime end,
            String resourceName, String operation, String clientIp
    ) {
        OffsetDateTime[] times = timesCorrection(start, end);
        start = times[0];
        end = times[1];

        resourceName = correctionOfResourceName(resourceName);
        operation = correctionOfOperation(operation);
        clientIp = correctionOfClientIp(clientIp);

        return repo.findByROC(start, end, resourceName, operation, clientIp);
    }
    public int getTimeAndROCCount(
            OffsetDateTime start, OffsetDateTime end,
            String resourceName, String operation, String clientIp
    ){
        return getTimeAndROC(start, end, resourceName, operation, clientIp).size();
    }

    /** 시간 + 4가지 컬럼으로 찾기 */
    //시간 + principal + resource_name + operation, client_ip
    public List<ResourceLevelFalse> getTimeAndPROC(
            OffsetDateTime start, OffsetDateTime end,
            String principal, String resourceName, String operation, String clientIp
    ){
        OffsetDateTime[] times = timesCorrection(start, end);
        start = times[0];
        end = times[1];

        principal = correctionOfPrincipal(principal);
        resourceName = correctionOfResourceName(resourceName);
        operation = correctionOfOperation(operation);
        clientIp = correctionOfClientIp(clientIp);

        return repo.findByPROC(start, end, principal, resourceName, operation, clientIp);
    }
    public int getTimeAndPROCCount(
            OffsetDateTime start, OffsetDateTime end,
            String principal, String resourceName, String operation, String clientIp
    ){
        return getTimeAndPROC(start, end, principal, resourceName, operation, clientIp).size();
    }


    //principal
    //resource_name
    //operation
    //client_ip
}
