package com.vulnlab.util;

import java.util.regex.Pattern;

/**
 * 路径验证工具类 - 用于路径遍历安全检查
 *
 * @author VulnLab
 */
public class PathUtil {

    // 文件名白名单模式（只允许字母、数字、下划线、点、短横线）
    private static final Pattern FILENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_.-]+$");

    /**
     * 验证文件名是否安全
     *
     * @param filename 文件名
     * @return 安全返回true，否则返回false
     */
    public static boolean isValidFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }

        // 检查是否包含路径遍历字符
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return false;
        }

        // 检查是否以点开头（隐藏文件）
        if (filename.startsWith(".")) {
            return false;
        }

        // 使用白名单验证
        return FILENAME_PATTERN.matcher(filename).matches();
    }

    /**
     * 清理路径，防止路径遍历
     *
     * @param path 原始路径
     * @return 清理后的路径
     */
    public static String sanitizePath(String path) {
        if (path == null) {
            return "";
        }

        // 移除路径遍历字符
        return path.replaceAll("\\.\\.", "")
                   .replaceAll("/", "")
                   .replaceAll("\\\\", "");
    }
}
