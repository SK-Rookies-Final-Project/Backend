package com.finalproject.springbackend.db.controller;

import com.finalproject.springbackend.db.entity.ResourceLevelFalse;
import com.finalproject.springbackend.db.service.ResourceLevelFalseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/db/resource_level_false")
@RequiredArgsConstructor
public class GetResourceLevelFalseController {

    private final ResourceLevelFalseService rlfService;

    /**
     * 시간 기준 resource-level-false 테이블의 레코드 반환
     * http://localhost:8080/api/db/resource_level_false?start=2025-09-12T00:12:43Z&end=2025-09-12T12:34:56Z로
     * GET 요청을 보내면 start과 end 사이의 레코드 반환
     * start 데이터 형식: yyyy-mm-ddThh:mm:ssZ
     * end 데이터 형식: yyyy-mm-ddThh:mm:ssZ
     *  yyyy-mm-dd 뒤에 T, hh:mm:ss 뒤에 Z를 붙여야 함
     */

    /** 전체 레코드 전부 반환*/
    @GetMapping
    public ResponseEntity<List<ResourceLevelFalse>> getAll(){
        List<ResourceLevelFalse> rlfAll = rlfService.getAll();
        return ResponseEntity.ok(rlfAll);
    }
    /** 전체 레코드 갯수 */
    @GetMapping("/count")
    public ResponseEntity<Long> getCount(){
        return ResponseEntity.ok(rlfService.getCount());
    }
    /**특정 원소 기준으로 일치하는 모든 레코드 반환 및 해당 갯수 반환 */
    //특정 principal 원소가 일치하는 레코드만 반환
    @GetMapping(params = {"principal"})
    public ResponseEntity<List<ResourceLevelFalse>> getPrincipalList(
            @RequestParam String principal){
        return ResponseEntity.ok(rlfService.getPrincipal(principal));
    }
    //특정 principal 원소가 일치하는 레코드 갯수 반환
    @GetMapping(value = "/count", params = {"principal"})
    public ResponseEntity<Long> getPrincipalListCount(
            @RequestParam String principal) {
        Long count = rlfService.getPrincipalCount(principal);
        return ResponseEntity.ok(count);
    }

    //resource_name
    @GetMapping(params={"resource_name"})
    public ResponseEntity<List<ResourceLevelFalse>> getResourceNameList(
            @RequestParam("resource_name") String resourceName
    ){
        return ResponseEntity.ok(rlfService.getResourceName(resourceName));
    }
    @GetMapping(value = "/count", params = {"resource_name"})
    public ResponseEntity<Long> getResourceNameListCount(
            @RequestParam("resource_name") String resourceName) {
        Long count = rlfService.getResourceNameCount(resourceName);
        return ResponseEntity.ok(count);
    }
    //operation
    @GetMapping(params={"operation"})
    public ResponseEntity<List<ResourceLevelFalse>> getOperationList(
            @RequestParam String operation
    ){
        return ResponseEntity.ok(rlfService.getOperation(operation));
    }
    @GetMapping(value = "/count", params = {"operation"})
    public ResponseEntity<Long> getOperationListCount(
            @RequestParam String operation) {
        Long count = rlfService.getOperationCount(operation);
        return ResponseEntity.ok(count);
    }
    //client_ip
    @GetMapping(params={"client_ip"})
    public ResponseEntity<List<ResourceLevelFalse>> getClientIpList(
            @RequestParam("client_ip") String clientIp
    ){
        return ResponseEntity.ok(rlfService.getClientIp(clientIp));
    }
    @GetMapping(value = "/count", params = {"client_ip"})
    public ResponseEntity<Long> getClientIpListCount(
            @RequestParam("client_ip") String clientIp) {
        Long count = rlfService.getClientIpCount(clientIp);
        return ResponseEntity.ok(count);
    }

