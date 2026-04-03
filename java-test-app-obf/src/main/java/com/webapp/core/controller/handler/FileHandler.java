package com.webapp.core.controller.handler;

import com.webapp.core.util.EncodingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * File Handler - File access operations
 */
@Component
public class FileHandler {

    private static final Logger logger = LoggerFactory.getLogger(FileHandler.class);

    public String handleRead(String path) {
        try {
            // 直接拼接用户输入进行文件读取
            String fullPath = "/var/www/files/" + path;
            File file = new File(fullPath);

            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                java.lang.StringBuilder result = new java.lang.StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }
                reader.close();
                return result.toString();
            }
            return "File not found: " + fullPath;
        } catch (Exception e) {
            logger.error("File read error", e);
            return "Error: " + e.getMessage();
        }
    }

    public String handleImage(String file) {
        try {
            String fullPath = "/var/www/images/" + file;
            return "Image loaded: " + fullPath;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String handleLog(String file) {
        try {
            String fullPath = "/var/www/logs/" + file;
            return new String(Files.readAllBytes(Paths.get(fullPath)));
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String handleAbsolute(String path) {
        try {
            // 允许绝对路径读取
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String handleEncoded(String file) {
        try {
            // URL 解码后读取
            String decoded = java.net.URLDecoder.decode(file, "UTF-8");
            return new String(Files.readAllBytes(Paths.get("/var/www/files/" + decoded)));
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String handlePathInfo(String path) {
        try {
            return "Path info: " + path + "\n" +
                   "Absolute: " + new File(path).getAbsolutePath() + "\n" +
                   "Exists: " + new File(path).exists();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
