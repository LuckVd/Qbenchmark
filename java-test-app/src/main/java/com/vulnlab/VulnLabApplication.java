package com.vulnlab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Mini Java Vulnerability Lab - 主应用程序
 * 小型Java安全漏洞靶场
 *
 * 包含以下漏洞类型：
 * 1. SQL注入 (SQL Injection)
 * 2. SSRF (服务端请求伪造)
 * 3. 命令注入 (Command Injection)
 * 4. XSS (跨站脚本)
 * 5. Log4j (Log4Shell)
 * 6. 路径遍历 (Path Traversal)
 */
@SpringBootApplication
public class VulnLabApplication {

    public static void main(String[] args) {
        SpringApplication.run(VulnLabApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("  Mini Java Vulnerability Lab Started!");
        System.out.println("  http://localhost:8080");
        System.out.println("========================================\n");
    }
}
