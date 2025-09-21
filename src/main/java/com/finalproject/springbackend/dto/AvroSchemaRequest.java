package com.finalproject.springbackend.dto;

import lombok.Data;

@Data
public class AvroSchemaRequest {
    private String subject;
    private String schema; // Avro raw schema string
}