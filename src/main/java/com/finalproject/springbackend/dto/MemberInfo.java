package com.finalproject.springbackend.dto;

import lombok.Data;
import java.util.List;

@Data
public class MemberInfo {
    private String consumerId;
    private String clientId;
    private String host;
    private List<AssignedPartition> assignedPartitions;

    public MemberInfo(String consumerId, String clientId, String host, List<AssignedPartition> assignedPartitions) {
        this.consumerId = consumerId;
        this.clientId = clientId;
        this.host = host;
        this.assignedPartitions = assignedPartitions;
    }
}