package com.vulnlab.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/file")
public class FileStoreController {

    private static final Logger logger = LoggerFactory.getLogger(FileStoreController.class);
    private static final String UPLOAD_DIR = System.getProperty("java.io.tmpdir");

    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        if (file.isEmpty()) {
            result.put("error", "File is empty");
            return result;
        }

        try {
            String filename = file.getOriginalFilename();
            logger.info("Uploading file: {}", filename);

            Path path = Paths.get(UPLOAD_DIR, filename);
            Files.write(path, file.getBytes());

            result.put("message", "File uploaded successfully");
            result.put("filename", filename);
            result.put("path", path.toString());
            result.put("size", file.getSize());
            result.put("url", "/uploads/" + filename);

            logger.info("File uploaded to: {}", path);
        } catch (Exception e) {
            logger.error("Upload error", e);
            result.put("error", "Upload failed: " + e.getMessage());
        }

        return result;
    }

    @PostMapping("/picture")
    public Map<String, Object> picture(@RequestParam("file") MultipartFile file,
                                       @RequestParam(value = "type", defaultValue = "image") String type) {
        Map<String, Object> result = new HashMap<>();

        if (file.isEmpty()) {
            result.put("error", "File is empty");
            return result;
        }

        try {
            String filename = file.getOriginalFilename();
            logger.info("Uploading picture: {}, type: {}", filename, type);

            if (isValidImageExtension(filename)) {
                Path path = Paths.get(UPLOAD_DIR, filename);
                Files.write(path, file.getBytes());

                result.put("message", "Picture uploaded successfully");
                result.put("filename", filename);
                result.put("path", path.toString());
                result.put("type", type);
                result.put("url", "/uploads/" + filename);

                logger.info("Picture uploaded to: {}", path);
            } else {
                result.put("error", "Invalid file type. Only images allowed.");
            }
        } catch (Exception e) {
            logger.error("Picture upload error", e);
            result.put("error", "Upload failed: " + e.getMessage());
        }

        return result;
    }

    @PostMapping("/base64")
    public Map<String, Object> base64(@RequestParam("filename") String filename,
                                       @RequestParam("content") String content) {
        Map<String, Object> result = new HashMap<>();

        try {
            byte[] decoded = java.util.Base64.getDecoder().decode(content);
            Path path = Paths.get(UPLOAD_DIR, filename);
            Files.write(path, decoded);

            result.put("message", "File uploaded successfully");
            result.put("filename", filename);
            result.put("path", path.toString());
            result.put("size", decoded.length);

            logger.info("Base64 file uploaded to: {}", path);
        } catch (Exception e) {
            logger.error("Base64 upload error", e);
            result.put("error", "Upload failed: " + e.getMessage());
        }

        return result;
    }

    @GetMapping("/list")
    public Map<String, Object> list() {
        Map<String, Object> result = new HashMap<>();

        try {
            File uploadDir = new File(UPLOAD_DIR);
            String[] files = uploadDir.list();

            result.put("uploadDir", UPLOAD_DIR);
            result.put("files", files);
            result.put("count", files != null ? files.length : 0);

            logger.info("Listed {} files in {}", files != null ? files.length : 0, UPLOAD_DIR);
        } catch (Exception e) {
            logger.error("List files error", e);
            result.put("error", "List failed: " + e.getMessage());
        }

        return result;
    }

    private boolean isValidImageExtension(String filename) {
        if (filename == null) return false;

        String lower = filename.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
               lower.endsWith(".png") || lower.endsWith(".gif") ||
               lower.endsWith(".bmp");
    }
}