    /**
     * 시작시각, 종료시각, 특정 컬럼(들)
     * 시작시각, 종료시각, 특정 컬럼(들)의 갯수
     */
    /**
     * @param start         시작 시각
     * @param end           종료 시각 (null 가능: 서비스에서 현재 시간으로 보정해줌)
     * @return ResponseEntity<List<ResourceLevelFalse>>
     *                      200 OK - 결과 리스트(없으면 빈 리스트),
     *                      400 Bad Request - 파라미터 검증 실패
     */
    @GetMapping(params = {"start"})
    public ResponseEntity<List<ResourceLevelFalse>> getTimes(
            @RequestParam OffsetDateTime start,
            @RequestParam(required = false) OffsetDateTime end
    ){
        List<ResourceLevelFalse> rlf = rlfService.getTimesOnly(start, end);

        return ResponseEntity.ok(rlf);
    }
    @GetMapping(value = "/count", params = {"start"})
    public ResponseEntity<Long> getTimesOnlyCount(
            @RequestParam OffsetDateTime start,
            @RequestParam(required = false) OffsetDateTime end
    ) {
        Long count = (long) rlfService.getTimesOnlyCount(start, end);
        return ResponseEntity.ok(count);
    }

    /**
     * @param start         시작 시각
     * @param end           종료 시각 (null 가능: 서비스에서 현재 시간으로 보정해줌)
     * @param principal     principal(검색 할 유저)값
     * @return ResponseEntity<List<ResourceLevelFalse>>
     *                      200 OK - 결과 리스트(없으면 빈 리스트),
     *                      400 Bad Request - 파라미터 검증 실패
     */
    @GetMapping(params = {"start", "principal"})
    public ResponseEntity<List<ResourceLevelFalse>> getTimeAndPrincipal(
            @RequestParam OffsetDateTime start, @RequestParam(required = false) OffsetDateTime end,
            @RequestParam String principal
    ){
        List<ResourceLevelFalse> rlf = rlfService.getTimeAndPrincipal(
                start, end, principal
        );
        return ResponseEntity.ok(rlf);
    }
    @GetMapping(value = "/count", params = {"start", "principal"})
    public ResponseEntity<Long> getTimeAndPrincipalCount(
            @RequestParam OffsetDateTime start,
            @RequestParam(required = false) OffsetDateTime end,
            @RequestParam String principal
    ) {
        Long count = (long) rlfService.getTimeAndPrincipalCount(start, end,principal);
        return ResponseEntity.ok(count);
    }


    /**
     * @param start         시작 시각
     * @param end           종료 시각 (null 가능: 서비스에서 현재 시간으로 보정해줌)
     * @param resourceName  resourceName(비인가 접근이 생긴 리소스 이름)
     * @return ResponseEntity<List<ResourceLevelFalse>>
     *                      200 OK - 결과 리스트(없으면 빈 리스트),
     *                      400 Bad Request - 파라미터 검증 실패
     */
    @GetMapping(params = {"start", "resource_name"})
    public ResponseEntity<List<ResourceLevelFalse>> getTimeAndResourceName(
            @RequestParam OffsetDateTime start, @RequestParam(required = false) OffsetDateTime end,
            @RequestParam("resource_name") String resourceName
    ){
        List<ResourceLevelFalse> rlf = rlfService.getTimeAndResourceName(
                start, end, resourceName
        );
        return ResponseEntity.ok(rlf);
    }
    @GetMapping(value = "/count", params = {"start", "resource_name"})
    public ResponseEntity<Long> getTimeAndResourceNameCount(
            @RequestParam OffsetDateTime start,
            @RequestParam(required = false) OffsetDateTime end,
            @RequestParam("resource_name") String resourceName
    ) {
        Long count = (long) rlfService.getTimeAndResourceNameCount(start, end, resourceName);
        return ResponseEntity.ok(count);
    }

