package com.le_cnam.studiumParisiense.parser;

import com.le_cnam.studiumParisiense.entities.DynamicEntity;
import com.le_cnam.studiumParisiense.entities.DynamicRelation;
import com.le_cnam.studiumParisiense.entities.IndexedNode;
import com.le_cnam.studiumParisiense.enums.Nodes;
import com.le_cnam.studiumParisiense.enums.Relationships;
import com.le_cnam.studiumParisiense.enums.Tags;
import com.le_cnam.studiumParisiense.helper.ParentChildHelper;
import com.le_cnam.studiumParisiense.helper.StringManipulations;
import com.le_cnam.studiumParisiense.crud.*;
import com.le_cnam.studiumParisiense.relations.*;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.le_cnam.studiumParisiense.helper.StringManipulations.*;

@Data
@Component
public class XmlParser {
    private final LinkedList<DynamicEntity> nodeList = new LinkedList<>();
    private final List<DynamicRelation> relationList = new ArrayList<>();
    private final List<DynamicEntity> ordinaryNodeListForRelation = new ArrayList<>();
    private DynamicEntity lastProcessedNode;
    private final Map<Nodes, List<BasicRelation>> basicRelationMap;
    private final Map<Nodes, CustomNodeRelation> customRelationMap;

    private List<Nodes> multiInstanceNodes = new ArrayList<>();

    {
        multiInstanceNodes.addAll(Arrays.asList(Nodes.REFERENCE, Nodes.NAME_VARIANTS, Nodes.COMMENT, Nodes.LOCATION, Nodes.BIBLIOGRAPHY));
        multiInstanceNodes.addAll(ParentChildHelper.getAllChildren());
    }


    /**
     * Needing to be refactored, unnecessary and confusing variable.
     **/
    private final Set<Nodes> unAllowedDuplicatedNodes = Set.of(Nodes.LOCATION, Nodes.REFERENCE, Nodes.NATION, Nodes.COLLEGE, Nodes.PERSON);
    private final DynamicCRUD dbClient;
    private final StringManipulations stringManipulations;
    private final Map<Nodes, Map<String, Object>> tempNodeMap = new HashMap<>();
    private final HashMap<String, String> usualTagAndValueMap = new HashMap<>();

    private final HashMap<String, Map<String, String>> commonAdditionalPropertiesForSpecificRelationships = new HashMap<String, Map<String, String>>();

    public XmlParser(RelationLoader relationLoader, DynamicCRUD dbClient, StringManipulations stringManipulations) throws IOException {
        this.basicRelationMap = relationLoader.getBasicRelationRules();
        this.customRelationMap = relationLoader.getCustomRelationRules();
        this.dbClient = dbClient;
        this.stringManipulations = stringManipulations;
    }

