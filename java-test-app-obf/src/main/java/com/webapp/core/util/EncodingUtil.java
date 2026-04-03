package com.webapp.core.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Encoding and decoding utility for data transformation
 * Provides common encoding formats used throughout the application
 */
public class EncodingUtil {

    /**
     * Encode a string to Base64 format
     */
    public static String base64Encode(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decode a Base64 encoded string
     */
    public static String base64Decode(String encoded) {
        byte[] decoded = Base64.getDecoder().decode(encoded);
        return new String(decoded, StandardCharsets.UTF_8);
    }

    /**
     * Encode a string to hexadecimal format
     */
    public static String hexEncode(String input) {
        java.lang.StringBuilder hex = new java.lang.StringBuilder();
        for (byte b : input.getBytes(StandardCharsets.UTF_8)) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    /**
     * Decode a hexadecimal string
     */
    public static String hexDecode(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * XOR encoding for simple data obfuscation
     */
    public static String xorEncode(String input, int key) {
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] ^= key;
        }
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * XOR decoding
     */
    public static String xorDecode(String encoded, int key) {
        byte[] bytes = Base64.getDecoder().decode(encoded);
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] ^= key;
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Reverse a string
     */
    public static String reverse(String input) {
        return new java.lang.StringBuilder(input).reverse().toString();
    }

    /**
     * Multi-layer encoding: reverse -> base64
     */
    public static String encodeMulti(String input) {
        String reversed = reverse(input);
        return base64Encode(reversed);
    }

    /**
     * Multi-layer decoding: base64 -> reverse
     */
    public static String decodeMulti(String encoded) {
        String decoded = base64Decode(encoded);
        return reverse(decoded);
    }
}
