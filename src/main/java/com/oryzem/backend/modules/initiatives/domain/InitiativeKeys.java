package com.oryzem.backend.modules.initiatives.domain;

public final class InitiativeKeys {

    private InitiativeKeys() {
    }

    public static String yearPk(String year) {
        return "YEAR#" + year;
    }

    public static String initiativeSk(String type, String status, String nameLower, String id) {
        return "TYPE#" + type
                + "#STATUS#" + status
                + "#NAME#" + nameLower
                + "#ID#" + id;
    }
}
