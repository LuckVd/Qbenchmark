package com.vulnlab.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传漏洞演示
 *
 * 漏洞说明：
 * 1. 未验证文件类型，允许上传任意文件
 * 2. 仅检查文件扩展名，可通过双扩展名绕过
 * 3. 上传的文件存储在可访问的目录，可能导致 Webshell 执行
 *
 * 修复方案：
 * 1. 验证文件的真实类型（Magic Number）
 * 2. 限制上传目录的执行权限
 * 3. 重命名上传的文件
 * 4. 使用白名单验证文件类型
 *
 * @author VulnLab
 */
@RestController
@RequestMapping("/file")
public class FileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    // 上传目录
    private static final String UPLOAD_DIR = System.getProperty("java.io.tmpdir");

    /**
     * 文件上传漏洞 - 任意文件上传
     *
     * 漏洞原理：不验证文件类型，允许上传任意文件
     * 攻击向量：上传 JSP Webshell 获取服务器控制权
     *
     * 测试步骤：
     * 1. 创建恶意 JSP 文件
     * 2. 上传到服务器
     * 3. 访问上传的文件执行恶意代码
     *
     * 命令示例:
     * curl -F "file=@shell.jsp" http://localhost:8080/file/upload
     *
     * @param file 上传的文件
     * @return 上传结果
     */
    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        if (file.isEmpty()) {
            result.put("error", "File is empty");
            return result;
        }

        try {
            // 获取文件名
            String filename = file.getOriginalFilename();
            logger.info("Uploading file: {}", filename);

            // 漏洞代码：不验证文件类型，直接保存
            Path path = Paths.get(UPLOAD_DIR, filename);
            Files.write(path, file.getBytes());

            result.put("message", "File uploaded successfully");
            result.put("filename", filename);
            result.put("path", path.toString());
            result.put("size", file.getSize());
            result.put("url", "/uploads/" + filename);

            logger.info("File uploaded to: {}", path);
        } catch (IOException e) {
            logger.error("Upload error", e);
            result.put("error", "Upload failed: " + e.getMessage());
        }

        return result;
    }

    /**
     * 文件上传漏洞 - 伪装图片上传
     *
     * 漏洞原理：仅检查扩展名，可通过双扩展名绕过
     * 攻击向量：上传 shell.jpg.jsp 或 shell.jsp;.jpg
     *
     * 绕过技巧：
     * - shell.jpg.jsp (双扩展名)
     * - shell.jsp;.jpg (分号绕过)
     * - shell.jsp%00.jpg (空字节绕过)
     *
     * 命令示例:
     * curl -F "file=@shell.jpg.jsp" http://localhost:8080/file/upload/picture
     * curl -F "file=@shell.jsp" -F "type=image/jpeg" http://localhost:8080/file/upload/picture
     *
     * @param file 上传的文件
     * @param type 文件类型（用户可控）
     * @return 上传结果
     */
    @PostMapping("/upload/picture")
    public Map<String, Object> uploadPicture(@RequestParam("file") MultipartFile file,
                                              @RequestParam(value = "type", defaultValue = "image") String type) {
        Map<String, Object> result = new HashMap<>();

        if (file.isEmpty()) {
            result.put("error", "File is empty");
            return result;
        }

        try {
            String filename = file.getOriginalFilename();
            logger.info("Uploading picture: {}, type: {}", filename, type);

            // 漏洞代码：仅检查扩展名，不检查真实文件类型
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
                // 但用户可以通过双扩展名绕过：shell.jpg.jsp
                result.put("error", "Invalid file type. Only images allowed.");
            }
        } catch (IOException e) {
            logger.error("Picture upload error", e);
            result.put("error", "Upload failed: " + e.getMessage());
        }

        return result;
    }

    /**
     * 文件上传漏洞 - Base64 编码上传
     *
     * 漏洞原理：接受 Base64 编码的文件内容并解码保存
     * 攻击向量：绕过客户端文件类型检查
     *
     * 命令示例:
     * curl -d "filename=shell.jsp&content=$(cat shell.jsp | base64)" http://localhost:8080/file/upload/base64
     *
     * @param filename 文件名
     * @param content Base64 编码的文件内容
     * @return 上传结果
     */
    @PostMapping("/upload/base64")
    public Map<String, Object> uploadBase64(@RequestParam("filename") String filename,
                                             @RequestParam("content") String content) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 漏洞代码：解码 Base64 并保存，不验证文件内容
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

    /**
     * 列出上传目录的文件
     *
     * 命令示例:
     * curl http://localhost:8080/file/list
     *
     * @return 文件列表
     */
    @GetMapping("/list")
    public Map<String, Object> listFiles() {
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

    /**
     * 简单的扩展名检查（容易被绕过）
     */
    private boolean isValidImageExtension(String filename) {
        if (filename == null) return false;

        String lower = filename.toLowerCase();
        // 漏洞代码：仅检查扩展名，可通过 shell.jpg.jsp 绕过
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
               lower.endsWith(".png") || lower.endsWith(".gif") ||
               lower.endsWith(".bmp");
    }
}
