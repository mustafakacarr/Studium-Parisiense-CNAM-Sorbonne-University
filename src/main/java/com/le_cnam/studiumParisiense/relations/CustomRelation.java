package com.le_cnam.studiumParisiense.relations;

import com.le_cnam.studiumParisiense.enums.ConditionType;
import com.le_cnam.studiumParisiense.enums.Relationships;
import com.le_cnam.studiumParisiense.enums.TargetType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomRelation {
    private TargetType to;
    private ConditionType condition;
    private Relationships relation;
    private Optional<String> startsWith;
    private Optional<String> specialTag;
}

