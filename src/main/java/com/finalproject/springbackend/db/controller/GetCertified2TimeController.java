package com.finalproject.springbackend.db.controller;

import com.finalproject.springbackend.db.entity.Certified2Time;
import com.finalproject.springbackend.db.repository.projection.AlertTypeCount;
import com.finalproject.springbackend.db.repository.projection.IpCount;
import com.finalproject.springbackend.db.service.Certified2TimeService;
import com.finalproject.springbackend.dto.Certified2TimeResponseDTO;
import com.finalproject.springbackend.util.TimeZoneUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/db/certified_2_time")
@RequiredArgsConstructor
public class GetCertified2TimeController {

    private final Certified2TimeService c2tService;

    @GetMapping
    public ResponseEntity<List<Certified2Time>> getAll(){
        List<Certified2Time> c2tList = c2tService.getAll();
        //더미데이터 살짝 넣어주기
//        List<Certified2Time> c2tList = List.of(
//                Certified2Time.builder()
//                        .id("abc")
//                        .clientIp("123.12.abc.abc")
//                        .alertTimeKST(OffsetDateTime.parse("2025-09-16T05:58:56.048Z"))
//                        .alertType("aaa")
//                        .description("abc")
//                        .failureCount(123L)
//                        .build(),
//                Certified2Time.builder()
//                        .id("abcde")
//                        .clientIp("123.12.111.123")
//                        .alertTimeKST(OffsetDateTime.parse("2025-09-16T05:58:56.048Z"))
//                        .alertType("aaa")
//                        .description("abc")
//                        .failureCount(123L)
//                        .build(),
//                Certified2Time.builder()
//                        .id("abc")
//                        .clientIp("123.12.222.231")
//                        .alertTimeKST(OffsetDateTime.parse("2025-09-16T05:58:56.048Z"))
//                        .alertType("aaa")
//                        .description("abc")
//                        .failureCount(123L)
//                        .build()
//        );
        return ResponseEntity.ok(c2tList);
    }

    @GetMapping(value = "/count")
    public ResponseEntity<Long> getAllCount(){
        Long cnt = c2tService.getAllCount();
//        Long cnt = 100L;
        return ResponseEntity.ok(cnt);
    }

    @GetMapping(params = {"clientIp"})
    public ResponseEntity<List<Certified2Time>> getOnlyClientIp(
            @RequestParam(value = "clientIp") String clientIp
    ) {
        List<Certified2Time> c2tList = c2tService.getOnlyClientIp(clientIp);
        //더미데이터 살짝 넣어주기
//        List<Certified2Time> c2tList = List.of(
//                Certified2Time.builder()
//                        .id("abc")
//                        .clientIp("123.12.abc.abc")
//                        .alertTimeKST(OffsetDateTime.parse("2025-09-16T05:58:56.048Z"))
//                        .alertType("aaa")
//                        .description("abc")
//                        .failureCount(123L)
//                        .build(),
//                Certified2Time.builder()
//                        .id("abcde")
//                        .clientIp("123.12.111.123")
//                        .alertTimeKST(OffsetDateTime.parse("2025-09-16T05:58:56.048Z"))
//                        .alertType("aaa")
//                        .description("abc")
//                        .failureCount(123L)
//                        .build(),
//                Certified2Time.builder()
//                        .id("abc")
//                        .clientIp("123.12.222.231")
//                        .alertTimeKST(OffsetDateTime.parse("2025-09-16T05:58:56.048Z"))
//                        .alertType("aaa")
//                        .description("abc")
//                        .failureCount(123L)
//                        .build()
//        );

        return ResponseEntity.ok(c2tList);
    }

    @GetMapping(value = "/count", params = {"clientIp"})
    public ResponseEntity<Long> getOnlyClientIpCount(@RequestParam(value = "clientIp") String clientIp){
        Long count = c2tService.getOnlyClientIpCount(clientIp);
        return ResponseEntity.ok(count);
    }

