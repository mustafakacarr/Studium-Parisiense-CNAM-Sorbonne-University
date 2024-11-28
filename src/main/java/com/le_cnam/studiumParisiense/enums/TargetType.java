package com.le_cnam.studiumParisiense.enums;

public enum TargetType {
    CURRENT_NODE("currentTag"),
    PREVIOUS_NODE("previousTag");

    private final String value;

    TargetType(String value) {
        this.value = value;
    }

    public TargetType fromValue(String value) {
        for (TargetType targetType : TargetType.values()) {
            if (targetType.value.equals(value)) {
                return targetType;
            }
        }
        throw new IllegalArgumentException("No target type found for: " + value);
    }
}
