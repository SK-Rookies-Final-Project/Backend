package com.finalproject.springbackend.dto;

import lombok.Data;

@Data
public class AssignedPartition {
    private String topic;
    private int partition;
    private long committedOffset;
    private long latestOffset;
    private long lag;

    public AssignedPartition(String topic, int partition, long committedOffset, long latestOffset, long lag) {
        this.topic = topic;
        this.partition = partition;
        this.committedOffset = committedOffset;
        this.latestOffset = latestOffset;
        this.lag = lag;
    }
}