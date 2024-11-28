package com.le_cnam.studiumParisiense.relations;

import com.le_cnam.studiumParisiense.enums.Nodes;
import com.le_cnam.studiumParisiense.enums.Relationships;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasicRelation {
    private Nodes to;
    private Relationships type;
}