package com.finalproject.springbackend.dto;

import lombok.Data;

@Data
public class RegisterSchemaRequestDTO {
    public String subject; // e.g. my-topic-value
    public String schema;  // Avro schema JSON string
}
