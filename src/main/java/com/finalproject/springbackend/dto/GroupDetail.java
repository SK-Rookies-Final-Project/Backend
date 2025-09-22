package com.finalproject.springbackend.dto;

import lombok.Data;
import java.util.List;

@Data
public class GroupDetail {
    private String groupId;
    private String state;
    private String coordinator;
    private List<MemberInfo> members;
    private Long totalLag;
}