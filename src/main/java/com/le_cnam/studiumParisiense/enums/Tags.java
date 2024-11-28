package com.le_cnam.studiumParisiense.enums;

public enum Tags {
    TAG_1A("1a", "id"),
    TAG_1B("1b", "name"),
    TAG_1C("1c", "variant"),
    TAG_1D("1d", "shortDescription"),
    TAG_1E("1e", "datesOfLife"),
    TAG_1F("1f", "activityPeriod"),
    TAG_1H("1h", "genderIndication"),
    TAG_1K("1k", "statusAtParis"),
    TAG_1G("1g", "medianActivityDate"),

    TAG_2A("2a", "birthLocation"),
    TAG_2B("2b", "originDiocese"),
    TAG_2C("2c", "parisArrivalDeparture"),

    TAG_3A("3a", "socialClass"),
    TAG_3B("3b", "familyNetwork"),
    TAG_3C("3c", "personalSocialClass"),
    TAG_3D("3d", "personalServiceRelation"),
    TAG_3E("3e", "friendOrEnemy"),
    TAG_3F("3f", "intellectualDebateInvolvement"),
    TAG_3G("3g", "ongoingCorrespondence"),
    TAG_3H("3h", "particularGroup"),
    TAG_3I("3i", "specialPoliticalConnections"),
    TAG_3J("3j", "collaborationConnection"),
    TAG_3K("3k", "executorConnection"),
    TAG_3L("3l", "masterStudentConnection"),
    TAG_5A("5a", "preUniversityEducation"),
    TAG_5B("5b", "universityOrStudium"),
    TAG_5C("5c", "curriculum"),
    TAG_5E("5e", "informationCollege"),
    TAG_5F("5f", "foundationCollege"),
    TAG_6A("6a", "uncertainEcclesiasticalStatus"),
    TAG_6B("6b", "secularEcclesiastic"),
    TAG_6C("6c", "secularBeneficesHeldByRegularEcclesiastic"),
    TAG_6D("6d", "regularEcclesiastic"),
    TAG_6F("6f", "hierarchicalPositionInRegularOrder"),
    TAG_6I("6i", "papalFunctions"),
    TAG_6J("6j", "ecclesiasticalFunctions"),
    TAG_6K("6k", "communityFoundation"),
    TAG_7A("7a", "schoolMasterOrTutor"),
    TAG_7B("7b", "university"),
    TAG_7C("7c", "legalProfessional"),
    TAG_7D("7d", "propertyAdministrator"),
    TAG_7G("7g", "religiousFunctionsForCourt"),
    TAG_7H("7h", "culturalFunctionsForCourt"),

    TAG_7I("7i", "chamberValet"),
    TAG_7K("7k", "localAdministrationFunctions"),
    TAG_7L("7l", "representationsIncludingEcclesiasticalDiplomacy"),
    TAG_7M("7m", "merchantArtisanSoldier"),
    TAG_7N("7n", "doctorSurgeon"),
    TAG_7S("7s", "diverseProfessions"),

    TAG_7J("7j", "administrationRoyal"),
    TAG_8A("8a", "politicalPosition"),
    TAG_8D("8d", "imprisoned"),
    TAG_8E("8e", "violentDeath"),
    TAG_8F("8f", "exiled"),
    TAG_8G("8g", "relatedLegalAction"),
    TAG_9("9", "travel"),
    TAG_10A("10a", "universityCommission"),
    TAG_10B("10b", "otherCommission"),
    TAG_11A("11a", "housingInParis"),
    TAG_11B("11b", "housingOutsideParis"),
    TAG_12A("12a", "income"),
    TAG_13A("13a", "willTestament"),
    TAG_14A("14a", "donationLegacy"),
    TAG_15A("15a", "coatOfArms"),
    TAG_15B("15b", "seal"),
    TAG_16A("16a", "sermons"),
    TAG_17("17", "otherActivity"),
    TAG_17A("17a", "otherActivity"),
    TAG_18A("18a", "library"),
    TAG_R("r", "reference"),

    TAG_COMMENT("comment", "comment"),

    TAG_BIBLIOGRAPHY("bibliography", "value");

    private final String code;
    private final String value;

    Tags(String code, String value) {
        this.code = code;
        this.value = value;
    }

    public String getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public static Tags fromCode(String code) {
        for (Tags tag : Tags.values()) {
            if (tag.getCode().equals(code)) {
                return tag;
            }
        }
        if (code.matches("^(1[9]|[2-9][0-9]).*")) {
            return TAG_BIBLIOGRAPHY;
        }
        throw new IllegalArgumentException("No tag found for the code: " + code);
    }
}
