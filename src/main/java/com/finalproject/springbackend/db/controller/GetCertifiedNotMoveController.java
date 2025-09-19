package com.finalproject.springbackend.db.controller;

import com.finalproject.springbackend.db.entity.CertifiedNotMove;
import com.finalproject.springbackend.db.repository.projection.AlertTypeCount;
import com.finalproject.springbackend.db.repository.projection.IpCount;
import com.finalproject.springbackend.db.service.CertifiedNotMoveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/db/certified_not_move")
@RequiredArgsConstructor
public class GetCertifiedNotMoveController {

    private final CertifiedNotMoveService cnmService;

    @GetMapping
    public ResponseEntity<List<CertifiedNotMove>> getAll(){
        return ResponseEntity.ok(cnmService.getAll());
    }

    @GetMapping(value = "/count")
    public ResponseEntity<Long> getAllCount(){
        return ResponseEntity.ok(cnmService.getAllCount());
    }

    @GetMapping(params = {"clientIp"})
    public ResponseEntity<List<CertifiedNotMove>> getOnlyClientIp(
            @RequestParam(value = "clientIp") String clientIp
    ) {
        List<CertifiedNotMove> c2tList = cnmService.getOnlyClientIp(clientIp);
        return ResponseEntity.ok(c2tList);
    }

    @GetMapping(value = "/count", params = {"clientIp"})
    public ResponseEntity<Long> getOnlyClientIpCount(@RequestParam(value = "clientIp") String clientIp){
        Long count = cnmService.getOnlyClientIpCount(clientIp);
        return ResponseEntity.ok(count);
    }

    @GetMapping(params = {"alertType"})
    public ResponseEntity<List<CertifiedNotMove>> getOnlyAlertType(@RequestParam(value = "alertType") String alertType){
        List<CertifiedNotMove> c2tList = cnmService.getOnlyAlertType(alertType);
        return ResponseEntity.ok(c2tList);
    }

    @GetMapping(value = "/count", params = {"alertType"})
    public ResponseEntity<Long> getOnlyAlertTypeCount(@RequestParam(value = "alertType") String alertType){
        Long cnt = cnmService.getOnlyAlertTypeCount(alertType);
        return ResponseEntity.ok(cnt);
    }

    @GetMapping(params = {"start"})
    public ResponseEntity<List<CertifiedNotMove>> getTimeOnly(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end
    ){
        List<CertifiedNotMove> c2tList = cnmService.getTimeOnly(start, end);
        return ResponseEntity.ok(c2tList);
    }

    @GetMapping(value = "/count", params = {"start"})
    public ResponseEntity<Long> getTimeOnlyCount(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end
    ){
        Long cnt = cnmService.getTimeOnlyCount(start, end);
        return ResponseEntity.ok(cnt);
    }

    @GetMapping(params = {"start", "clientIp"})
    public ResponseEntity<List<CertifiedNotMove>> getC(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "clientIp") String clientIp
    ){
        List<CertifiedNotMove> c2tList = cnmService.getC(start, end, clientIp);
        return ResponseEntity.ok(c2tList);
    }

    @GetMapping(value = "/count", params = {"start", "clientIp"})
    public ResponseEntity<Long> getCCount(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "clientIp") String clientIp
    ){
        Long cnt = cnmService.getCCount(start, end,clientIp);
        return ResponseEntity.ok(cnt);
    }

    @GetMapping(params = {"start", "alertType"})
    public ResponseEntity<List<CertifiedNotMove>> getA(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "alertType") String alertType
    ){
        List<CertifiedNotMove> c2tList = cnmService.getA(start, end, alertType);
        return ResponseEntity.ok(c2tList);
    }

    @GetMapping(value = "/count", params = {"start", "alertType"})
    public ResponseEntity<Long> getACount(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "alertType") String alertType
    ){
        Long cnt = cnmService.getACount(start, end, alertType);
        return ResponseEntity.ok(cnt);
    }

    @GetMapping(value = "/count/group/clientIp", params = {"start"})
    public ResponseEntity<List<IpCount>> getIpCount(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end
    ){
        List<IpCount> c2tIpCountList = cnmService.getIpCount(start, end);
        return ResponseEntity.ok(c2tIpCountList);
    }

    @GetMapping(value = "/count/group/clientIp")
    public ResponseEntity<List<IpCount>> getIpCountAll(){
        List<IpCount> c2tIpCountList = cnmService.getIpCountAll();
        return ResponseEntity.ok(c2tIpCountList);
    }

    @GetMapping(value = "/count/group/alertType", params = {"start"})
    public ResponseEntity<List<AlertTypeCount>> getAlertTypeCount (
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end
    ) {
        List<AlertTypeCount> c2tATCountList = cnmService.getAlertTypeCount(start, end);
        return ResponseEntity.ok(c2tATCountList);
    }

    @GetMapping(value="/count/group/alertType")
    public ResponseEntity<List<AlertTypeCount>> getAlterTypeCountAll(){
        List<AlertTypeCount> c2tAlterTypeCountList = cnmService.getAlertTypeCountAll();
        return ResponseEntity.ok(c2tAlterTypeCountList);
    }

}
