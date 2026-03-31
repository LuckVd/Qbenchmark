package com.vulnlab.controller;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 其他漏洞类型集合控制器
 *
 * 包含以下漏洞类型:
 * - CSV Injection (CSV 注入)
 * - Password Reset (密码重置漏洞)
 * - Login Bypass (登录绕过)
 * - Unauthorized Access (未授权访问)
 * - Blacklist Bypass (黑名单绕过)
 *
 * @author VulnLab
 */
@RestController
public class OtherVulnController {

    // ==================== CSV Injection ====================

    /**
     * CSV 注入漏洞 - 用户数据导出
     *
     * 漏洞代码：用户输入直接放入 CSV 文件，未过滤
     *
     * 攻击 payload: =cmd|' /C calc'!A0
     * 攻击 payload: =HYPERLINK("http://evil.com","Click me")
     * 攻击 payload: =10+20
     *
     * @param username 用户名
     * @param email 邮箱
     * @param amount 金额
     * @return CSV 文件
     */
    @GetMapping("/csv/export")
    public void csvExport(@RequestParam("username") String username,
                          @RequestParam("email") String email,
                          @RequestParam(value = "amount", defaultValue = "100") String amount,
                          HttpServletResponse response) throws IOException {

        // 漏洞代码：直接将用户输入写入 CSV
        String csv = String.format("username,email,amount\n%s,%s,%s", username, email, amount);

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=users.csv");

        PrintWriter writer = response.getWriter();
        writer.write(csv);
        writer.flush();
    }

    /**
     * CSV 注入漏洞 - 订单导出
     *
     * @param orderId 订单 ID（可注入恶意公式）
     * @return CSV 文件
     */
    @GetMapping("/csv/orders")
    public void csvOrders(@RequestParam("orderId") String orderId,
                           HttpServletResponse response) throws IOException {

        // 漏洞代码：订单 ID 直接写入 CSV
        String csv = String.format("orderId,product,amount\n%s,Product A,100", orderId);

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=orders.csv");

        PrintWriter writer = response.getWriter();
        writer.write(csv);
        writer.flush();
    }

    // ==================== Password Reset ====================

    // 模拟数据库
    private static final Map<String, User> USERS = new ConcurrentHashMap<>();
    private static final Map<String, ResetToken> RESET_TOKENS = new ConcurrentHashMap<>();

    static {
        USERS.put("user1", new User("user1", "user1@example.com", "password123"));
        USERS.put("admin", new User("admin", "admin@example.com", "admin123"));
    }

    /**
     * 密码重置漏洞 - Host 头注入
     *
     * 漏洞代码：重置链接使用 Host 头
     *
     * 攻击: Host: evil.com
     * 结果: 密码重置链接发送到 evil.com
     *
     * @param username 用户名
     * @param host Host 头
     * @return 重置结果
     */
    @PostMapping("/reset/request")
    public String resetRequest(@RequestParam("username") String username,
                                @RequestHeader(value = "Host", defaultValue = "localhost:8080") String host) {

        User user = USERS.get(username);
        if (user == null) {
            return "用户不存在";
        }

        // 漏洞代码：使用 Host 头构造重置链接
        String token = generateToken();
        RESET_TOKENS.put(token, new ResetToken(username, System.currentTimeMillis() + 3600000));

        String resetLink = String.format("http://%s/reset/confirm?token=%s", host, token);

        // 模拟发送邮件（实际会发送到 Host 指定的服务器）
        return String.format(
            "=== 密码重置邮件已发送 ===\n" +
            "用户: %s\n" +
            "邮箱: %s\n" +
            "重置链接: %s\n" +
            "\n[!] 漏洞: Host 头可被篡改，重置链接发送到攻击者服务器",
            username, user.email, resetLink
        );
    }

    /**
     * 密码重置漏洞 - 可预测的 Token
     *
     * 漏洞代码：Token 基于时间戳生成
     *
     * @param username 用户名
     * @return Token
     */
    @GetMapping("/reset/token")
    public String getResetToken(@RequestParam("username") String username) {
        // 漏洞代码：Token 是用户名 + 时间戳的简单编码
        String token = Base64.getEncoder().encodeToString((username + ":" + System.currentTimeMillis()).getBytes());

        return String.format(
            "=== 重置 Token ===\n" +
            "用户: %s\n" +
            "Token: %s\n" +
            "\n[!] 漏洞: Token 可预测",
            username, token
        );
    }

