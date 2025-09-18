package com.finalproject.springbackend.db.controller;

import com.finalproject.springbackend.db.entity.SystemLevelFalse;
import com.finalproject.springbackend.db.service.SystemLevelFalseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/db/system_level_false")
@RequiredArgsConstructor
public class GetSystemLevelFalseController {
    private final SystemLevelFalseService slfService;

    /**전체 레코드 반환*/
    @GetMapping
    public ResponseEntity<List<SystemLevelFalse>> getAll(){
        List<SystemLevelFalse> slfList = slfService.getAll();
        return ResponseEntity.ok(slfList);
    }
    /**전체 레코드 갯수 반환*/
    @GetMapping(value = "/count")
    public ResponseEntity<Long> getAllCount(){
        return ResponseEntity.ok(slfService.getCount());
    }

    //start, end(없으면 현재 시간으로 보정) 시간 넣으면 해당 시간 범위에 부합하는 레코드 반환
    @GetMapping(params = {"start"})
    public ResponseEntity<List<SystemLevelFalse>> getTimeOnly(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end
    ){
        List<SystemLevelFalse> slfList = slfService.getTimeOnly(start, end);
        return ResponseEntity.ok(slfList);
    }

    //start, end(없으면 현재 시간으로 보정) 시간 넣으면 해당 시간 범위에 부합하는 레코드의 갯수 반환
    @GetMapping(value = "/count", params = {"start"})
    public ResponseEntity<Long> getTimeOnlyCount(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end
    ){
        Long count = slfService.getTimeOnlyCount(start, end);
        return ResponseEntity.ok(count);
    }
    
    //하나의 필드로 레코드 반환
    @GetMapping(params = {"principal"})
    public ResponseEntity<List<SystemLevelFalse>> getPrincipal(@RequestParam(value = "principal") String principal){
        List<SystemLevelFalse> slfList = slfService.getPrincipal(principal);
        return ResponseEntity.ok(slfList);
    }
    @GetMapping(params = {"resource_name"})
    public ResponseEntity<List<SystemLevelFalse>> getResourceName(@RequestParam(value = "resource_name") String resourceName){
        List<SystemLevelFalse> slfList = slfService.getResourceName(resourceName);
        return ResponseEntity.ok(slfList);
    }
    @GetMapping(params = {"operation"})
    public ResponseEntity<List<SystemLevelFalse>> getOperation(@RequestParam(value = "operation") String operation){
        List<SystemLevelFalse> slfList = slfService.getOperation(operation);
        return ResponseEntity.ok(slfList);
    }
    @GetMapping(params = {"client_ip"})
    public ResponseEntity<List<SystemLevelFalse>> getClientIp(@RequestParam(value = "client_ip") String clientIp){
        List<SystemLevelFalse> slfList = slfService.getClientIp(clientIp);
        return ResponseEntity.ok(slfList);
    }

    /**하나의 필드에 대한 레코드의 갯수*/
    @GetMapping(value = "/count", params = {"principal"})
    public ResponseEntity<Long> getPrincipalCount(@RequestParam(value = "principal") String principal){
        Long count = slfService.getPrincipalCount(principal);
        return ResponseEntity.ok(count);
    }

    @GetMapping(value = "/count", params = {"resource_name"})
    public ResponseEntity<Long> getResourceNameCount(@RequestParam(value = "resource_name") String resourceName){
        Long count = slfService.getResourceNameCount(resourceName);
        return ResponseEntity.ok(count);
    }
    @GetMapping(value = "/count", params = {"operation"})
    public ResponseEntity<Long> getOperationCount(@RequestParam(value = "operation") String operation){
        Long count = slfService.getOperationCount(operation);
        return ResponseEntity.ok(count);
    }
    @GetMapping(value = "/count", params = {"client_ip"})
    public ResponseEntity<Long> getClientIpCount(@RequestParam(value = "client_ip") String clientIp){
        Long count = slfService.getClientIpCount(clientIp);
        return ResponseEntity.ok(count);
    }

