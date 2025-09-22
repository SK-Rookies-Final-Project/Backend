package com.finalproject.springbackend.dto;

import lombok.Data;
import java.util.List;

@Data
public class GroupSummary {
    private String groupId;
    private String state;
    private Long members;
    private Long totalLag;
    private List<String> topics;

    public GroupSummary(String groupId, String state, Long members, Long totalLag, List<String> topics) {
        this.groupId = groupId;
        this.state = state;
        this.members = members;
        this.totalLag = totalLag;
        this.topics = topics;
    }
}