    /**
     * @param start         시작 시각
     * @param end           종료 시각 (null 가능: 서비스에서 현재 시간으로 보정해줌)
     * @param operation     어떤 권한으로 접근했는지
     * @return ResponseEntity<List<ResourceLevelFalse>>
     *                      200 OK - 결과 리스트(없으면 빈 리스트),
     *                      400 Bad Request - 파라미터 검증 실패
     */
    @GetMapping(params = {"start", "operation"})
    public ResponseEntity<List<ResourceLevelFalse>> getTimeAndOperation(
            @RequestParam OffsetDateTime start, @RequestParam(required = false) OffsetDateTime end,
            @RequestParam String operation
    ){
        List<ResourceLevelFalse> rlf = rlfService.getTimeAndOperation(
                start, end, operation
        );
        return ResponseEntity.ok(rlf);
    }
    @GetMapping(value = "/count", params = {"start", "operation"})
    public ResponseEntity<Long> getTimeAndOperationCount(
            @RequestParam OffsetDateTime start,
            @RequestParam(required = false) OffsetDateTime end,
            @RequestParam String operation
    ) {
        Long count = (long) rlfService.getTimeAndOperationCount(start, end, operation);
        return ResponseEntity.ok(count);
    }
    /**
     * @param start         시작 시각
     * @param end           종료 시각 (null 가능: 서비스에서 현재 시간으로 보정해줌)
     * @param clientIp      어떤 IP로 비인가 접근을 했는지
     * @return ResponseEntity<List<ResourceLevelFalse>>
     *                      200 OK - 결과 리스트(없으면 빈 리스트),
     *                      400 Bad Request - 파라미터 검증 실패
     */
    @GetMapping(params = {"start", "client_ip"})
    public ResponseEntity<List<ResourceLevelFalse>> getTimeAndClientIp(
            @RequestParam OffsetDateTime start, @RequestParam(required = false) OffsetDateTime end,
            @RequestParam("client_ip") String clientIp
    ){
        List<ResourceLevelFalse> rlf = rlfService.getTimeAndClientIp(
                start, end, clientIp
        );
        return ResponseEntity.ok(rlf);
    }
    @GetMapping(value = "/count", params = {"start", "client_ip"})
    public ResponseEntity<Long> getTimeAndClientIpCount(
            @RequestParam OffsetDateTime start,
            @RequestParam(required = false) OffsetDateTime end,
            @RequestParam("client_ip") String clientIp
    ) {
        Long count = (long) rlfService.getTimeAndClientIpCount(start, end, clientIp);
        return ResponseEntity.ok(count);
    }

    /**
     * @param start         시작 시각
     * @param end           종료 시각 (null 가능: 서비스에서 현재 시간으로 보정해줌)
     * @param principal     principal(검색 할 유저)값
     * @param resourceName  resourceName(비인가 접근이 생긴 리소스 이름)
     * @return ResponseEntity<List<ResourceLevelFalse>>
     *                      200 OK - 결과 리스트(없으면 빈 리스트),
     *                      400 Bad Request - 파라미터 검증 실패
     */
    @GetMapping(params = {"start", "principal", "resource_name"})
    public ResponseEntity<List<ResourceLevelFalse>> getTimeAndPR(
            @RequestParam OffsetDateTime start, @RequestParam(required = false) OffsetDateTime end,
            @RequestParam String principal, @RequestParam("resource_name") String resourceName
    ){
        List<ResourceLevelFalse> rlf = rlfService.getTimeAndPR(
                start, end, principal, resourceName
        );
        return ResponseEntity.ok(rlf);
    }
    @GetMapping(value = "/count", params = {"start", "principal","resource_name"})
    public ResponseEntity<Long> getTimeAndPRCount(
            @RequestParam OffsetDateTime start,
            @RequestParam(required = false) OffsetDateTime end,
            @RequestParam String principal,
            @RequestParam("resource_name") String resourceName
    ) {
        Long count = (long) rlfService.getTimeAndPRCount(start, end, principal, resourceName);
        return ResponseEntity.ok(count);
    }

