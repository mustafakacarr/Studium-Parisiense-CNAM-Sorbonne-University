package com.le_cnam.studiumParisiense.enums;

public enum ConditionType {
    BY_VALUE("byValue"),
    BY_TAG("byTag");

    private final String value;

    ConditionType(String value) {
        this.value = value;
    }
}
