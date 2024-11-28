package com.le_cnam.studiumParisiense.entities;

import com.le_cnam.studiumParisiense.enums.Relationships;
import lombok.Data;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;

import java.util.HashMap;
import java.util.Map;

@Data
public class DynamicRelation {
    @Id
    @GeneratedValue(generatorClass = GeneratedValue.UUIDGenerator.class)
    private String UUID = java.util.UUID.randomUUID().toString();
    private Relationships type; // Relationship type (e.g., "FRIENDS_WITH")
    private String startId; // ID of the starting node
    private String endId; // ID of the ending node
    private String field; // Field name of the relationship



    private Map<String, String> properties = new HashMap<>();
    private String rootRelationUUID;

    public DynamicRelation() {
        properties.put("UUID", UUID);
        properties.put("field", field);
        properties.put("rootRelationUUID", rootRelationUUID);

    }

    public void addProperty(String key, String value) {
        properties.put(key, value);
    }

    public void addProperties(Map<String, String> properties) {
        this.properties.putAll(properties);
    }

    public String toString() {
        return "DynamicRelation{" +
                "type=" + type +
                ", startId='" + startId + '\'' +
                ", endId='" + endId + '\'' +
                ", field='" + field + '\'' +
                ", properties=" + properties +
                '}';
    }
}