    /**
     * @param start         시작 시각
     * @param end           종료 시각 (null 가능: 서비스에서 현재 시간으로 보정해줌)
     * @param principal     principal(검색 할 유저)값
     * @param operation     어떤 권한으로 접근했는지
     * @return ResponseEntity<List<ResourceLevelFalse>>
     *                      200 OK - 결과 리스트(없으면 빈 리스트),
     *                      400 Bad Request - 파라미터 검증 실패
     */
    @GetMapping(params = {"start", "principal", "operation"})
    public ResponseEntity<List<ResourceLevelFalse>> getTimeAndPO(
            @RequestParam OffsetDateTime start, @RequestParam(required = false) OffsetDateTime end,
            @RequestParam String principal, @RequestParam String operation
    ){
        List<ResourceLevelFalse> rlf = rlfService.getTimeAndPO(
                start, end, principal, operation
        );
        return ResponseEntity.ok(rlf);
    }
    @GetMapping(value = "/count", params = {"start", "principal","operation"})
    public ResponseEntity<Long> getTimeAndPOCount(
            @RequestParam OffsetDateTime start, @RequestParam(required = false) OffsetDateTime end,
            @RequestParam String principal, @RequestParam String operation
    ) {
        Long count = (long) rlfService.getTimeAndPOCount(start, end, principal, operation);
        return ResponseEntity.ok(count);
    }


    /**
     * @param start         시작 시각
     * @param end           종료 시각 (null 가능: 서비스에서 현재 시간으로 보정해줌)
     * @param principal     principal(검색 할 유저)값
     * @param clientIp      어떤 IP로 비인가 접근을 했는지
     * @return ResponseEntity<List<ResourceLevelFalse>>
     *                      200 OK - 결과 리스트(없으면 빈 리스트),
     *                      400 Bad Request - 파라미터 검증 실패
     */
    @GetMapping(params = {"start", "principal", "client_ip"})
    public ResponseEntity<List<ResourceLevelFalse>> getTimeAndPC(
            @RequestParam OffsetDateTime start, @RequestParam(required = false) OffsetDateTime end,
            @RequestParam String principal, @RequestParam("client_ip") String clientIp
    ){
        List<ResourceLevelFalse> rlf = rlfService.getTimeAndPC(
                start, end, principal, clientIp
        );
        return ResponseEntity.ok(rlf);
    }
    @GetMapping(value = "/count", params = {"start", "principal","client_ip"})
    public ResponseEntity<Long> getTimeAndPCCount(
            @RequestParam OffsetDateTime start, @RequestParam(required = false) OffsetDateTime end,
            @RequestParam String principal, @RequestParam("client_ip") String clientIp
    ) {
        Long count = (long) rlfService.getTimeAndPCCount(start, end, principal, clientIp);
        return ResponseEntity.ok(count);
    }

    /**
     * @param start         시작 시각
     * @param end           종료 시각 (null 가능: 서비스에서 현재 시간으로 보정해줌)
     * @param resourceName  resourceName(비인가 접근이 생긴 리소스 이름)
     * @param operation     어떤 권한으로 접근했는지
     * @return ResponseEntity<List<ResourceLevelFalse>>
     *                      200 OK - 결과 리스트(없으면 빈 리스트),
     *                      400 Bad Request - 파라미터 검증 실패
     */
    @GetMapping(params = {"start", "resource_name", "operation"})
    public ResponseEntity<List<ResourceLevelFalse>> getTimeAndRO(
            @RequestParam OffsetDateTime start, @RequestParam(required = false) OffsetDateTime end,
            @RequestParam("resource_name") String resourceName, @RequestParam String operation
    ){
        List<ResourceLevelFalse> rlf = rlfService.getTimeAndRO(
                start, end, resourceName, operation
        );
        return ResponseEntity.ok(rlf);
    }
    @GetMapping(value = "/count", params = {"start", "resource_name", "operation"})
    public ResponseEntity<Long> getTimeAndROCount(
            @RequestParam OffsetDateTime start, @RequestParam(required = false) OffsetDateTime end,
            @RequestParam("resource_name") String resourceName, @RequestParam String operation
    ) {
        Long count = (long) rlfService.getTimeAndROCount(start, end, resourceName, operation);
        return ResponseEntity.ok(count);
    }