    /**
     * 密码重置漏洞 - Token 复用
     *
     * @param token Token
     * @param newPassword 新密码
     * @return 重置结果
     */
    @PostMapping("/reset/confirm")
    public String resetConfirm(@RequestParam("token") String token,
                                 @RequestParam("newPassword") String newPassword) {

        ResetToken resetToken = RESET_TOKENS.get(token);

        if (resetToken == null) {
            return "Token 无效";
        }

        // 漏洞代码：Token 验证后不删除，可以重复使用
        User user = USERS.get(resetToken.username);
        if (user != null) {
            user.password = newPassword;

            return String.format(
                "=== 密码重置成功 ===\n" +
                "用户: %s\n" +
                "新密码: %s\n" +
                "\n[!] 漏洞: Token 未删除，可重复使用",
                user.username, newPassword
            );
        }

        return "用户不存在";
    }

    // ==================== Login Bypass ====================

    /**
     * 登录绕过 - SQL 万能密码
     *
     * 漏洞代码：用户输入直接拼接到 SQL
     *
     * 攻击: username=admin' or '1'='1&password=anything
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录结果
     */
    @PostMapping("/login/sql")
    public String loginSql(@RequestParam("username") String username,
                            @RequestParam("password") String password) {

        // 漏洞代码：拼接 SQL（模拟）
        String sql = "SELECT * FROM users WHERE username='" + username + "' AND password='" + password + "'";

        // 模拟 SQL 注入检测
        boolean isSqlInjection = username.toLowerCase().contains("' or '1'='1")
            || username.toLowerCase().contains("' or 1=1")
            || username.contains("--");

        if (isSqlInjection) {
            return String.format(
                "=== 登录成功 ===\n" +
                "SQL: %s\n" +
                "用户: %s\n" +
                "\n[!] 漏洞: SQL 万能密码绕过认证",
                sql, username.split("'")[0]
            );
        }

        return "登录失败：用户名或密码错误";
    }

    /**
     * 登录绕过 - 密码为空检测
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录结果
     */
    @PostMapping("/login/empty")
    public String loginEmpty(@RequestParam("username") String username,
                              @RequestParam(value = "password", defaultValue = "") String password) {

        // 漏洞代码：密码为空时跳过验证
        if (password.isEmpty()) {
            return String.format(
                "=== 登录成功 ===\n" +
                "用户: %s\n" +
                "\n[!] 漏洞: 空密码绕过",
                username
            );
        }

        User user = USERS.get(username);
        if (user != null && user.password.equals(password)) {
            return "登录成功";
        }

        return "登录失败";
    }

    /**
     * 登录绕过 - 固定 Token
     *
     * @param username 用户名
     * @param token Token
     * @return 登录结果
     */
    @PostMapping("/login/token")
    public String loginToken(@RequestParam("username") String username,
                              @RequestParam("token") String token) {

        // 漏洞代码：admin 用户使用固定 Token "admin123"
        if ("admin".equals(username) && "admin123".equals(token)) {
            return "=== 管理员登录成功 ===\n[!] 漏洞: 固定 Token";
        }

        return "登录失败：Token 无效";
    }

    // ==================== Unauthorized Access ====================

