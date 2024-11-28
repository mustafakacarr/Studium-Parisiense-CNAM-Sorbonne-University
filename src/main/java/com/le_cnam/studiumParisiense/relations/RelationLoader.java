package com.le_cnam.studiumParisiense.relations;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.le_cnam.studiumParisiense.enums.Nodes;
import com.le_cnam.studiumParisiense.enums.Tags;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Data
public class RelationLoader {

    private static final String FILE_PATH = "/Users/mustafa/Desktop/CNAM WorkSpace/studiumParisiense/src/main/java/com/le_cnam/studiumParisiense/relations/jsonConfig/";
    private static final String DEFAULT_BASIC_RELATION_FILE = FILE_PATH + "BasicRelationRules.json";
    private static final String DEFAULT_CUSTOM_RELATION_FILE = FILE_PATH + "CustomRelationRulesForMultiInstanceNodes.json";

    private Map<Nodes, List<BasicRelation>> basicRelationRules;
    private Map<Nodes, CustomNodeRelation> customRelationRules;
    private static ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module());

    public RelationLoader() {
        try {
            this.basicRelationRules = loadBasicRelations(DEFAULT_BASIC_RELATION_FILE);
            this.customRelationRules = loadCustomRelations(DEFAULT_CUSTOM_RELATION_FILE);
        } catch (IOException e) {
            System.err.println("Failed to load relationships: " + e.getMessage());
            this.basicRelationRules = Collections.emptyMap();
            this.customRelationRules = Collections.emptyMap();
        }
    }

    public Map<Nodes, List<BasicRelation>> loadBasicRelations(String jsonFilePath) throws IOException {
        File jsonFile = getJsonFile(jsonFilePath);
        Map<String, BasicNodeRelation> rawMap = deserializeJsonFile(jsonFile, new TypeReference<Map<String, BasicNodeRelation>>() {
        });
        return parseBasicRelationshipMap(rawMap);
    }

    public Map<Nodes, CustomNodeRelation> loadCustomRelations(String jsonFilePath) throws IOException {
        File jsonFile = getJsonFile(jsonFilePath);
        Map<String, CustomNodeRelation> rawMap = deserializeJsonFile(jsonFile, new TypeReference<Map<String, CustomNodeRelation>>() {
        });
        return parseCustomRelationshipMap(rawMap);
    }


    private static File getJsonFile(String jsonFilePath) throws IOException {
        File jsonFile = new File(jsonFilePath);
        if (!jsonFile.exists()) {
            throw new IOException("JSON file not found: " + jsonFilePath);
        }
        return jsonFile;
    }

    private static <T> Map<String, T> deserializeJsonFile(File jsonFile, TypeReference<Map<String, T>> typeReference) throws IOException {
        return objectMapper.readValue(jsonFile, typeReference);
    }

    private static Map<Nodes, List<BasicRelation>> parseBasicRelationshipMap(Map<String, BasicNodeRelation> rawMap) {
        Map<Nodes, List<BasicRelation>> relationshipsMap = new HashMap<>();
        for (Map.Entry<String, BasicNodeRelation> entry : rawMap.entrySet()) {
            try {
                Nodes nodeType = Nodes.valueOf(entry.getKey());
                relationshipsMap.put(nodeType, entry.getValue().getRelations());
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid node type in JSON: " + entry.getKey());
            }
        }
        return relationshipsMap;
    }

    private static Map<Nodes, CustomNodeRelation> parseCustomRelationshipMap(Map<String, CustomNodeRelation> rawMap) {
        Map<Nodes, CustomNodeRelation> customMap = new HashMap<>();
        for (Map.Entry<String, CustomNodeRelation> entry : rawMap.entrySet()) {
            try {
                Nodes node = Nodes.valueOf(entry.getKey());
                CustomNodeRelation relation = entry.getValue();
                customMap.put(node, relation);
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid tag in JSON: " + entry.getKey());
            }
        }
        return customMap;
    }

    public List<BasicRelation> getRelationshipsForNode(Nodes node) {
        return basicRelationRules.getOrDefault(node, Collections.emptyList());
    }

    /*  public CustomRelation getCustomRelationForTag(Nodes node) {
          return customRelationRules.getOrDefault(node, new CustomRelation());
      }
  */

}