    /**
     * @param start         시작 시각
     * @param end           종료 시각 (null 가능: 서비스에서 현재 시간으로 보정해줌)
     * @param resourceName  resourceName(비인가 접근이 생긴 리소스 이름)
     * @param clientIp      어떤 IP로 비인가 접근을 했는지
     * @return ResponseEntity<List<ResourceLevelFalse>>
     *                      200 OK - 결과 리스트(없으면 빈 리스트),
     *                      400 Bad Request - 파라미터 검증 실패
     */
    @GetMapping(params = {"start", "resource_name", "client_ip"})
    public ResponseEntity<List<ResourceLevelFalse>> getTimeAndRC(
            @RequestParam OffsetDateTime start, @RequestParam(required = false) OffsetDateTime end,
            @RequestParam("resource_name") String resourceName, @RequestParam("client_ip") String clientIp
    ){
        List<ResourceLevelFalse> rlf = rlfService.getTimeAndRC(
                start, end, resourceName, clientIp
        );
        return ResponseEntity.ok(rlf);
    }
    @GetMapping(value = "/count", params = {"start", "resource_name", "client_ip"})
    public ResponseEntity<Long> getTimeAndRCCount(
            @RequestParam OffsetDateTime start, @RequestParam(required = false) OffsetDateTime end,
            @RequestParam("resource_name") String resourceName, @RequestParam("client_ip") String clientIp
    ) {
        Long count = (long) rlfService.getTimeAndRCCount(start, end, resourceName, clientIp);
        return ResponseEntity.ok(count);
    }

    /**
     * @param start         시작 시각
     * @param end           종료 시각 (null 가능: 서비스에서 현재 시간으로 보정해줌)
     * @param operation     어떤 권한으로 접근했는지
     * @param clientIp      어떤 IP로 비인가 접근을 했는지
     * @return ResponseEntity<List<ResourceLevelFalse>>
     *                      200 OK - 결과 리스트(없으면 빈 리스트),
     *                      400 Bad Request - 파라미터 검증 실패
     */
    @GetMapping(params = {"start", "operation", "client_ip"})
    public ResponseEntity<List<ResourceLevelFalse>> getTimeAndOC(
            @RequestParam OffsetDateTime start, @RequestParam(required = false) OffsetDateTime end,
            @RequestParam String operation, @RequestParam("client_ip") String clientIp
    ){
        List<ResourceLevelFalse> rlf = rlfService.getTimeAndOC(
                start, end, operation, clientIp
        );
        return ResponseEntity.ok(rlf);
    }
    @GetMapping(value = "/count", params = {"start", "operation", "client_ip"})
    public ResponseEntity<Long> getTimeAndOCCount(
            @RequestParam OffsetDateTime start, @RequestParam(required = false) OffsetDateTime end,
            @RequestParam String operation, @RequestParam("client_ip") String clientIp
    ) {
        Long count = (long) rlfService.getTimeAndOCCount(start, end, operation, clientIp);
        return ResponseEntity.ok(count);
    }