    /**
     * 未授权访问 - 管理员面板
     *
     * 漏洞代码：没有身份验证
     *
     * @return 管理面板
     */
    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return String.format(
            "=== 管理员面板 ===\n" +
            "用户数: %d\n" +
            "订单数: 128\n" +
            "收入: ¥128,000\n" +
            "\n[!] 漏洞: 无需认证即可访问",
            USERS.size()
        );
    }

    /**
     * 未授权访问 - 用户列表
     *
     * @return 用户列表
     */
    @GetMapping("/admin/users")
    public String adminUsers() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 所有用户 ===\n");
        for (User user : USERS.values()) {
            sb.append(String.format("- %s: %s (密码: %s)\n", user.username, user.email, user.password));
        }
        sb.append("\n[!] 漏洞: 无需认证即可查看");
        return sb.toString();
    }

    /**
     * 未授权访问 - 配置文件
     *
     * @return 配置信息
     */
    @GetMapping("/admin/config")
    public String adminConfig() {
        return String.format(
            "=== 系统配置 ===\n" +
            "数据库: mysql://localhost:3306/qbench\n" +
            "API Key: sk_live_xxxxxxxxxxxx\n" +
            "Secret: my_secret_key_123\n" +
            "Debug: true\n" +
            "\n[!] 漏洞: 敏感配置泄露",
            System.currentTimeMillis()
        );
    }

    // ==================== Blacklist Bypass ====================

    /**
     * 文件扩展名绕过
     *
     * @param filename 文件名
     * @return 上传结果
     */
    @PostMapping("/upload/extension")
    public String uploadExtension(@RequestParam("filename") String filename) {

        // 简单黑名单
        String[] blacklist = {".exe", ".jsp", ".php"};

        // 漏洞代码：只检查后缀，可以绕过
        boolean isBlacklisted = false;
        for (String ext : blacklist) {
            if (filename.endsWith(ext)) {
                isBlacklisted = true;
                break;
            }
        }

        if (isBlacklisted) {
            return "上传失败：不允许的文件类型";
        }

        return String.format(
            "=== 上传成功 ===\n" +
            "文件名: %s\n" +
            "\n[!] 漏洞: 可使用绕过技巧",
            filename
        );
    }

    /**
     * MIME 类型绕过
     *
     * @param filename 文件名
     * @param contentType Content-Type
     * @return 上传结果
     */
    @PostMapping("/upload/mime")
    public String uploadMime(@RequestParam("filename") String filename,
                              @RequestParam("contentType") String contentType) {

        // 漏洞代码：只检查 Content-Type，客户端可控
        if (!contentType.startsWith("image/")) {
            return "上传失败：只允许图片";
        }

        return String.format(
            "=== 上传成功 ===\n" +
            "文件名: %s\n" +
            "Content-Type: %s\n" +
            "\n[!] 漏洞: Content-Type 可伪造",
            filename, contentType
        );
    }

    /**
     * 双重扩展名绕过
     *
     * @param filename 文件名
     * @return 上传结果
     */
    @PostMapping("/upload/double")
    public String uploadDouble(@RequestParam("filename") String filename) {

        // 漏洞代码：只取第一个点后的扩展名
        String extension = filename.substring(filename.lastIndexOf('.') + 1);

        if (extension.equals("jsp") || extension.equals("php")) {
            return "上传失败：不允许 " + extension + " 文件";
        }

        return String.format(
            "=== 上传成功 ===\n" +
            "文件名: %s\n" +
            "检测扩展名: %s\n" +
            "\n[!] 漏洞: shell.jsp.jpg 可绕过",
            filename, extension
        );
    }

    /**
     * 信息端点
     *
     * @return 测试信息
     */
    @GetMapping("/other/info")
    public String info() {
        return String.format(
            "其他漏洞类型演示%n" +
            "================%n" +
            "Java Version: %s%n" +
            "OS: %s %s%n" +
            "%n" +
            "CSV Injection:%n" +
            "- GET /csv/export?username=&email=&amount=%n" +
            "- GET /csv/orders?orderId=%n" +
            "%n" +
            "Password Reset:%n" +
            "- POST /reset/request?username=user1%n" +
            "- GET /reset/token?username=user1%n" +
            "- POST /reset/confirm?token=&newPassword=%n" +
            "%n" +
            "Login Bypass:%n" +
            "- POST /login/sql?username=&password=%n" +
            "- POST /login/empty?username=user1&password=%n" +
            "- POST /login/token?username=admin&token=admin123%n" +
            "%n" +
            "Unauthorized Access:%n" +
            "- GET /admin/dashboard%n" +
            "- GET /admin/users%n" +
            "- GET /admin/config%n" +
            "%n" +
            "Blacklist Bypass:%n" +
            "- POST /upload/extension?filename=%n" +
            "- POST /upload/mime?filename=&contentType=%n" +
            "- POST /upload/double?filename=%n",
            System.getProperty("java.version"),
            System.getProperty("os.name"),
            System.getProperty("os.version")
        );
    }

    // ==================== 辅助方法 ====================

    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    // ==================== 数据模型 ====================

    static class User {
        String username;
        String email;
        String password;

        User(String username, String email, String password) {
            this.username = username;
            this.email = email;
            this.password = password;
        }
    }

    static class ResetToken {
        String username;
        long expireTime;

        ResetToken(String username, long expireTime) {
            this.username = username;
            this.expireTime = expireTime;
        }
    }
}
