package com.le_cnam.studiumParisiense.helper;

import com.le_cnam.studiumParisiense.enums.ConditionType;
import com.le_cnam.studiumParisiense.enums.Nodes;
import com.le_cnam.studiumParisiense.enums.Relationships;
import com.le_cnam.studiumParisiense.enums.IndexPositions;
import com.le_cnam.studiumParisiense.entities.IndexedNode;
import com.le_cnam.studiumParisiense.entities.IndexingInfo;
import com.le_cnam.studiumParisiense.relations.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class StringManipulations {

    private final Map<Nodes, CustomNodeRelation> customRelationMap;
    private final Map<Nodes, List<BasicRelation>> basicRelationMap;

    public StringManipulations(RelationLoader relationLoader) {
        this.customRelationMap = relationLoader.getCustomRelationRules();
        this.basicRelationMap = relationLoader.getBasicRelationRules();
    }

    public static HashMap<Character, IndexingInfo> indexingChars = new HashMap<>() {
        {
            put('*', new IndexingInfo(Nodes.LOCATION, Relationships.HAS_LOCATION, IndexPositions.STARTS_WITH, true));
            put('£', new IndexingInfo(Nodes.NATION, Relationships.HAS_NATION, IndexPositions.STARTS_WITH, true));
            put('$', new IndexingInfo(Nodes.PERSON, Relationships.HAS_RELATION, IndexPositions.START_AND_END_WITH, true));
            put('%', new IndexingInfo(null, null, IndexPositions.START_AND_END_WITH, false));
        }
    };

    public static HashMap<String, List<Nodes>> regexPatternsForNodes = new HashMap<>() {
        {
            put("\\*(.*?) \\(nation de £(.*?)\\),? %(.*?)%[.,\\s]*", List.of(Nodes.LOCATION, Nodes.NATION));
            put("\\*(.*?) \\(collège de £(.*?)\\),? %(.*?)%[.,\\s]*", List.of(Nodes.LOCATION, Nodes.COLLEGE));
        }
    };
    public static HashMap<String, String> regexForSpesificValues = new HashMap<>() {
        {
            put("%(.*?)%", "dateOfDegree");
        }
    };

    public static Map<String, String> getPropertyNameAndClearValue(String value) {
        Map<String, String> propertyAndClearValue = new HashMap<>();

        for (Map.Entry<String, String> entry : regexForSpesificValues.entrySet()) {
            String regex = entry.getKey();
            String propertyName = entry.getValue();

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(value);

            if (matcher.find()) {
                String dateValue = matcher.group(matcher.groupCount());
                if (dateValue != null) {
                    getPrecisionAndDate(dateValue, matcher).forEach(propertyAndClearValue::put);

                    String matchedGroup = matcher.group(1);
                    if (matchedGroup.contains("-")) {
                        String[] dateParts = matchedGroup.split("-");
                        propertyAndClearValue.put("startDateOfDegree", dateParts[0]);
                        propertyAndClearValue.put("endDateOfDegree", dateParts[1]);
                    } else {
                        propertyAndClearValue.put(propertyName, matchedGroup);
                    }
                    return propertyAndClearValue;
                }
            }
        }
        return null;
    }

    static String toProperCase(String s) {
        return s.substring(0, 1).toUpperCase() +
                s.substring(1).toLowerCase();
    }

    public static boolean isContainsLinkChars(String value) {
        return value.startsWith("//");
    }

    public static String addHrefEndOfPrevious(String previousValue, String url) {
        return previousValue + "<a href='" + url.substring(2) + "'>Visit the link</a>";
    }

    public static boolean startsWithSpecialCharacter(char specialChar, String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        return value.length() > 1 && value.startsWith(String.valueOf(specialChar)) && value.charAt(1) != specialChar;
    }

    public static boolean startsWithSpecialCharacter(String specialChar, String value) {
        return startsWithSpecialCharacter(specialChar.charAt(0), value);
    }

    public static List<Character> findIndexingCharacters(String value) {
        List<Character> indexingCharsFound = new ArrayList<>();
        for (Character key : indexingChars.keySet()) {
            if (value.contains(String.valueOf(key))) {
                indexingCharsFound.add(key);
            }
        }

        if (indexingCharsFound.isEmpty()) {
            System.out.println("No indexing characters found for: " + value);
        } else {
            System.out.println("Found indexing characters: " + indexingCharsFound + " for value: " + value);
        }
        return indexingCharsFound;
    }


    public static boolean isNecessaryToCreateNewNodeByIndexChar(char indexingChar) {
        return indexingChars.get(indexingChar).isNecessaryToCreateNode();
    }

    public static Nodes getNodeTypeFromIndexingChar(String value) {
        for (Map.Entry<Character, IndexingInfo> entry : indexingChars.entrySet()) {
            if (value.contains(String.valueOf(entry.getKey())) && entry.getValue().getNodeType() != null) {
                // Return the first (and typically only) Node in the map
                return entry.getValue().getNodeType();
            }
        }
        throw new IllegalArgumentException("No node type found for: " + value);
    }

    public Relationships getRelationshipTypeFromIndexedNode(Nodes nodeType, Optional<String> tag) {
        // First, search in indexingChars
        for (Map.Entry<Character, IndexingInfo> entry : indexingChars.entrySet()) {
            if (entry.getValue().getNodeType() == nodeType && entry.getValue().getRelationship() != null) {
                CustomRelation customRelation = getCustomRelationForTag(Nodes.RELATIONAL_INSERTION, tag.orElse(""));
                if (nodeType.equals(Nodes.PERSON) && tag.isPresent() && customRelation != null) {
                    // Custom relation found for PERSON nodes
                    return customRelation.getRelation();
                }
                return entry.getValue().getRelationship();
            }
        }

        for (Map.Entry<Nodes, List<BasicRelation>> entry : basicRelationMap.entrySet()) {
            List<BasicRelation> relations = entry.getValue();
            for (BasicRelation relation : relations) {
                if (relation.getTo().equals(nodeType)) {
                    return relation.getType(); // Return the "type" if "to" matches the nodeType
                }
            }
        }

        // Throw an exception if no relationship is found in either map
        throw new IllegalArgumentException("No relationship type found for: " + nodeType);
    }


    public IndexedNode createIndexedNodeByValue(String tag, String value) {
        Nodes nodeType = getNodeTypeFromIndexingChar(value);
        Relationships relationship = getRelationshipTypeFromIndexedNode(nodeType, Optional.of(tag));
        String newValue = extractIndexingValue(value);
        System.out.println("Node type: " + nodeType + " Relationship: " + relationship + " Value: " + newValue + " Original value: " + value);
        return new IndexedNode(nodeType, relationship, newValue, value);
    }

    public static String extractIndexingValue(String value) {
        System.out.println(value + " is the value");
        String originalValue = value;
        for (Map.Entry<Character, IndexingInfo> entry : indexingChars.entrySet()) {
            char indexingChar = entry.getKey();
            IndexingInfo info = entry.getValue();
            String charAsString = String.valueOf(indexingChar);

            if (info.getIndexPosition() == IndexPositions.START_AND_END_WITH) {
                String regex = "\\" + charAsString + "(.*?)" + "\\" + charAsString;
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(value);

                if (matcher.find()) {
                    value = matcher.group(1).trim();
                }
            } else if (info.getIndexPosition() == IndexPositions.STARTS_WITH) {
                System.out.println("Starts with: " + charAsString);
                if (value.contains(charAsString)) {
                    String regex = "\\" + charAsString + "\\s*([^\\s.,;!?]+)";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(value);

                    if (matcher.find()) {
                        System.out.println("Found starts with: " + matcher.group(1) + " for value: " + value);
                        value = matcher.group(1).trim();
                    }
                }
            }
        }

        return value.equals(originalValue) ? value : value.trim();
    }


    public static String clearValue(String value) {
        // Loop over indexingChars to clean the value based on different rules
        for (Map.Entry<Character, IndexingInfo> entry : indexingChars.entrySet()) {
            char indexingChar = entry.getKey();
            IndexingInfo info = entry.getValue();
            String charAsString = String.valueOf(indexingChar);

            // Remove all leading occurrences of STARTS_WITH characters
            if (info.getIndexPosition() == IndexPositions.STARTS_WITH) {
                while (value.startsWith(charAsString)) {
                    value = value.substring(1);
                }
            }

            // Remove all trailing occurrences of ENDS_WITH characters
            if (info.getIndexPosition() == IndexPositions.ENDS_WITH) {
                while (value.endsWith(charAsString)) {
                    value = value.substring(0, value.length() - 1);
                }
            }

            // Remove any substrings enclosed by START_AND_END_WITH characters
            if (info.getIndexPosition() == IndexPositions.START_AND_END_WITH) {
                String regex = "\\" + charAsString + "(.*?)" + "\\" + charAsString;
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(value);

                // Replace all matched indexed substrings (without the surrounding special chars)
                while (matcher.find()) {
                    value = value.replace(matcher.group(0), matcher.group(1).trim());
                }
            }
        }

        // Final cleanup: remove any remaining individual index characters
        for (Character indexingChar : indexingChars.keySet()) {
            value = value.replace(String.valueOf(indexingChar), "");
        }

        return value.trim();
    }

    public List<IndexedNode> getMatcherForValue(String value) {
        List<IndexedNode> indexedNodes = new ArrayList<>();

        for (Map.Entry<String, List<Nodes>> entry : regexPatternsForNodes.entrySet()) {
            Pattern pattern = Pattern.compile(entry.getKey(), Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(value);

            if (matcher.find()) {
                List<Nodes> nodeList = entry.getValue();

                for (int i = 0; i < matcher.groupCount() - 1; i++) {
                    String matchedValue = matcher.group(i + 1);
                    System.out.println(matchedValue + " for the node " + nodeList.get(i) + i);

                    IndexedNode indexedNode = new IndexedNode(
                            nodeList.get(i),
                            getRelationshipTypeFromIndexedNode(nodeList.get(i), Optional.of("")),
                            clearValue(matchedValue),
                            matchedValue
                    );

                    String dateValue = matcher.group(matcher.groupCount());
                    if (dateValue != null) {
                        getPrecisionAndDate(dateValue, matcher)
                                .forEach(indexedNode::addCommonAdditionalPropertyForPreviousRelation);

                        if (dateValue.contains("-")) {
                            String[] dateParts = dateValue.split("-");
                            indexedNode.addCommonAdditionalPropertyForPreviousRelation("startDateOfStudying", dateParts[0]);
                            indexedNode.addCommonAdditionalPropertyForPreviousRelation("endDateOfStudying", dateParts[1]);
                        } else {
                            indexedNode.addCommonAdditionalPropertyForPreviousRelation("dateOfStudying", dateValue);
                        }
                    }

                    indexedNodes.add(indexedNode);
                }

                return indexedNodes; // Exit early after processing
            }
        }

        return null;
    }


    public static int countCharacter(String value, char character) {
        return (int) value.chars().filter(ch -> ch == character).count();
    }

    public CustomRelation getCustomRelationForTag(Nodes node, String tag) {
        CustomNodeRelation customNodeRelation = customRelationMap.get(node);
        if (customNodeRelation == null) return null;
        System.out.println("Custom relation found for tag: " + tag + " in node: " + node + " with relation: " + customNodeRelation.getRules().get(0).getRelation());
        return customNodeRelation.getRules().stream()
                .filter(rule -> rule.getCondition().equals(ConditionType.BY_TAG) && rule.getSpecialTag().get().equals(tag))
                .findFirst().orElse(null);
    }

    public CustomRelation getCustomRelationForValue(Nodes node, String value) {
        CustomNodeRelation customNodeRelation = customRelationMap.get(node);
        if (customNodeRelation == null) return null;

        return customNodeRelation.getRules().stream()
                .filter(rule -> rule.getCondition().equals(ConditionType.BY_VALUE) && startsWithSpecialCharacter(rule.getStartsWith().get(), value))
                .findFirst().orElse(null);
    }

    public static Map<String, String> getPrecisionAndDate(String dateValue, Matcher matcher) {
        String precision = "certain";
        String startDate = "";
        String endDate = "";

        if (dateValue != null) {
            if (dateValue.contains("-")) {
                String[] dateParts = dateValue.split("-");
                startDate = dateParts[0];
                endDate = dateParts[1];

                if (startDate.startsWith(":")) {
                    precision = "before";
                } else if (endDate.endsWith(":")) {
                    precision = "after";
                } else if (startDate.contains(":") || endDate.contains(":")) {
                    precision = "around";
                }
            } else {
                startDate = dateValue;
                if (matcher != null) {
                    if (startDate.startsWith(":")) {
                        precision = "before";
                    } else if (startDate.endsWith(":")) {
                        precision = "after";
                    }
                }
            }
        }

        return Map.of("precision", precision);
    }


}
