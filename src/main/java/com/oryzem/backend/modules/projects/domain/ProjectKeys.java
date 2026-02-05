package com.oryzem.backend.modules.projects.domain;

public final class ProjectKeys {

    private ProjectKeys() {
    }

    public static String projectPk(String projectId) {
        return "PROJECT#" + projectId;
    }

    public static String metaSk() {
        return "META";
    }

    public static String milestoneSk(int als, Gate gate, Phase phase) {
        return "MS#ALS" + als + "#GATE#" + gate + "#PHASE#" + phase;
    }

    public static String datePk(String date) {
        return "DATE#" + date;
    }

    public static String dateSk(String projectId, int als, Gate gate, Phase phase) {
        return "PROJECT#" + projectId + "#ALS" + als + "#GATE#" + gate + "#PHASE#" + phase;
    }
}
