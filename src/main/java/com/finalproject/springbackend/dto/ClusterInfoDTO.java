package com.finalproject.springbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClusterInfoDTO {
    private String clusterId;
    private List<BrokerInfo> brokers;
    private Map<String, String> clusterConfigs;
    private int totalPartitions;
    private int totalTopics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BrokerInfo {
        private int id;
        private String host;
        private int port;
        private String rack;
        private boolean isController;
    }
}
