package com.le_cnam.studiumParisiense.relations;

import lombok.Data;

import java.util.List;

@Data
public class CustomNodeRelation {
    private List<CustomRelation> rules;
}

