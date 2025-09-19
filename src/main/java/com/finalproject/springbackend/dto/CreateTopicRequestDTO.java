package com.finalproject.springbackend.dto;

import lombok.Data;

@Data
public class CreateTopicRequestDTO {
    public String name;
    public int partitions;
    public short replicationFactor;
}