    public void parseXmlData(String xmlData) {
        ordinaryNodeListForRelation.clear();
        nodeList.clear();
        relationList.clear();

        List<String> lines = extractLines(xmlData);
        Pattern pattern = Pattern.compile("^<([a-zA-Z0-9]+)>\\s*(.*)$");

        for (String line : lines) {
            Matcher matcher = pattern.matcher(line.trim());

            if (matcher.matches()) {
                String tag = matcher.group(1);
                String value = matcher.group(2);

                String createdBy = "<" + tag + "> TAG";
                Nodes nodeType;
                DynamicEntity parentNode = null;
                if (StringManipulations.startsWithSpecialCharacter('/', value)) {
                    nodeType = Nodes.fromFirstCharOfValue(value);
                    createdBy = "BY / PREFIX";
                } else {
                    nodeType = Nodes.fromCode(tag);
                    if (Nodes.getParentNodeOfChildNode(tag) != null) {
                        Nodes parentNodeType = Nodes.getParentNodeOfChildNode(tag);

                        Map<String, Object> parentProperties = new HashMap<>();
                        parentProperties.put("createdBy", "AUTO-CREATED PARENT NODE");

                        parentNode = createNewNode(parentNodeType, parentProperties);

                    }
                }

                Map<String, Object> properties = new HashMap<>();
                properties.put("createdBy", createdBy);
                String property = Tags.fromCode(tag).getValue();


                // Handle multi-instance nodes like Name_variants, Comments, References, etc.
                DynamicEntity createdNode = null;
                if (multiInstanceNodes.contains(nodeType)) {
                    if (customRelationMap.containsKey(nodeType)) {
                        CustomRelation customRelationForTag = stringManipulations.getCustomRelationForTag(nodeType, tag);
                        CustomRelation customRelationForValue = stringManipulations.getCustomRelationForValue(nodeType, value);
                        CustomRelation tempCustomRelation = null;
                        if (customRelationForTag != null) {
                            tempCustomRelation = customRelationForTag;
                            property = "text";

                        } else if (customRelationForValue != null) {
                            tempCustomRelation = customRelationForValue;
                            property = "text";
                            value = value.substring(1);
                        }

                        properties.put(property, clearValue(value));
                        createdNode = createNewNode(nodeType, properties);
                        createCustomRelationshipIfNeeded(createdNode, tempCustomRelation);
                    } else {
                        properties.put(property, clearValue(value));
                        createdNode = createNewNode(nodeType, properties);
                        if (parentNode != null) {
                            createRelationship(parentNode, createdNode, determineRelationshipBetweenParentAndChild(createdNode.getNodeType()), Optional.empty());
                        }
                    }
                    if (!checkRegexForValue(createdNode, tag, value))
                        checkIndexing(createdNode, tag, value);

                } else {
                    usualTagAndValueMap.put(tag, value);
                    createdNode = processNode(tag, value);
                    if (parentNode != null) {
                        createRelationship(parentNode, createdNode, determineRelationshipBetweenParentAndChild(createdNode.getNodeType()), Optional.empty());
                    }

                }
            }
        }
        processOrdinaryRelationships();
        processCommonAdditionalProperties();
        addRawText(xmlData);
    }

    private Relationships determineRelationshipBetweenParentAndChild(Nodes childNode) {
        return ParentChildHelper.getRelationshipBetweenParentAndChild(childNode);
    }

    private void processCommonAdditionalProperties() {
        if (!commonAdditionalPropertiesForSpecificRelationships.isEmpty()) {
            for (Map.Entry<String, Map<String, String>> entry : commonAdditionalPropertiesForSpecificRelationships.entrySet()) {
                String baseRelationUUID = entry.getKey();

                Map<String, String> commonProperties = entry.getValue();
                DynamicRelation relation = findBaseRelationByEndNodeUUID(baseRelationUUID);
                if (relation != null) {
                    relation.addProperties(commonProperties);
                    dbClient.updateRelationship(relation);
                }
            }
        }
    }

    private DynamicRelation findBaseRelationByEndNodeUUID(String endNodeUUID) {
        return dbClient.findSingleRelationshipByEndNodeUUID(endNodeUUID).orElse(null);
    }

    private static List<String> extractLines(String xmlData) {
        return List.of(xmlData.split("\n"));
    }

    private DynamicEntity processNode(String tag, String value) {
        Nodes currentNodeType = Nodes.fromCode(tag);
        String currentProperty = Tags.fromCode(tag).getValue();
        String currentValue = value;

        if (stringManipulations.isContainsLinkChars(currentValue)) {
            String previousTag = getPreviousTagOnTagValueList(tag);
            if (previousTag != null) {
                String previousValue = usualTagAndValueMap.get(previousTag);
                String newFormOfPreviousValue = stringManipulations.addHrefEndOfPrevious(previousValue, currentValue);
                usualTagAndValueMap.put(previousTag, newFormOfPreviousValue);
                tempNodeMap.computeIfAbsent(currentNodeType, k -> new HashMap<>()).put(Tags.fromCode(previousTag).toString(), newFormOfPreviousValue);
            }
        }

        tempNodeMap.computeIfAbsent(currentNodeType, k -> new HashMap<>()).put(currentProperty, clearValue(currentValue));
        tempNodeMap.computeIfAbsent(currentNodeType, k -> new HashMap<>()).put("createdBy", "<" + tag + "> TAG");
        DynamicEntity createdNode = createOrUpdateSingleInstanceNode(currentNodeType, tempNodeMap.get(currentNodeType));
        if (!checkRegexForValue(createdNode, tag, value))
            checkIndexing(createdNode, tag, value);

        return createdNode;
    }

