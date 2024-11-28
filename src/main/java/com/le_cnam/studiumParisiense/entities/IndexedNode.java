package com.le_cnam.studiumParisiense.entities;

import com.le_cnam.studiumParisiense.enums.Nodes;
import com.le_cnam.studiumParisiense.enums.Relationships;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class IndexedNode {
    Nodes nodeType;
    Relationships relationshipType;
    String clearValue;
    String originalValue;
    HashMap<String, String> relationAdditionalProperties = new HashMap<>();
    HashMap<String, String> commonAdditionalPropertiesForPreviousRelation = new HashMap<>();

    String baseRelationUUID;

    public void addRelationAdditionalProperty(String key, String value) {
        relationAdditionalProperties.put(key, value);
    }
    public void addCommonAdditionalPropertyForPreviousRelation(String key, String value) {
        commonAdditionalPropertiesForPreviousRelation.put(key, value);
    }

    public IndexedNode(Nodes nodeType, Relationships relationshipType, String clearValue, String originalValue) {
        this.nodeType = nodeType;
        this.relationshipType = relationshipType;
        this.clearValue = clearValue;
        this.originalValue = originalValue;
    }
}
