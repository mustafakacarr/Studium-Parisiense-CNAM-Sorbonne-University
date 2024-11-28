package com.le_cnam.studiumParisiense.helper;

import com.le_cnam.studiumParisiense.entities.ParentChildInfo;
import com.le_cnam.studiumParisiense.enums.Nodes;
import com.le_cnam.studiumParisiense.enums.Relationships;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static com.le_cnam.studiumParisiense.enums.Nodes.*;

public class ParentChildHelper {
    public static List<ParentChildInfo> parentChildList = new ArrayList<>();

    static {
        parentChildList.add(new ParentChildInfo(COURSE, new HashMap<>() {{
            put(PRE_UNIVERSITY_EDUCATION, new ParentChildInfo.ChildInfo(Relationships.HAS_PRE_UNIVERSITY_EDUCATION, "5a"));
            put(UNIVERSITY_OR_STUDIUM, new ParentChildInfo.ChildInfo(Relationships.HAS_UNIVERSITY_OR_STUDIUM, "5b"));
            put(CURRICULUM, new ParentChildInfo.ChildInfo(Relationships.HAS_CURRICULUM, "5c"));
            put(INFORMATION_COLLEGE, new ParentChildInfo.ChildInfo(Relationships.HAS_INFORMATION_COLLEGE, "5e"));
            put(FOUNDATION_COLLEGE, new ParentChildInfo.ChildInfo(Relationships.HAS_FOUNDATION_COLLEGE, "5f"));

        }}, "5"));
        parentChildList.add(new ParentChildInfo(ECCLESIASTICAL_CAREER, new HashMap<>() {{
            put(UNCERTAIN_ECCLESIASTICAL_STATUS, new ParentChildInfo.ChildInfo(Relationships.HAS_UNCERTAIN_ECCLESIASTICAL_STATUS, "6a"));
            put(SECULAR_ECCLESIASTIC, new ParentChildInfo.ChildInfo(Relationships.HAS_SECULAR_ECCLESIASTIC, "6b"));
            put(SECULAR_BENEFICES_HELD_BY_REGULAR_ECCLESIASTIC, new ParentChildInfo.ChildInfo(Relationships.HAS_SECULAR_BENEFICES_HELD_BY_REGULAR_ECCLESIASTIC, "6c"));
            put(REGULAR_ECCLESIASTIC, new ParentChildInfo.ChildInfo(Relationships.HAS_REGULAR_ECCLESIASTIC, "6d"));
            put(HIERARCHICAL_POSITION_IN_REGULAR_ORDER, new ParentChildInfo.ChildInfo(Relationships.HAS_HIERARCHICAL_POSITION_IN_REGULAR_ORDER, "6f"));

        }}, "6"));
        parentChildList.add(new ParentChildInfo(PROFESSIONAL_CAREER, new HashMap<>() {{
            put(SCHOOL_MASTER_OR_TUTOR, new ParentChildInfo.ChildInfo(Relationships.HAS_SCHOOL_MASTER_OR_TUTOR, "7a"));
            put(UNIVERSITY, new ParentChildInfo.ChildInfo(Relationships.HAS_UNIVERSITY, "7b"));
            put(LEGAL_PROFESSIONAL, new ParentChildInfo.ChildInfo(Relationships.HAS_LEGAL_PROFESSIONAL, "7c"));
            put(PROPERTY_ADMINISTRATOR, new ParentChildInfo.ChildInfo(Relationships.HAS_PROPERTY_ADMINISTRATOR, "7d"));
            put(RELIGIOUS_FUNCTIONS_FOR_COURT, new ParentChildInfo.ChildInfo(Relationships.HAS_RELIGIOUS_FUNCTIONS_FOR_COURT, "7g"));
            put(CULTURAL_FUNCTIONS_FOR_COURT, new ParentChildInfo.ChildInfo(Relationships.HAS_CULTURAL_FUNCTIONS_FOR_COURT, "7h"));
            put(ADMINISTRATION_ROYAL, new ParentChildInfo.ChildInfo(Relationships.HAS_ADMINISTRATION_ROYAL, "7j"));
            put(LOCAL_ADMINISTRATION_FUNCTIONS, new ParentChildInfo.ChildInfo(Relationships.HAS_LOCAL_ADMINISTRATION_FUNCTIONS, "7k"));
            put(REPRESENTATIONS_INCLUDING_ECLESIASTICAL_DIPLOMACY, new ParentChildInfo.ChildInfo(Relationships.HAS_REPRESENTATIONS_INCLUDING_ECLESIASTICAL_DIPLOMACY, "7l"));
            put(MERCHANT_ARTISAN_SOLDIER, new ParentChildInfo.ChildInfo(Relationships.HAS_MERCHANT_ARTISAN_SOLDIER, "7m"));
            put(DOCTOR_SURGEON, new ParentChildInfo.ChildInfo(Relationships.HAS_DOCTOR_SURGEON, "7n"));
            put(DIVERSE_PROFESSIONS, new ParentChildInfo.ChildInfo(Relationships.HAS_DIVERSE_PROFESSIONS, "7s"));
            put(CHAMBER_VALET, new ParentChildInfo.ChildInfo(Relationships.HAS_CHAMBER_VALET, "7i"));

        }}, "7"));
    }

    public static Relationships getRelationshipBetweenParentAndChild(Nodes childNodeType) {
        for (ParentChildInfo parentChildInfo : parentChildList) {
            if (parentChildInfo.childNodes.containsKey(childNodeType)) {
                return parentChildInfo.childNodes.get(childNodeType).relation;
            }
        }
        return null;
    }

    public String getTagForChildNode(Nodes childNodeType) {
        for (ParentChildInfo parentChildInfo : parentChildList) {
            if (parentChildInfo.childNodes.containsKey(childNodeType)) {
                return parentChildInfo.childNodes.get(childNodeType).tag;
            }
        }
        return null;
    }

    public String getPrefixForParentNode(Nodes parentNode) {
        for (ParentChildInfo parentChildInfo : parentChildList) {
            if (parentChildInfo.parentNode == parentNode) {
                return parentChildInfo.prefix;
            }
        }
        return null;
    }
    public static List<Nodes> getAllChildren(){
        List<Nodes> children = new ArrayList<>();
        for (ParentChildInfo parentChildInfo : parentChildList) {
             children.addAll(parentChildInfo.childNodes.keySet());
        }
        return children;
    }
}
