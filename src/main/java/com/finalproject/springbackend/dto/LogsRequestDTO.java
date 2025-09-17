package com.finalproject.springbackend.dto;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@ToString
public class LogsRequestDTO {
    private Integer page=0;     //페이지 번호 (0부터 시작)
    private Integer size=20;    //페이지 크기 (최대 100)
    private String from;        //시작 시간 (ISO 8601 형식)
    private String to;          //종료 시간 (ISO 8601 형식)
    private String users;       //사용자 목록 (쉼표로 구분)
    private String status;      //권한 상태 (success, failed, 쉼표로 구분)
    private String methods;     //메소드 목록 (쉼표로 구분)
    private String resources;   //리소스 타입 (쉼표로 구분)

}