    /**시간 + 필드 하나*/
    @GetMapping(params = {"start", "principal"})
    public ResponseEntity<List<SystemLevelFalse>> getP(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "principal") String principal
    ){
        List<SystemLevelFalse> slfList = slfService.getP(start, end, principal);
        return ResponseEntity.ok(slfList);
    }
    @GetMapping(params = {"start", "resource_name"})
    public ResponseEntity<List<SystemLevelFalse>> getR(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "resource_name") String resourceName
    ){
        List<SystemLevelFalse> slfList = slfService.getR(start, end,resourceName);
        return ResponseEntity.ok(slfList);
    }
    @GetMapping(params = {"start", "operation"})
    public ResponseEntity<List<SystemLevelFalse>> getO(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "operation") String operation
    ){
        List<SystemLevelFalse> slfList = slfService.getO(start, end, operation);
        return ResponseEntity.ok(slfList);
    }
    @GetMapping(params = {"start", "client_ip"})
    public ResponseEntity<List<SystemLevelFalse>> getC(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "client_ip") String clientIp
    ){
        List<SystemLevelFalse> slfList = slfService.getC(start, end, clientIp);
        return ResponseEntity.ok(slfList);
    }

    @GetMapping(value = "/count", params = {"start", "principal"})
    public ResponseEntity<Long> getPCount(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "principal") String principal
    ){
        Long count = slfService.getPCount(start, end, principal);
        return ResponseEntity.ok(count);
    }
    @GetMapping(value = "/count", params = {"start", "resource_name"})
    public ResponseEntity<Long> getRCount(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "resource_name") String resourceName
    ){
        Long count = slfService.getRCount(start, end, resourceName);
        return ResponseEntity.ok(count);
    }
    @GetMapping(value = "/count", params = {"start", "operation"})
    public ResponseEntity<Long> getOCount(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "operation") String operation
    ){
        Long count = slfService.getOCount(start, end, operation);
        return ResponseEntity.ok(count);
    }
    @GetMapping(value = "/count", params = {"start", "client_ip"})
    public ResponseEntity<Long> getCCount(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "client_ip") String clientIp
    ){
        Long count = slfService.getCCount(start, end, clientIp);
        return ResponseEntity.ok(count);
    }

    /**시간 + 필드 둘*/
    @GetMapping(params = {"start", "principal", "resource_name"})
    public ResponseEntity<List<SystemLevelFalse>> getPR(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "principal") String principal,
            @RequestParam(value = "resource_name") String resourceName
    ){
        List<SystemLevelFalse> slfList = slfService.getPR(start, end, principal, resourceName);
        return ResponseEntity.ok(slfList);
    }
    @GetMapping(value = "/count", params = {"start", "principal", "resource_name"})
    public ResponseEntity<Long> getPRCount(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "principal") String principal,
            @RequestParam(value = "resource_name") String resourceName
    ){
        Long count = slfService.getPRCount(start, end, principal, resourceName);
        return ResponseEntity.ok(count);
    }

    @GetMapping(params = {"start", "principal", "operation"})
    public ResponseEntity<List<SystemLevelFalse>> getPO(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "principal") String principal,
            @RequestParam(value = "operation") String operation
    ){
        List<SystemLevelFalse> slfList = slfService.getPO(start, end, principal, operation);
        return ResponseEntity.ok(slfList);
    }
    @GetMapping(value = "/count", params = {"start", "principal", "operation"})
    public ResponseEntity<Long> getPOCount(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "principal") String principal,
            @RequestParam(value = "operation") String operation
    ){
        Long count = slfService.getPOCount(start, end, principal, operation);
        return ResponseEntity.ok(count);
    }

    @GetMapping(params = {"start", "principal", "client_ip"})
    public ResponseEntity<List<SystemLevelFalse>> getPC(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "principal") String input1,
            @RequestParam(value = "client_ip") String input2
    ){
        List<SystemLevelFalse> slfList = slfService.getPC(start, end, input1, input2);
        return ResponseEntity.ok(slfList);
    }
    @GetMapping(value = "/count", params = {"start", "principal", "client_ip"})
    public ResponseEntity<Long> getPCCount(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "principal") String input1,
            @RequestParam(value = "client_ip") String input2
    ){
        Long count = slfService.getPCCount(start, end, input1, input2);
        return ResponseEntity.ok(count);
    }

    @GetMapping(params = {"start", "resource_name", "operation"})
    public ResponseEntity<List<SystemLevelFalse>> getRO(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "resource_name") String input1,
            @RequestParam(value = "operation") String input2
    ){
        List<SystemLevelFalse> slfList = slfService.getRO(start, end, input1, input2);
        return ResponseEntity.ok(slfList);
    }
    @GetMapping(value = "/count", params = {"start", "resource_name", "operation"})
    public ResponseEntity<Long> getROCount(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "resource_name") String input1,
            @RequestParam(value = "operation") String input2
    ){
        Long count = slfService.getROCount(start, end, input1, input2);
        return ResponseEntity.ok(count);
    }

    @GetMapping(params = {"start", "resource_name", "client_ip"})
    public ResponseEntity<List<SystemLevelFalse>> getRC(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "resource_name") String input1,
            @RequestParam(value = "client_ip") String input2
    ){
        List<SystemLevelFalse> slfList = slfService.getRC(start, end, input1, input2);
        return ResponseEntity.ok(slfList);
    }
    @GetMapping(value = "/count", params = {"start", "resource_name", "client_ip"})
    public ResponseEntity<Long> getRCCount(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "resource_name") String input1,
            @RequestParam(value = "client_ip") String input2
    ){
        Long count = slfService.getRCCount(start, end, input1, input2);
        return ResponseEntity.ok(count);
    }

    @GetMapping(params = {"start", "operation", "client_ip"})
    public ResponseEntity<List<SystemLevelFalse>> getOC(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "operation") String input1,
            @RequestParam(value = "client_ip") String input2
    ){
        List<SystemLevelFalse> slfList = slfService.getOC(start, end, input1, input2);
        return ResponseEntity.ok(slfList);
    }
    @GetMapping(value = "/count", params = {"start", "operation", "client_ip"})
    public ResponseEntity<Long> getOCCount(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "operation") String input1,
            @RequestParam(value = "client_ip") String input2
    ){
        Long count = slfService.getOCCount(start, end, input1, input2);
        return ResponseEntity.ok(count);
    }

    @GetMapping(params = {"start", "principal", "resource_name", "operation"})
    public ResponseEntity<List<SystemLevelFalse>> getPRO(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "principal") String input1,
            @RequestParam(value = "resource_name") String input2,
            @RequestParam(value = "operation") String input3
    ){
        List<SystemLevelFalse> slfList = slfService.getPRO(start, end, input1, input2,input3);
        return ResponseEntity.ok(slfList);
    }
    @GetMapping(value = "/count", params = {"start", "principal", "resource_name", "operation"})
    public ResponseEntity<Long> getPROCount(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "principal") String input1,
            @RequestParam(value = "resource_name") String input2,
            @RequestParam(value = "operation") String input3
    ){
        Long count = slfService.getPROCount(start, end, input1, input2,input3);
        return ResponseEntity.ok(count);
    }

    @GetMapping(params = {"start", "principal", "resource_name", "client_ip"})
    public ResponseEntity<List<SystemLevelFalse>> getPRC(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "principal") String input1,
            @RequestParam(value = "resource_name") String input2,
            @RequestParam(value = "client_ip") String input3
    ){
        List<SystemLevelFalse> slfList = slfService.getPRC(start, end, input1, input2,input3);
        return ResponseEntity.ok(slfList);
    }
    @GetMapping(value = "/count", params = {"start", "principal", "resource_name", "client_ip"})
    public ResponseEntity<Long> getPRCCount(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "principal") String input1,
            @RequestParam(value = "resource_name") String input2,
            @RequestParam(value = "client_ip") String input3
    ){
        Long count = slfService.getPRCCount(start, end, input1, input2,input3);
        return ResponseEntity.ok(count);
    }

    @GetMapping(params = {"start", "principal", "operation", "client_ip"})
    public ResponseEntity<List<SystemLevelFalse>> getPOC(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "principal") String input1,
            @RequestParam(value = "operation") String input2,
            @RequestParam(value = "client_ip") String input3
    ){
        List<SystemLevelFalse> slfList = slfService.getPOC(start, end, input1, input2,input3);
        return ResponseEntity.ok(slfList);
    }
    @GetMapping(value = "/count", params = {"start", "principal", "operation", "client_ip"})
    public ResponseEntity<Long> getPOCCount(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "principal") String input1,
            @RequestParam(value = "operation") String input2,
            @RequestParam(value = "client_ip") String input3
    ){
        Long count = slfService.getPOCCount(start, end, input1, input2,input3);
        return ResponseEntity.ok(count);
    }

    @GetMapping(params = {"start", "resource_name", "operation", "client_ip"})
    public ResponseEntity<List<SystemLevelFalse>> getROC(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "resource_name") String input1,
            @RequestParam(value = "operation") String input2,
            @RequestParam(value = "client_ip") String input3
    ){
        List<SystemLevelFalse> slfList = slfService.getROC(start, end, input1, input2,input3);
        return ResponseEntity.ok(slfList);
    }
    @GetMapping(value = "/count", params = {"start", "resource_name", "operation", "client_ip"})
    public ResponseEntity<Long> getROCCount(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "resource_name") String input1,
            @RequestParam(value = "operation") String input2,
            @RequestParam(value = "client_ip") String input3
    ){
        Long count = slfService.getROCCount(start, end, input1, input2,input3);
        return ResponseEntity.ok(count);
    }

    @GetMapping(params = {"start", "principal", "resource_name", "operation", "client_ip"})
    public ResponseEntity<List<SystemLevelFalse>> getPROC(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "principal") String input1,
            @RequestParam(value = "resource_name") String input2,
            @RequestParam(value = "operation") String input3,
            @RequestParam(value = "client_ip") String input4
    ){
        List<SystemLevelFalse> slfList = slfService.getPROC(start, end, input1, input2,input3,input4);
        return ResponseEntity.ok(slfList);
    }
    @GetMapping(value = "/count", params = {"start", "principal", "resource_name", "operation", "client_ip"})
    public ResponseEntity<Long> getPROCCount(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "principal") String input1,
            @RequestParam(value = "resource_name") String input2,
            @RequestParam(value = "operation") String input3,
            @RequestParam(value = "client_ip") String input4
    ){
        Long count = slfService.getPROCCount(start, end, input1, input2,input3,input4);
        return ResponseEntity.ok(count);
    }

}