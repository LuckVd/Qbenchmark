package com.envtest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Java Simple Vulnerability Lab - 环境验证靶场
 *
 * 这是一个简化的 Java 安全漏洞靶场，用于安全扫描器的环境构建检测。
 * 只包含单个命令注入漏洞端点，保持完整的 Java 项目框架结构。
 *
 * 启动后访问: http://localhost:8080
 */
@SpringBootApplication
public class SimpleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("  Java Simple Vuln Lab Started!");
        System.out.println("  http://localhost:8080");
        System.out.println("  环境验证靶场 - 用于扫描器检测");
        System.out.println("========================================\n");
    }
}