    public boolean checkRegexForValue(DynamicEntity dynamicEntity, String tag, String value) {
        List<IndexedNode> indexedNodes = stringManipulations.getMatcherForValue(value);
        if (indexedNodes != null) {
            for (IndexedNode indexedNode : indexedNodes) {
                processIndexing(dynamicEntity, indexedNode);
            }
            return true;
        } else {
            return false;
        }
    }

    private void checkIndexing(DynamicEntity createdNode, String tag, String value) {
        List<Character> indexingChars = findIndexingCharacters(value);

        if (indexingChars.isEmpty()) {
            System.out.println("No indexing characters found for value: " + value);
            Map<String, String> propertyNameAndClearValue = getPropertyNameAndClearValue(value);
            if (propertyNameAndClearValue != null) {
                commonAdditionalPropertiesForSpecificRelationships.computeIfAbsent(createdNode.getUUID(), k -> new HashMap<>()).putAll(propertyNameAndClearValue);
            }
        } else {
            String baseValue = value; // Default to the whole value initially


            for (char indexingChar : indexingChars) {
                System.out.println("Processing indexing character: " + indexingChar + " for tag: " + tag + " and value: " + value);

/**
 * WE WILL CREATE A NEW NODE HERE ACCORDING TO INDEXING CHAR.
 * FOR EXAMPLE, IF THE INDEXING CHAR IS '*', WE WILL CREATE A NEW LOCATION NODE AFTER CHECKED THE CURRENT LOCATION DOESN'T EXIST ON DB.
 **/
                if (isNecessaryToCreateNewNodeByIndexChar(indexingChar)) {
                    IndexedNode indexedNode = stringManipulations.createIndexedNodeByValue(tag, baseValue);
                    processIndexing(createdNode, indexedNode);
                } else {
                    Map<String, String> propertyNameAndClearValue = getPropertyNameAndClearValue(value);
                    if (propertyNameAndClearValue != null) {
                        commonAdditionalPropertiesForSpecificRelationships.computeIfAbsent(createdNode.getUUID(), k -> new HashMap<>()).putAll(propertyNameAndClearValue);
                    }
                }
            }
        }
    }


    private void processIndexing(DynamicEntity createdNode, IndexedNode indexedNode) {
        String basePropertyName = "value";

        if (indexedNode.getNodeType() == Nodes.PERSON)
            basePropertyName = "name";
        if (!indexedNode.getCommonAdditionalPropertiesForPreviousRelation().isEmpty()) {
            commonAdditionalPropertiesForSpecificRelationships.computeIfAbsent(createdNode.getUUID(), k -> new HashMap<>()).putAll(indexedNode.getCommonAdditionalPropertiesForPreviousRelation());
        }

        DynamicEntity nodeInDB = dbClient.findByPropertySingle(indexedNode.getNodeType(), basePropertyName, indexedNode.getClearValue()).orElse(null);

        if (nodeInDB == null) {
            DynamicEntity indexedNodeOnDB = createNewNode(indexedNode, Optional.of(basePropertyName));
            createRelationship(createdNode, indexedNodeOnDB, indexedNode.getRelationshipType(), Optional.of(indexedNode.getRelationAdditionalProperties()));
        } else {
            createRelationship(createdNode, nodeInDB, indexedNode.getRelationshipType(), Optional.of(indexedNode.getRelationAdditionalProperties()));
        }
    }

    private String getPreviousTagOnTagValueList(String currentTag) {
        List<String> keys = new ArrayList<>(usualTagAndValueMap.keySet());
        int currentIndex = keys.indexOf(currentTag);
        return (currentIndex > 0) ? keys.get(currentIndex - 1) : null;
    }

    private void createCustomRelationshipIfNeeded(DynamicEntity currentNode, CustomRelation customRelation) {
        DynamicEntity previousNode = currentNode;
        int currentIndex = nodeList.indexOf(currentNode);
        int i = currentIndex - 1;
        while (i >= 0) {
            previousNode = nodeList.get(i);
            if (previousNode.getNodeType() != currentNode.getNodeType()) {
                break;
            }
            i--;
        }

        if (customRelation != null && previousNode != null && previousNode != currentNode) {
            createRelationship(previousNode, currentNode, customRelation.getRelation(), Optional.empty());
        }
    }

