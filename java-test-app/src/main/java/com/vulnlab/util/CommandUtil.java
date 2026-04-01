package com.vulnlab.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

/**
 * 命令执行工具类 - 用于命令注入漏洞演示
 *
 * @author VulnLab
 */
public class CommandUtil {

    private static final Logger logger = LoggerFactory.getLogger(CommandUtil.class);

    // IP地址验证正则
    private static final Pattern IP_PATTERN = Pattern.compile(
        "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}" +
        "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$"
    );

    /**
     * 使用Runtime.exec执行命令
     */
    public static String executeByRuntime(String cmd) throws IOException {
        StringBuilder result = new StringBuilder();

        Process process = Runtime.getRuntime().exec(cmd);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line).append("\n");
        }

        reader.close();
        return result.toString();
    }

    /**
     * 使用ProcessBuilder执行命令
     */
    public static String executeByProcessBuilder(String[] cmd) throws IOException {
        StringBuilder result = new StringBuilder();

        ProcessBuilder builder = new ProcessBuilder(cmd);
        builder.redirectErrorStream(true);
        Process process = builder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line).append("\n");
        }

        reader.close();
        return result.toString();
    }

    /**
     * 验证IP地址格式（安全检查）
     */
    public static boolean isValidIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        return IP_PATTERN.matcher(ip).matches();
    }
}
