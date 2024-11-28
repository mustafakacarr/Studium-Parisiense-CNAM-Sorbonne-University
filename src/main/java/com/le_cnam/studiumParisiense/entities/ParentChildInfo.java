package com.le_cnam.studiumParisiense.entities;

import com.le_cnam.studiumParisiense.enums.Nodes;
import com.le_cnam.studiumParisiense.enums.Relationships;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

public class ParentChildInfo {

    public Nodes parentNode;
    public Map<Nodes, ChildInfo> childNodes;
    public String prefix;

    public ParentChildInfo(Nodes parentNode, Map<Nodes, ChildInfo> childNodes, String prefix) {
        this.parentNode = parentNode;
        this.childNodes = childNodes;
        this.prefix = prefix;
    }

    public static class ChildInfo {
        public Relationships relation;
       public String tag;

        public ChildInfo(Relationships relation, String tag) {
            this.relation = relation;
            this.tag = tag;
        }

        @Override
        public String toString() {
            return "Relation: " + relation + ", Tag: " + tag;
        }
    }



    public void addChildNode(Nodes childNode, Relationships relation, String tag) {
        childNodes.put(childNode, new ChildInfo(relation, tag));
    }


}
