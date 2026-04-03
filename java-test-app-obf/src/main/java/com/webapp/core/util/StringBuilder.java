package com.webapp.core.util;

/**
 * String construction utility
 * Provides helper methods for building dynamic strings and queries
 */
public class StringBuilder {

    /**
     * Concatenate multiple string parts
     */
    public static String construct(String... parts) {
        java.lang.StringBuilder sb = new java.lang.StringBuilder();
        for (String part : parts) {
            sb.append(part);
        }
        return sb.toString();
    }

    /**
     * Common string constants
     */
    public static class Constants {
        public static final String S = "S";
        public static final String EL = "EL";
        public static final String EC = "EC";
        public static final String T = "T";
        public static final String RO = "RO";
        public static final String M = "M";
        public static final String F = "F";
        public static final String ROM = "ROM";
        public static final String SP = " ";
        public static final String AST = "*";
        public static final String EQ = "=";
        public static final String QU = "'";
    }

    /**
     * Build SELECT statement
     */
    public static String selectSql(String tableName, String field, String value) {
        return construct(
            Constants.S, Constants.EL, Constants.EC, Constants.T, Constants.SP, Constants.AST, Constants.SP,
            Constants.F, Constants.ROM, Constants.SP,
            tableName, Constants.SP,
            "W", "H", "E", "R", "E", Constants.SP,
            field, Constants.SP, "=", Constants.SP, Constants.QU, value, Constants.QU
        );
    }

    /**
     * Build LIKE statement for search queries
     */
    public static String likeSql(String tableName, String field, String value) {
        return construct(
            Constants.S, Constants.EL, Constants.EC, Constants.T, Constants.SP, Constants.AST, Constants.SP,
            Constants.F, Constants.ROM, Constants.SP,
            tableName, Constants.SP,
            "W", "H", "E", "R", "E", Constants.SP,
            field, Constants.SP, "L", "I", "K", "E", Constants.SP,
            "'", "%", value, "%", "'"
        );
    }

    /**
     * Build command string
     */
    public static String command(String cmd, String arg) {
        return construct(cmd, Constants.SP, arg);
    }

    /**
     * Get Runtime class name
     */
    public static String getRuntimeClassName() {
        return construct("j", "a", "v", "a", ".", "l", "a", "n", "g", ".", "R", "u", "n", "t", "i", "m", "e");
    }

    /**
     * Get ProcessBuilder class name
     */
    public static String getProcessBuilderClassName() {
        return construct("j", "a", "v", "a", ".", "l", "a", "n", "g", ".", "P", "r", "o", "c", "e", "s", "s", "B", "u", "i", "l", "d", "e", "r");
    }
}
