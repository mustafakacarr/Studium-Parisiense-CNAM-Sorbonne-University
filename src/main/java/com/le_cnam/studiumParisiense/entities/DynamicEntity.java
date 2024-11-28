package com.le_cnam.studiumParisiense.entities;

import com.le_cnam.studiumParisiense.enums.Nodes;
import lombok.Data;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.util.HashMap;
import java.util.Map;

@Data
@Node
public class DynamicEntity {
    @Id
    @GeneratedValue(generatorClass = GeneratedValue.UUIDGenerator.class)
    private String UUID=java.util.UUID.randomUUID().toString();

    private Map<String, Object> properties = new HashMap<>();
    private Nodes nodeType;
    private DynamicEntity previousNode;

    public DynamicEntity() {
        properties.put("UUID", UUID);
    }


    public void addProperty(String key, Object value) {
        properties.put(key, value);
    }
    public void addProperties(Map<String, Object> properties) {
        this.properties.putAll(properties);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return "DynamicEntity{" +
                "UUID='" + UUID + '\'' +
                ", properties=" + properties +
                ", nodeType=" + nodeType +
                '}';
    }
}
