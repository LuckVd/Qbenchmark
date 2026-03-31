package com.vulnlab.controller;

import com.vulnlab.util.PathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 路径遍历 (Path Traversal) 漏洞演示
 *
 * 漏洞说明：
 * - 应用程序未正确验证用户输入的文件路径
 * - 攻击者可使用 ../ 等特殊字符访问任意文件
 *
 * 常用payload：
 * - ../../../etc/passwd
 * - ..\..\..\..\windows\win.ini (Windows)
 * - ....// (URL编码绕过)
 * - %2e%2e%2f (双重编码)
 * - ..%252f (URL编码后再编码)
 * - ....// (容器自动解码)
 * - /etc/passwd (绝对路径)
 *
 * 修复方案：
 * 1. 使用白名单验证文件名
 * 2. 规范化路径后检查是否在允许目录内
 * 3. 不使用用户输入直接构造文件路径
 * 4. 使用文件ID/UUID代替文件名
 *
 * @author VulnLab
 */
@RestController
@RequestMapping("/path")
public class PathTraversalController {

    private static final Logger logger = LoggerFactory.getLogger(PathTraversalController.class);
    private static final String BASE_DIR = "/var/www/html/files";

    /**
     * 路径遍历漏洞 - 文件读取
     *
     * 测试用例：
     * - 正常: http://localhost:8080/path/traversal/vuln?filename=test.txt
     * - 读取/etc/passwd: http://localhost:8080/path/traversal/vuln?filename=../../../../../../etc/passwd
     * - 读取应用源码: http://localhost:8080/path/traversal/vuln?filename=../../../../../../app/application.properties
     * - Windows: http://localhost:8080/path/traversal/vuln?filename=..\\..\\..\\..\\windows\\win.ini
     *
     * @param filename 文件名
     * @return 文件内容
     */
    @GetMapping("/traversal/vuln")
    public String pathTraversalVuln(@RequestParam("filename") String filename) {
        try {
            // 漏洞代码：直接拼接用户输入的路径
            String filepath = BASE_DIR + "/" + filename;
            logger.info("Reading file: {}", filepath);

            byte[] content = Files.readAllBytes(Paths.get(filepath));
            return new String(content);
        } catch (IOException e) {
            logger.error("File read error", e);
            return "Error reading file: " + e.getMessage();
        }
    }

    /**
     * 路径遍历漏洞 - 图片下载
     *
     * 测试用例：
     * - 正常: http://localhost:8080/path/image/vuln?file=avatar.jpg
     * - 下载任意文件: http://localhost:8080/path/image/vuln?file=../../etc/passwd
     *
     * @param file 图片文件名
     * @return Base64编码的文件内容
     */
    @GetMapping("/image/vuln")
    public String imageVuln(@RequestParam("file") String file) {
        try {
            // 漏洞代码：直接使用用户输入
            String imagePath = "/var/www/html/images/" + file;
            logger.info("Reading image: {}", imagePath);

            byte[] content = Files.readAllBytes(Paths.get(imagePath));
            return java.util.Base64.getEncoder().encodeToString(content);
        } catch (IOException e) {
            logger.error("Image read error", e);
            return "Error reading image: " + e.getMessage();
        }
    }

    /**
     * 路径遍历漏洞 - 日志查看
     *
     * 测试用例：
     * - 正常: http://localhost:8080/path/log/vuln?date=2024-01-01
     * - 读取其他文件: http://localhost:8080/path/log/vuln?date=../../../../../etc/passwd
     *
     * @param date 日期
     * @return 日志内容
     */
    @GetMapping("/log/vuln")
    public String logVuln(@RequestParam("date") String date) {
        try {
            // 漏洞代码：日期参数直接用于路径构造
            String logPath = "/var/log/app/" + date + ".log";
            logger.info("Reading log: {}", logPath);

            byte[] content = Files.readAllBytes(Paths.get(logPath));
            return new String(content);
        } catch (IOException e) {
            logger.error("Log read error", e);
            return "Error reading log: " + e.getMessage();
        }
    }

    /**
     * 路径遍历漏洞 - 绝对路径访问
     *
     * 测试用例：
     * - 直接读取: http://localhost:8080/path/absolute/vuln?path=/etc/passwd
     * - Windows: http://localhost:8080/path/absolute/vuln?path=C:/windows/win.ini
     *
     * @param path 绝对路径
     * @return 文件内容
     */
    @GetMapping("/absolute/vuln")
    public String absolutePathVuln(@RequestParam("path") String path) {
        try {
            // 漏洞代码：直接使用绝对路径
            logger.info("Reading file from absolute path: {}", path);

            byte[] content = Files.readAllBytes(Paths.get(path));
            return new String(content);
        } catch (IOException e) {
            logger.error("File read error", e);
            return "Error reading file: " + e.getMessage();
        }
    }

    /**
     * 路径遍历漏洞 - URL编码绕过
     *
     * 测试用例：
     * - 双重编码: http://localhost:8080/path/encode/vuln?file=%252e%252e%252f%252e%252e%252f%252e%252e%252fetc%252fpasswd
     * - URL编码: http://localhost:8080/path/encode/vuln?file=%2e%2e%2f%2e%2e%2f%2e%2e%2fetc%2fpasswd
     *
     * @param file 编码的文件名
     * @return 文件内容
     */
    @GetMapping("/encode/vuln")
    public String encodeVuln(@RequestParam("file") String file) {
        try {
            // 尝试多次URL解码（模拟某些容器的行为）
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

    /**
     * 路径遍历安全代码 - 路径验证
     *
     * 测试用例：
     * - 正常: http://localhost:8080/path/traversal/sec?filename=test.txt
     * - 被拒绝: http://localhost:8080/path/traversal/sec?filename=../../etc/passwd
     *
     * @param filename 文件名
     * @return 文件内容或错误信息
     */
    @GetMapping("/traversal/sec")
    public String pathTraversalSecure(@RequestParam("filename") String filename) {
        try {
            // 安全代码：验证文件名
            if (!PathUtil.isValidFilename(filename)) {
                logger.warn("Invalid filename detected: {}", filename);
                return "Invalid filename";
            }

            // 规范化路径并确保在允许目录内
            String fullPath = Paths.get(BASE_DIR, filename).normalize().toString();

            if (!fullPath.startsWith(BASE_DIR)) {
                logger.warn("Path traversal attempt detected: {}", filename);
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
