package com.vulnobf.util;

/**
 * String obfuscation utility
 * Splits and recombines strings to hide direct string literals
 */
public class StringObfuscator {

    /**
     * Reconstruct a string from split parts
     */
    public static String reconstruct(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            sb.append(part);
        }
        return sb.toString();
    }

    /**
     * Common string parts for obfuscation
     */
    public static class Parts {
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
        public static final String SEMI = ";";
    }

    /**
     * Build SELECT statement from parts
     */
    public static String selectSql(String tableName, String field, String value) {
        return reconstruct(
            Parts.S, Parts.EL, Parts.EC, Parts.T, Parts.SP, Parts.AST, Parts.SP,
            Parts.F, Parts.ROM, Parts.SP,
            tableName, Parts.SP,
            "W", "H", "E", "R", "E", Parts.SP,
            field, Parts.SP, "=", Parts.SP, Parts.QU, value, Parts.QU
        );
    }

    /**
     * Build LIKE statement from parts
     */
    public static String likeSql(String tableName, String field, String value) {
        return reconstruct(
            Parts.S, Parts.EL, Parts.EC, Parts.T, Parts.SP, Parts.AST, Parts.SP,
            Parts.F, Parts.ROM, Parts.SP,
            tableName, Parts.SP,
            "W", "H", "E", "R", "E", Parts.SP,
            field, Parts.SP, "L", "I", "K", "E", Parts.SP,
            "'", "%", value, "%", "'"
        );
    }

    /**
     * Build command string
     */
    public static String command(String cmd, String arg) {
        return reconstruct(cmd, Parts.SP, arg);
    }

    /**
     * Get the Runtime class via string construction
     */
    public static String getRuntimeClassName() {
        return reconstruct("j", "a", "v", "a", ".", "l", "a", "n", "g", ".", "R", "u", "n", "t", "i", "m", "e");
    }

    /**
     * Get the ProcessBuilder class via string construction
     */
    public static String getProcessBuilderClassName() {
        return reconstruct("j", "a", "v", "a", ".", "l", "a", "n", "g", ".", "P", "r", "o", "c", "e", "s", "s", "B", "u", "i", "l", "d", "e", "r");
    }
}