    @GetMapping(params = {"alertType"})
    public ResponseEntity<List<Certified2Time>> getOnlyAlertType(@RequestParam(value = "alertType") String alertType){
        List<Certified2Time> c2tList = c2tService.getOnlyAlertType(alertType);
        //더미데이터 살짝 넣어주기
//        List<Certified2Time> c2tList = List.of(
//                Certified2Time.builder()
//                        .id("abc")
//                        .clientIp("123.12.abc.abc")
//                        .alertTimeKST(OffsetDateTime.parse("2025-09-16T05:58:56.048Z"))
//                        .alertType("aaa")
//                        .description("abc")
//                        .failureCount(123L)
//                        .build(),
//                Certified2Time.builder()
//                        .id("abcde")
//                        .clientIp("123.12.111.123")
//                        .alertTimeKST(OffsetDateTime.parse("2025-09-16T05:58:56.048Z"))
//                        .alertType("aaa")
//                        .description("abc")
//                        .failureCount(123L)
//                        .build(),
//                Certified2Time.builder()
//                        .id("abc")
//                        .clientIp("123.12.222.231")
//                        .alertTimeKST(OffsetDateTime.parse("2025-09-16T05:58:56.048Z"))
//                        .alertType("aaa")
//                        .description("abc")
//                        .failureCount(123L)
//                        .build()
//        );

        return ResponseEntity.ok(c2tList);
    }

    @GetMapping(value = "/count", params = {"alertType"})
    public ResponseEntity<Long> getOnlyAlertTypeCount(@RequestParam(value = "alertType") String alertType){
        Long cnt = c2tService.getOnlyAlertTypeCount(alertType);
//        Long cnt = 100L;
        return ResponseEntity.ok(cnt);
    }

