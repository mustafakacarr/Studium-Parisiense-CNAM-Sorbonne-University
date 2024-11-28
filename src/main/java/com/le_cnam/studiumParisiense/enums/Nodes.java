package com.le_cnam.studiumParisiense.enums;

import com.le_cnam.studiumParisiense.entities.ParentChildInfo;
import com.le_cnam.studiumParisiense.helper.ParentChildHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum Nodes {
    PERSON("PERSON"),
    NAME_VARIANTS("NAME_VARIANTS"),
    ORIGIN("ORIGIN"),
    RELATIONAL_INSERTION("RELATIONAL_INSERTION"),
    COURSE("COURSE"),
    ECCLESIASTICAL_CAREER("ECCLESIASTICAL_CAREER"),
    PROFESSIONAL_CAREER("PROFESSIONAL_CAREER"),
    POLITICAL_CAREER("POLITICAL_CAREER"),
    VOYAGE("VOYAGE"),
    COMMISSION("COMMISSION"),
    HOUSING("HOUSING"),
    INDIVIDUATION_SIGNS("INDIVIDUATION_SIGNS"),
    ORALITY("ORALITY"),
    VARIOUS("VARIOUS"),
    LIBRARY("LIBRARY"),
    TEXTUAL_PRODUCTION("TEXTUAL_PRODUCTION"),
    REFERENCE("REFERENCE"),
    COMMENT("COMMENT"),
    LOCATION("LOCATION"),
    NATION("NATION"),
    COLLEGE("COLLEGE"),
    UNIVERSITY("UNIVERSITY"),
    BIBLIOGRAPHY("BIBLIOGRAPHY"),
    RAW_TEXT("RAW_TEXT"),
    PRE_UNIVERSITY_EDUCATION("PRE_UNIVERSITY_EDUCATION"),
    UNIVERSITY_OR_STUDIUM("UNIVERSITY_OR_STUDIUM"),
    CURRICULUM("CURRICULUM"),
    INFORMATION_COLLEGE("INFORMATION_COLLEGE"),
    FOUNDATION_COLLEGE("FOUNDATION_COLLEGE"),
    UNCERTAIN_ECCLESIASTICAL_STATUS("UNCERTAIN_ECCLESIASTICAL_STATUS"),
    SECULAR_ECCLESIASTIC("SECULAR_ECCLESIASTIC"),
    SECULAR_BENEFICES_HELD_BY_REGULAR_ECCLESIASTIC("SECULAR_BENEFICES_HELD_BY_REGULAR_ECCLESIASTIC"),
    REGULAR_ECCLESIASTIC("REGULAR_ECCLESIASTIC"),
    HIERARCHICAL_POSITION_IN_REGULAR_ORDER("HIERARCHICAL_POSITION_IN_REGULAR_ORDER"),
    PAPAL_FUNCTIONS("PAPAL_FUNCTIONS"),
    SCHOOL_MASTER_OR_TUTOR("SCHOOL_MASTER_OR_TUTOR"),
    LEGAL_PROFESSIONAL("LEGAL_PROFESSIONAL"),
    PROPERTY_ADMINISTRATOR("PROPERTY_ADMINISTRATOR"),
    ADMINISTRATION_ROYAL("ADMINISTRATION_ROYAL"),
    RELIGIOUS_FUNCTIONS_FOR_COURT("RELIGIOUS_FUNCTIONS_FOR_COURT"),
    CULTURAL_FUNCTIONS_FOR_COURT("CULTURAL_FUNCTIONS_FOR_COURT"),
    LOCAL_ADMINISTRATION_FUNCTIONS("LOCAL_ADMINISTRATION_FUNCTIONS"),
    REPRESENTATIONS_INCLUDING_ECLESIASTICAL_DIPLOMACY("REPRESENTATIONS_INCLUDING_ECLESIASTICAL_DIPLOMACY"),
    MERCHANT_ARTISAN_SOLDIER("MERCHANT_ARTISAN_SOLDIER"),
    DOCTOR_SURGEON("DOCTOR_SURGEON"),
    DIVERSE_PROFESSIONS("DIVERSE_PROFESSIONS"),
    CHAMBER_VALET("CHAMBER_VALET"),
    ECCLESIASTICAL_FUNCTIONS("ECCLESIASTICAL_FUNCTIONS"),
    COMMUNITY_FOUNDATION("COMMUNITY_FOUNDATION");

    private final String type;

    Nodes(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }


    ;

    public static Nodes fromType(String type) {
        for (Nodes node : Nodes.values()) {
            if (node.getType().equals(type)) {
                return node;
            }
        }
        throw new IllegalArgumentException("No node type found for: " + type);
    }

    public static Nodes fromCode(String code) {
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("Code cannot be null or empty");
        }
        if (code.startsWith("1c")) {
            return NAME_VARIANTS;
        }
        //They're child nodes that means we have to create base node for each one. For example nodes that starts with 5,
        // have to be related with COURSE node. Because course nodes are parent of them. Its just for make easier to follow traces of nodes on our graph database.
        // it aims to gather them in a parent node.
        for (ParentChildInfo parentChildInfo : ParentChildHelper.parentChildList) {
            for (Map.Entry<Nodes, ParentChildInfo.ChildInfo> entry : parentChildInfo.childNodes.entrySet()) {
                if (entry.getValue().tag.equals(code)) {
                    return entry.getKey();
                }
            }
        }

        // If its in range 19-99
        if (code.matches("^(1[9]|[2-9][0-9]).*")) {
            return BIBLIOGRAPHY;
        }
        // If its in range 10-18
        if (code.matches("^(1[0-8])$")) {
            return switch (code) {
                case "10" -> COMMISSION;
                case "11", "12", "13", "14" -> HOUSING; // If its in range 11-14
                case "15" -> INDIVIDUATION_SIGNS;
                case "16" -> ORALITY;
                case "17" -> VARIOUS;
                case "18" -> LIBRARY;
                default -> PERSON;
            };
        }

        char firstChar = code.charAt(0);

        return switch (firstChar) {
            case '1' -> PERSON;
            case '2' -> ORIGIN;
            case '3' -> RELATIONAL_INSERTION;
            case '5' -> COURSE;
            case '6' -> ECCLESIASTICAL_CAREER;
            case '7' -> PROFESSIONAL_CAREER;
            case '8' -> POLITICAL_CAREER;
            case '9' -> VOYAGE;
            case 'r' -> REFERENCE;
            default -> throw new IllegalArgumentException("No node type found for the code: " + code);
        };
    }

    public static Nodes fromFirstCharOfValue(String value) {
        return switch (value.charAt(0)) {
            case '/' -> COMMENT;
            default -> throw new IllegalArgumentException("No node type found for the value: " + value);
        };

    }


    public static Nodes getParentNodeOfChildNode(String tag) {
        if (tag.startsWith("5")) {
            return COURSE;
        } else if (tag.startsWith("6")) {
            return ECCLESIASTICAL_CAREER;
        } else if (tag.startsWith("7")) {
            return PROFESSIONAL_CAREER;
        } else return null;

    }
}
