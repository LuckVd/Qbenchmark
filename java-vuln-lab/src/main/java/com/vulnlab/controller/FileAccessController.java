package com.vulnlab.controller;

import com.vulnlab.util.PathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/v1/file")
public class FileAccessController {

    private static final Logger logger = LoggerFactory.getLogger(FileAccessController.class);
    private static final String BASE_DIR = "/var/www/html/files";

    @GetMapping("/read")
    public String read(@RequestParam("name") String name) {
        try {
            String filepath = BASE_DIR + "/" + name;
            logger.info("Reading file: {}", filepath);

            byte[] content = Files.readAllBytes(Paths.get(filepath));
            return new String(content);
        } catch (IOException e) {
            logger.error("File read error", e);
            return "Error reading file: " + e.getMessage();
        }
    }

    @GetMapping("/image")
    public String image(@RequestParam("file") String file) {
        try {
            String imagePath = "/var/www/html/images/" + file;
            logger.info("Reading image: {}", imagePath);

            byte[] content = Files.readAllBytes(Paths.get(imagePath));
            return java.util.Base64.getEncoder().encodeToString(content);
        } catch (IOException e) {
            logger.error("Image read error", e);
            return "Error reading image: " + e.getMessage();
        }
    }

    @GetMapping("/log")
    public String log(@RequestParam("date") String date) {
        try {
            String logPath = "/var/log/app/" + date + ".log";
            logger.info("Reading log: {}", logPath);

            byte[] content = Files.readAllBytes(Paths.get(logPath));
            return new String(content);
        } catch (IOException e) {
            logger.error("Log read error", e);
            return "Error reading log: " + e.getMessage();
        }
    }

    @GetMapping("/absolute")
    public String absolute(@RequestParam("path") String path) {
        try {
            logger.info("Reading file from absolute path: {}", path);

            byte[] content = Files.readAllBytes(Paths.get(path));
            return new String(content);
        } catch (IOException e) {
            logger.error("File read error", e);
            return "Error reading file: " + e.getMessage();
        }
    }

    @GetMapping("/encoded")
    public String encoded(@RequestParam("file") String file) {
        try {
            String decoded = file;
            for (int i = 0; i < 3; i++) {
                try {
                    decoded = java.net.URLDecoder.decode(decoded, "UTF-8");
                } catch (Exception e) {
                    break;
                }
            }

            String filepath = BASE_DIR + "/" + decoded;
            logger.info("Reading file: {}", filepath);

            byte[] content = Files.readAllBytes(Paths.get(filepath));
            return new String(content);
        } catch (IOException e) {
            logger.error("File read error", e);
            return "Error reading file: " + e.getMessage();
        }
    }

    @GetMapping("/read/sec")
    public String readSecure(@RequestParam("name") String name) {
        try {
            if (!PathUtil.isValidFilename(name)) {
                logger.warn("Invalid filename detected: {}", name);
                return "Invalid filename";
            }

            String fullPath = Paths.get(BASE_DIR, name).normalize().toString();

            if (!fullPath.startsWith(BASE_DIR)) {
                logger.warn("Path traversal attempt detected: {}", name);
                return "Access denied";
            }

            logger.info("Reading file: {}", fullPath);
            byte[] content = Files.readAllBytes(Paths.get(fullPath));
            return new String(content);
        } catch (IOException e) {
            logger.error("File read error", e);
            return "Error reading file: " + e.getMessage();
        }
    }
}