    @GetMapping(params = {"start"})
    public ResponseEntity<List<Certified2TimeResponseDTO>> getTimeOnly(
            @RequestParam(value = "start") String start,
            @RequestParam(value = "end", required = false) String end
    ){
        try {
            // 프론트엔드에서 받은 ISO 시간 문자열을 한국 시간으로 파싱
            OffsetDateTime startTime = TimeZoneUtil.parseFromFrontend(start);
            OffsetDateTime endTime = end != null ? TimeZoneUtil.parseFromFrontend(end) : null;
            
            log.debug("🕐 Time range query: {} to {}", 
                     TimeZoneUtil.formatForDebug("start", startTime),
                     TimeZoneUtil.formatForDebug("end", endTime));
            
            List<Certified2Time> c2tList = c2tService.getTimeOnly(startTime, endTime);
            List<Certified2TimeResponseDTO> responseDTOs = c2tList.stream()
                    .map(Certified2TimeResponseDTO::from)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(responseDTOs);
        } catch (IllegalArgumentException e) {
            log.error("❌ Invalid date format: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping(value = "/count", params = {"start"})
    public ResponseEntity<Long> getTimeOnlyCount(
            @RequestParam(value = "start") String start,
            @RequestParam(value = "end", required = false) String end
    ){
        try {
            OffsetDateTime startTime = TimeZoneUtil.parseFromFrontend(start);
            OffsetDateTime endTime = end != null ? TimeZoneUtil.parseFromFrontend(end) : null;
            
            Long cnt = c2tService.getTimeOnlyCount(startTime, endTime);
            return ResponseEntity.ok(cnt);
        } catch (IllegalArgumentException e) {
            log.error("❌ Invalid date format: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping(params = {"start", "clientIp"})
    public ResponseEntity<List<Certified2TimeResponseDTO>> getC(
            @RequestParam(value = "start") String start,
            @RequestParam(value = "end", required = false) String end,
            @RequestParam(value = "clientIp") String clientIp
    ){
        try {
            OffsetDateTime startTime = TimeZoneUtil.parseFromFrontend(start);
            OffsetDateTime endTime = end != null ? TimeZoneUtil.parseFromFrontend(end) : null;
            
            List<Certified2Time> c2tList = c2tService.getC(startTime, endTime, clientIp);
            List<Certified2TimeResponseDTO> responseDTOs = c2tList.stream()
                    .map(Certified2TimeResponseDTO::from)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(responseDTOs);
        } catch (IllegalArgumentException e) {
            log.error("❌ Invalid date format: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping(value = "/count", params = {"start", "clientIp"})
    public ResponseEntity<Long> getCCount(
            @RequestParam(value = "start") String start,
            @RequestParam(value = "end", required = false) String end,
            @RequestParam(value = "clientIp") String clientIp
    ){
        try {
            OffsetDateTime startTime = TimeZoneUtil.parseFromFrontend(start);
            OffsetDateTime endTime = end != null ? TimeZoneUtil.parseFromFrontend(end) : null;
            
            Long cnt = c2tService.getCCount(startTime, endTime, clientIp);
            return ResponseEntity.ok(cnt);
        } catch (IllegalArgumentException e) {
            log.error("❌ Invalid date format: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping(params = {"start", "alertType"})
    public ResponseEntity<List<Certified2Time>> getA(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "alertType") String alertType
    ){
        List<Certified2Time> c2tList = c2tService.getA(start, end, alertType);
        //더미데이터 살짝 넣어주기
//        List<Certified2Time> c2tList = List.of(
//                Certified2Time.builder()
//                        .id("abc")
//                        .clientIp("123.12.abc.abc")
//                        .alertTimeKST(OffsetDateTime.parse("2025-09-16T05:58:56.048Z"))
//                        .alertType("aaa")
//                        .description("abc")
//                        .failureCount(123L)
//                        .build(),
//                Certified2Time.builder()
//                        .id("abcde")
//                        .clientIp("123.12.111.123")
//                        .alertTimeKST(OffsetDateTime.parse("2025-09-16T05:58:56.048Z"))
//                        .alertType("aaa")
//                        .description("abc")
//                        .failureCount(123L)
//                        .build(),
//                Certified2Time.builder()
//                        .id("abc")
//                        .clientIp("123.12.222.231")
//                        .alertTimeKST(OffsetDateTime.parse("2025-09-16T05:58:56.048Z"))
//                        .alertType("aaa")
//                        .description("abc")
//                        .failureCount(123L)
//                        .build()
//        );

        return ResponseEntity.ok(c2tList);
    }

    @GetMapping(value = "/count", params = {"start", "alertType"})
    public ResponseEntity<Long> getACount(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end,
            @RequestParam(value = "alertType") String alertType
    ){
        Long cnt = c2tService.getACount(start, end, alertType);
//        Long cnt = 100L;
        return ResponseEntity.ok(cnt);
    }

    @GetMapping(value = "/count/group/clientIp", params = {"start"})
    public ResponseEntity<List<IpCount>> getIpCount(
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end
    ){
        List<IpCount> c2tIpCountList = c2tService.getIpCount(start, end);
        return ResponseEntity.ok(c2tIpCountList);
    }

    @GetMapping(value = "/count/group/clientIp")
    public ResponseEntity<List<IpCount>> getIpCountAll(){
        List<IpCount> c2tIpCountList = c2tService.getIpCountAll();
        return ResponseEntity.ok(c2tIpCountList);
    }

    @GetMapping(value = "/count/group/alertType", params = {"start"})
    public ResponseEntity<List<AlertTypeCount>> getAlertTypeCount (
            @RequestParam(value = "start") OffsetDateTime start,
            @RequestParam(value = "end", required = false) OffsetDateTime end
    ) {
        List<AlertTypeCount> c2tATCountList = c2tService.getAlertTypeCount(start, end);
        return ResponseEntity.ok(c2tATCountList);
    }

    @GetMapping(value="/count/group/alertType")
    public ResponseEntity<List<AlertTypeCount>> getAlterTypeCountAll(){
        List<AlertTypeCount> c2tAlterTypeCountList = c2tService.getAlertTypeCountAll();
        return ResponseEntity.ok(c2tAlterTypeCountList);
    }

}
