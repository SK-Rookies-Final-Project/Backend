package com.finalproject.springbackend.service;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchemaService {

    private final SchemaRegistryFactory factory;

    /**
     * Avro 스키마 등록
     */
    public int registerAvro(String subject, String rawSchema) throws Exception {
        try {
            SchemaRegistryClient client = factory.client();
            ParsedSchema schema = new AvroSchema(rawSchema);
            int id = client.register(subject, schema);
            log.info("✅ Registered Avro schema for subject '{}' with ID: {}", subject, id);
            return id;
        } catch (Exception e) {
            log.error("❌ Failed to register Avro schema for subject '{}': {}", subject, e.getMessage());
            throw e;
        }
    }

    /**
     * 모든 스키마 subject 목록 조회
     */
    public List<String> listSubjects() throws Exception {
        try {
            List<String> subjects = factory.client().getAllSubjects().stream().sorted().toList();
            log.debug("📋 Listed {} schema subjects", subjects.size());
            return subjects;
        } catch (Exception e) {
            log.error("❌ Failed to list schema subjects: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 특정 subject의 최신 스키마 메타데이터 조회
     */
    public SchemaMetadata getLatest(String subject) throws Exception {
        try {
            SchemaMetadata metadata = factory.client().getLatestSchemaMetadata(subject);
            log.debug("📄 Retrieved latest schema for subject '{}': version={}, id={}", 
                     subject, metadata.getVersion(), metadata.getId());
            return metadata;
        } catch (Exception e) {
            log.error("❌ Failed to get latest schema for subject '{}': {}", subject, e.getMessage());
            throw e;
        }
    }
}