    /**
     * @param start         시작 시각
     * @param end           종료 시각 (null 가능: 서비스에서 현재 시간으로 보정해줌)
     * @param principal     principal(검색 할 유저)값
     * @param resourceName  resourceName(비인가 접근이 생긴 리소스 이름)
     * @param operation     어떤 권한으로 접근했는지
     * @return ResponseEntity<List<ResourceLevelFalse>>
     *                      200 OK - 결과 리스트(없으면 빈 리스트),
     *                      400 Bad Request - 파라미터 검증 실패
     */
    @GetMapping(params = {"start", "principal", "resource_name", "operation"})
    public ResponseEntity<List<ResourceLevelFalse>> getTimeAndPRO(
            @RequestParam OffsetDateTime start, @RequestParam(required = false) OffsetDateTime end,
            @RequestParam String principal,
            @RequestParam("resource_name") String resourceName,
            @RequestParam String operation
    ){
        List<ResourceLevelFalse> rlf = rlfService.getTimeAndPRO(
                start, end, principal, resourceName, operation);
        return ResponseEntity.ok(rlf);
    }
    @GetMapping(value = "/count", params = {"start", "principal", "resource_name", "operation"})
    public ResponseEntity<Long> getTimeAndPROCount(
            @RequestParam OffsetDateTime start, @RequestParam(required = false) OffsetDateTime end,
            @RequestParam String principal,
            @RequestParam("resource_name") String resourceName,
            @RequestParam String operation
    ) {
        Long count = (long) rlfService.getTimeAndPROCount(start, end, principal, resourceName, operation);
        return ResponseEntity.ok(count);
    }

    /**
     * @param start         시작 시각
     * @param end           종료 시각 (null 가능: 서비스에서 현재 시간으로 보정해줌)
     * @param principal     principal(검색 할 유저)값
     * @param resourceName  resourceName(비인가 접근이 생긴 리소스 이름)
     * @param clientIp      어떤 IP로 비인가 접근을 했는지
     * @return ResponseEntity<List<ResourceLevelFalse>>
     *                      200 OK - 결과 리스트(없으면 빈 리스트),
     *                      400 Bad Request - 파라미터 검증 실패
     */
    @GetMapping(params = {"start", "principal", "resource_name", "client_ip"})
    public ResponseEntity<List<ResourceLevelFalse>> getTimeAndPRC(
            @RequestParam OffsetDateTime start, @RequestParam(required = false) OffsetDateTime end,
            @RequestParam String principal,
            @RequestParam("resource_name") String resourceName,
            @RequestParam("client_ip") String clientIp
    ){
        List<ResourceLevelFalse> rlf = rlfService.getTimeAndPRC(
                start, end, principal, resourceName, clientIp);
        return ResponseEntity.ok(rlf);
    }
    @GetMapping(value = "/count", params = {"start", "principal", "resource_name", "client_ip"})
    public ResponseEntity<Long> getTimeAndPRCCount(
            @RequestParam OffsetDateTime start, @RequestParam(required = false) OffsetDateTime end,
            @RequestParam String principal,
            @RequestParam("resource_name") String resourceName,
            @RequestParam("client_ip") String clientIp
    ) {
        Long count = (long) rlfService.getTimeAndPRCCount(start, end, principal, resourceName, clientIp);
        return ResponseEntity.ok(count);
    }

    /**
     * @param start         시작 시각
     * @param end           종료 시각 (null 가능: 서비스에서 현재 시간으로 보정해줌)
     * @param principal     principal(검색 할 유저)값
     * @param operation     어떤 권한으로 접근했는지
     * @param clientIp      어떤 IP로 비인가 접근을 했는지
     * @return ResponseEntity<List<ResourceLevelFalse>>
     *                      200 OK - 결과 리스트(없으면 빈 리스트),
     *                      400 Bad Request - 파라미터 검증 실패
     */
    @GetMapping(params = {"start", "principal", "operation", "client_ip"})
    public ResponseEntity<List<ResourceLevelFalse>> getTimeAndPOC(
            @RequestParam OffsetDateTime start, @RequestParam(required = false) OffsetDateTime end,
            @RequestParam String principal,
            @RequestParam String operation,
            @RequestParam("client_ip") String clientIp
    ){
        List<ResourceLevelFalse> rlf = rlfService.getTimeAndPOC(
                start, end, principal, operation, clientIp);
        return ResponseEntity.ok(rlf);
    }
    @GetMapping(value = "/count", params = {"start", "principal", "operation", "client_ip"})
    public ResponseEntity<Long> getTimeAndPOCCount(
            @RequestParam OffsetDateTime start, @RequestParam(required = false) OffsetDateTime end,
            @RequestParam String principal,
            @RequestParam String operation,
            @RequestParam("client_ip") String clientIp
    ) {
        Long count = (long) rlfService.getTimeAndPOCCount(start, end, principal, operation, clientIp);
        return ResponseEntity.ok(count);
    }

