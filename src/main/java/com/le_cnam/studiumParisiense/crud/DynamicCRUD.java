package com.le_cnam.studiumParisiense.crud;

import com.le_cnam.studiumParisiense.entities.DynamicEntity;
import com.le_cnam.studiumParisiense.entities.DynamicRelation;
import com.le_cnam.studiumParisiense.enums.Nodes;
import com.le_cnam.studiumParisiense.enums.Relationships;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DynamicCRUD {

    private final Neo4jClient neo4jClient;

    public DynamicCRUD(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    public void saveEntity(DynamicEntity entity) {
        String nodeType = entity.getNodeType().name(); // Use `name()` to get the enum value as a string
        StringBuilder cypher = new StringBuilder("CREATE (n:" + nodeType + " {");

        for (Map.Entry<String, Object> entry : entity.getProperties().entrySet()) {
            cypher.append(entry.getKey()).append(": $").append(entry.getKey()).append(", ");
        }

        cypher.setLength(cypher.length() - 2); // Remove the trailing comma and space
        cypher.append("})");

        Map<String, Object> properties = entity.getProperties();
        try {
            neo4jClient.query(cypher.toString())
                    .bindAll(properties)
                    .run();
            //    System.out.println("Entity saved successfully: " + nodeType);
        } catch (Exception e) {
            System.err.println("Error saving entity: " + e.getMessage());
        }
    }

    public void updateEntity(DynamicEntity entity) {
        String nodeType = entity.getNodeType().name(); // Use `name()` to get the enum value as a string
        StringBuilder cypher = new StringBuilder("MATCH (n:" + nodeType + " {UUID: $uuid}) SET ");

        for (Map.Entry<String, Object> entry : entity.getProperties().entrySet()) {
            cypher.append("n.").append(entry.getKey()).append(" = $").append(entry.getKey()).append(", ");
        }

        cypher.setLength(cypher.length() - 2); // Remove the trailing comma and space

        Map<String, Object> properties = entity.getProperties();
        properties.put("uuid", entity.getUUID()); // Ensure that the UUID is included for the match

        try {
            neo4jClient.query(cypher.toString())
                    .bindAll(properties)
                    .run();
            // System.out.println("Entity updated successfully: " + nodeType);
        } catch (Exception e) {
            System.err.println("Error updating entity: " + e.getMessage());
        }
    }


    public void saveRelationship(DynamicRelation relationship) {
        String relationshipType = relationship.getType().name(); // Use `name()` for enum to string conversion
        String cypher = "MATCH (a {UUID: $startId}) " +
                "WITH a " +
                "MATCH (b {UUID: $endId}) " +
                "CREATE (a)-[r:" + relationshipType + "]->(b) ";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("startId", relationship.getStartId());
        parameters.put("endId", relationship.getEndId());

        if (relationship.getProperties() != null && !relationship.getProperties().isEmpty()) {
            cypher += "SET ";
            for (Map.Entry<String, String> entry : relationship.getProperties().entrySet()) {
                cypher += "r." + entry.getKey() + " = $" + entry.getKey() + ", ";
                parameters.put(entry.getKey(), entry.getValue());
            }
            cypher = cypher.substring(0, cypher.length() - 2);
        }

        cypher += " RETURN r";

        try {
            neo4jClient.query(cypher)
                    .bindAll(parameters)
                    .run();
            System.out.println("Relationship created successfully: " + relationshipType);
        } catch (Exception e) {
            System.err.println("Error creating relationship: " + e.getMessage());
        }
    }

    public void updateRelationship(DynamicRelation relationship) {
        String relationshipType = relationship.getType().name(); // Use `name()` for enum to string conversion
        String cypher = "MATCH (a {UUID: $startId})-[r:" + relationshipType + "]->(b {UUID: $endId}) SET ";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("startId", relationship.getStartId());
        parameters.put("endId", relationship.getEndId());

        if (relationship.getProperties() != null && !relationship.getProperties().isEmpty()) {
            for (Map.Entry<String, String> entry : relationship.getProperties().entrySet()) {
                cypher += "r." + entry.getKey() + " = $" + entry.getKey() + ", ";
                parameters.put(entry.getKey(), entry.getValue());
            }
            cypher = cypher.substring(0, cypher.length() - 2); // Remove the trailing comma and space
        }

        cypher += " RETURN r"; // Optionally return the updated relationship

        try {
            neo4jClient.query(cypher)
                    .bindAll(parameters)
                    .run();
            System.out.println("Relationship updated successfully: " + relationshipType);
        } catch (Exception e) {
            System.err.println("Error updating relationship: " + e.getMessage());
        }
    }

    public Optional<DynamicEntity> findByUUID(String uuid) {
        String cypher = "MATCH (n {UUID: $uuid}) RETURN n";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("uuid", uuid);

        try {
            System.out.println("Finding entity by UUID: " + uuid);
            return neo4jClient.query(cypher)
                    .bindAll(parameters)
                    .fetchAs(DynamicEntity.class)
                    .mappedBy((typeSystem, record) -> mapToDynamicEntity(record.get("n").asNode()))
                    .one();
        } catch (Exception e) {
            System.err.println("Error finding entity by UUID: " + e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<DynamicEntity> findByPropertySingle(Nodes nodeType, String propertyKey, String propertyValue) {
        String cypher = "MATCH (n:" + nodeType.name() + " {" + propertyKey + ": $value}) RETURN n";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("value", propertyValue);

        try {
            return neo4jClient.query(cypher)
                    .bindAll(parameters)
                    .fetchAs(DynamicEntity.class)
                    .mappedBy((typeSystem, record) -> mapToDynamicEntity(record.get("n").asNode()))
                    .one();
        } catch (Exception e) {
            System.err.println("Error finding entity by propert as single: " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<DynamicEntity> findByTypeAndProperties(Nodes nodeType, Map<String, Object> properties) {
        if (properties.containsKey("UUID")) {
            properties.remove("UUID");

        }
        StringBuilder cypher = new StringBuilder("MATCH (n:" + nodeType.name() + " {");

        for (String key : properties.keySet()) {
            cypher.append(key).append(": $").append(key).append(", ");
        }

        cypher.setLength(cypher.length() - 2); // Remove the trailing comma and space
        cypher.append("}) RETURN n");

        try {
            return neo4jClient.query(cypher.toString())
                    .bindAll(properties)
                    .fetchAs(DynamicEntity.class)
                    .mappedBy((typeSystem, record) -> mapToDynamicEntity(record.get("n").asNode()))

                    .all().stream().toList();
        } catch (Exception e) {
            System.err.println("Error finding entities by type and properties: " + e.getMessage());
            return List.of();
        }
    }

    public List<DynamicEntity> findByProperty(Nodes nodeType, String propertyKey, String propertyValue) {
        String cypher = "MATCH (n:" + nodeType.name() + " {" + propertyKey + ": $value}) RETURN n";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("value", propertyValue);

        try {
            return neo4jClient.query(cypher)
                    .bindAll(parameters)
                    .fetchAs(DynamicEntity.class)
                    .all().stream().toList();
        } catch (Exception e) {
            System.err.println("Error finding entities by property: " + e.getMessage());
            return List.of();
        }
    }

    private DynamicEntity mapToDynamicEntity(Node node) {
        DynamicEntity entity = new DynamicEntity();
        entity.setNodeType(Nodes.valueOf(node.labels().iterator().next())); // Assuming the node has a single label
        entity.setUUID(node.get("UUID").asString());

        Map<String, Object> properties = new HashMap<>();
        node.asMap().forEach(properties::put);
        entity.addProperties(properties);

        return entity;
    }

    public List<DynamicRelation> findAllRelationships() {
        String cypher = "MATCH (a)-[r]->(b) RETURN a.UUID AS startNodeUUID, r, b.UUID AS endNodeUUID ";
        try {
            return neo4jClient.query(cypher)
                    .fetchAs(DynamicRelation.class)
                    .mappedBy((typeSystem, record) -> {
                        var relationship = record.get("r").asRelationship();
                        var startNodeUUID = record.get("startNodeUUID").asString();
                        var endNodeUUID = record.get("endNodeUUID").asString();
                        return mapToDynamicRelationWithUUIDs(relationship, startNodeUUID, endNodeUUID);
                    })
                    .all().stream().toList();
        } catch (Exception e) {
            System.err.println("Error finding all relationships: " + e.getMessage());
            return List.of();
        }
    }

    public Optional<DynamicRelation> findSingleRelationshipByEndNodeUUID(String endNodeUUID) {
        String cypher = "MATCH (a)-[r]->(b {UUID: $endUUID}) RETURN a.UUID AS startNodeUUID, r, b.UUID AS endNodeUUID LIMIT 1"; // Match both nodes
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("endUUID", endNodeUUID);
        try {
            Optional<DynamicRelation> relationshipOpt = neo4jClient.query(cypher)
                    .bindAll(parameters)
                    .fetchAs(DynamicRelation.class)
                    .mappedBy((typeSystem, record) -> {
                        var relationship = record.get("r").asRelationship();
                        var startNodeUUID = record.get("startNodeUUID").asString();
                        return mapToDynamicRelationWithUUIDs(relationship, startNodeUUID, endNodeUUID);
                    })
                    .one();

            if (relationshipOpt.isPresent()) {
                System.out.println("Found relationship: " + relationshipOpt.get());
            } else {
                System.out.println("No relationship found for UUID: " + endNodeUUID);
            }

            return relationshipOpt;
        } catch (Exception e) {
            System.err.println("Error finding relationship by end node UUID: " + e.getMessage());
            return Optional.empty(); // Return empty if an error occurs
        }
    }
    public Optional<DynamicRelation> findSingleRelationshipByStartAndEndNodeUUID(String startNodeUUID, String endNodeUUID) {
        String cypher = "MATCH (a {UUID: $startUUID})-[r]->(b {UUID: $endUUID}) RETURN a.UUID AS startNodeUUID, r, b.UUID AS endNodeUUID LIMIT 1"; // Match both nodes with relationship
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("startUUID", startNodeUUID);
        parameters.put("endUUID", endNodeUUID);

        try {
            Optional<DynamicRelation> relationshipOpt = neo4jClient.query(cypher)
                    .bindAll(parameters)
                    .fetchAs(DynamicRelation.class)
                    .mappedBy((typeSystem, record) -> {
                        var relationship = record.get("r").asRelationship();
                        var startNode = record.get("startNodeUUID").asString();
                        var endNode = record.get("endNodeUUID").asString();
                        return mapToDynamicRelationWithUUIDs(relationship, startNode, endNode);
                    })
                    .one();

            if (relationshipOpt.isPresent()) {
                System.out.println("Found relationship: " + relationshipOpt.get());
            } else {
//                System.out.println("No relationship found for startNodeUUID: " + startNodeUUID + " and endNodeUUID: " + endNodeUUID);
            }

            return relationshipOpt;
        } catch (Exception e) {
            System.err.println("Error finding relationship by start and end node UUID: " + e.getMessage());
            return Optional.empty(); // Return empty if an error occurs
        }
    }


    private DynamicRelation mapToDynamicRelation(Relationship relationship) {

        DynamicRelation dynamicRelation = new DynamicRelation();
        dynamicRelation.setType(Relationships.valueOf(relationship.type()));
        dynamicRelation.setStartId(relationship.startNodeElementId());
        dynamicRelation.setEndId(relationship.endNodeElementId());

        Map<String, String> properties = new HashMap<>();
        for (Map.Entry<String, Object> entry : relationship.asMap().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            properties.put(key, value.toString());
        }
        dynamicRelation.setProperties(properties);

        return dynamicRelation;
    }

    private DynamicRelation mapToDynamicRelationWithUUIDs(Relationship relationship, String startNodeUUID, String endNodeUUID) {

        DynamicRelation dynamicRelation = new DynamicRelation();
        dynamicRelation.setType(Relationships.valueOf(relationship.type()));
        dynamicRelation.setStartId(startNodeUUID);
        dynamicRelation.setEndId(endNodeUUID);

        Map<String, String> properties = new HashMap<>();
        for (Map.Entry<String, Object> entry : relationship.asMap().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            properties.put(key, value.toString());
        }
        dynamicRelation.setProperties(properties);

        return dynamicRelation;
    }


}
