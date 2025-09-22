package com.finalproject.springbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartitionInfoDTO {
    private String topicName;
    private int partition;
    private int leader;
    private List<Integer> replicas;
    private List<Integer> isr;
    private boolean offline;
    private long size;
    private long offset;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartitionReassignmentRequest {
        private String topicName;
        private int partition;
        private List<Integer> replicas;
    }
}