    private void processOrdinaryRelationships() {
        System.out.println("Processing ordinary relationships");
        for (DynamicEntity entity : ordinaryNodeListForRelation) {
            List<BasicRelation> rules = basicRelationMap.get(entity.getNodeType());
            if (rules != null && !rules.isEmpty()) {
                for (BasicRelation rule : rules) {
                    try {
                        Nodes targetNodeType = rule.getTo();
                        List<DynamicEntity> targetEntities = findNodesByType(targetNodeType);

                        for (DynamicEntity targetEntity : targetEntities) {
                            if (targetEntity != null) {
                                DynamicRelation relationship = new DynamicRelation();
                                relationship.setStartId(entity.getUUID());
                                relationship.setEndId(targetEntity.getUUID());
                                relationship.setType(rule.getType());

                                createRelationship(entity, targetEntity, rule.getType(), Optional.empty());
                            } else {
                                throw new IllegalArgumentException("No target node found for relationship rule: " + rule.getType() + " to " + rule.getTo());
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("No target node found for relationship rule: " + rule.getType() + " to " + rule.getTo());
                    }
                }
            }
        }
    }

    private void createRelationship(DynamicEntity startNode, DynamicEntity endNode, Relationships relationshipType, Optional<Map<String, String>> properties) {
        // System.out.println(startNode.getNodeType() + " -> " + endNode.getNodeType() + " : " + relationshipType);
        if (startNode != null && endNode != null) {

            if (!dbClient.findSingleRelationshipByStartAndEndNodeUUID(startNode.getUUID(), endNode.getUUID()).isPresent()) {

                DynamicRelation relationship = new DynamicRelation();
                relationship.setStartId(startNode.getUUID());
                relationship.setEndId(endNode.getUUID());
                relationship.setType(relationshipType);
                if (properties.isPresent()) {
                    relationship.addProperties(properties.get());
                }

                dbClient.saveRelationship(relationship);
            }
        }
    }

    /**
     * OVERLOADING
     */
    private DynamicEntity createNewNode(Nodes nodeType, Map<String, Object> properties) {

        if (unAllowedDuplicatedNodes.contains(nodeType)) {
            List<DynamicEntity> existingNodes = dbClient.findByTypeAndProperties(nodeType, properties);
            if (existingNodes.size() > 0) {
                nodeList.add(existingNodes.get(0));
                return existingNodes.get(0);
            }
        }
        DynamicEntity entity = new DynamicEntity();
        entity.setNodeType(nodeType);
        entity.addProperties(properties);
        dbClient.saveEntity(entity);
        nodeList.add(entity);
        if (properties.get("createdBy") != null && properties.get("createdBy").equals("INDEXING")) {
            nodeList.removeLast();
        }
        return entity;
    }

    /**
     * OVERLOADING
     */
    private DynamicEntity createNewNode(IndexedNode indexedNode, Optional<String> basePropertyName) {
        HashMap<String, Object> properties = new HashMap<>();
        properties.put(basePropertyName.orElse("value"), indexedNode.getClearValue());
        properties.put("createdBy", "INDEXING");

        return createNewNode(indexedNode.getNodeType(), properties);
    }

    private DynamicEntity createOrUpdateSingleInstanceNode(Nodes nodeType, Map<String, Object> properties) {
        Optional<DynamicEntity> existingNode = findNodesByType(nodeType).stream().findFirst();
        if (existingNode.isPresent()) {

            DynamicEntity nodeInDb = dbClient.findByUUID(existingNode.get().getUUID()).orElse(null);
            if (nodeInDb != null) {
                nodeInDb.addProperties(properties);
                dbClient.updateEntity(nodeInDb);
                return nodeInDb;
            } else
                throw new RuntimeException("Node not found in the database");

        } else {
            DynamicEntity newNode = createNewNode(nodeType, properties);
            if (!multiInstanceNodes.contains(newNode.getNodeType())) {
                ordinaryNodeListForRelation.add(newNode);
            }
            return newNode;
        }
    }

    private List<DynamicEntity> findNodesByType(Nodes nodeType) {
        return nodeList.stream()
                .filter(node -> node.getNodeType() == nodeType)
                .collect(Collectors.toList());
    }

    private void addRawText(String rawText) {
        DynamicEntity rawTextNode = createNewNode(Nodes.RAW_TEXT, Map.of("value", rawText));
        createRelationship(nodeList.getFirst(), rawTextNode, Relationships.HAS_RAW_TEXT, Optional.empty());
    }
}
