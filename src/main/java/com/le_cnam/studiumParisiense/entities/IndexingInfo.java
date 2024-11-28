package com.le_cnam.studiumParisiense.entities;

import com.le_cnam.studiumParisiense.enums.IndexPositions;
import com.le_cnam.studiumParisiense.enums.Nodes;
import com.le_cnam.studiumParisiense.enums.Relationships;
import lombok.Data;

@Data
public class IndexingInfo {
    private Nodes nodeType;
    private Relationships relationship;
    private IndexPositions indexPosition;
    private boolean isNecessaryToCreateNode = false;

    public IndexingInfo(Nodes nodeType, Relationships relationship, IndexPositions indexPosition, boolean isNecessaryToCreateNode) {
        this.nodeType = nodeType;
        this.relationship = relationship;
        this.indexPosition = indexPosition;
        this.isNecessaryToCreateNode = isNecessaryToCreateNode;
    }




}