    /**
     * @param start         시작 시각
     * @param end           종료 시각 (null 가능: 서비스에서 현재 시간으로 보정해줌)
     * @param resourceName  resourceName(비인가 접근이 생긴 리소스 이름)
     * @param operation     어떤 권한으로 접근했는지
     * @param clientIp      어떤 IP로 비인가 접근을 했는지
     * @return ResponseEntity<List<ResourceLevelFalse>>
     *                      200 OK - 결과 리스트(없으면 빈 리스트),
     *                      400 Bad Request - 파라미터 검증 실패
     */
    @GetMapping(params = {"start", "resource_name", "operation", "client_ip"})
    public ResponseEntity<List<ResourceLevelFalse>> getTimeAndROC(
            @RequestParam OffsetDateTime start, @RequestParam(required = false) OffsetDateTime end,
            @RequestParam("resource_name") String resourceName,
            @RequestParam String operation,
            @RequestParam("client_ip") String clientIp
    ){
        List<ResourceLevelFalse> rlf = rlfService.getTimeAndROC(
                start, end, resourceName, operation, clientIp);
        return ResponseEntity.ok(rlf);
    }
    @GetMapping(value = "/count", params = {"start", "resource_name", "operation", "client_ip"})
    public ResponseEntity<Long> getTimeAndROCCount(
            @RequestParam OffsetDateTime start, @RequestParam(required = false) OffsetDateTime end,
            @RequestParam("resource_name") String resourceName,
            @RequestParam String operation,
            @RequestParam("client_ip") String clientIp
    ) {
        Long count = (long) rlfService.getTimeAndROCCount(start, end, resourceName, operation, clientIp);
        return ResponseEntity.ok(count);
    }
    /**
     * @param start         시작 시각
     * @param end           종료 시각 (null 가능: 서비스에서 현재 시간으로 보정해줌)
     * @param principal     principal(검색 할 유저)값
     * @param resourceName  resourceName(비인가 접근이 생긴 리소스 이름)
     * @param operation     어떤 권한으로 접근했는지
     * @param clientIp      어떤 IP로 비인가 접근을 했는지
     * @return ResponseEntity<List<ResourceLevelFalse>>
     *                      200 OK - 결과 리스트(없으면 빈 리스트),
     *                      400 Bad Request - 파라미터 검증 실패
     */
    @GetMapping(params = {"start", "principal", "resource_name","operation","client_ip"})
    public ResponseEntity<List<ResourceLevelFalse>> getTimeAndPROC(
            @RequestParam OffsetDateTime start, @RequestParam(required = false) OffsetDateTime end,
            @RequestParam String principal,
            @RequestParam("resource_name") String resourceName,
            @RequestParam String operation,
            @RequestParam("client_ip") String clientIp
    ){
        List<ResourceLevelFalse> rlf = rlfService.getTimeAndPROC(
                start, end, principal, resourceName, operation, clientIp);
        return ResponseEntity.ok(rlf);
    }
    @GetMapping(value = "/count", params = {"start", "principal", "resource_name", "operation", "client_ip"})
    public ResponseEntity<Long> getTimeAndPROCCount(
            @RequestParam OffsetDateTime start, @RequestParam(required = false) OffsetDateTime end,
            @RequestParam String principal,
            @RequestParam("resource_name") String resourceName,
            @RequestParam String operation,
            @RequestParam("client_ip") String clientIp
    ) {
        Long count = (long) rlfService.getTimeAndPROCCount(start, end, principal, resourceName, operation, clientIp);
        return ResponseEntity.ok(count);
    }


}
