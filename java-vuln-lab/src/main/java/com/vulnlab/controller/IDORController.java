package com.vulnlab.controller;

import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IDOR (Insecure Direct Object Reference) 不安全的直接对象引用漏洞演示
 *
 * 漏洞说明：
 * - IDOR 是一种访问控制漏洞，攻击者可以直接访问其他用户的资源
 * - 属于 OWASP API Security Top 10 (API1:2019)
 * - 常见于 API 设计不当，未验证用户是否有权访问指定资源
 *
 * 漏洞原理：
 * 1. 应用使用可预测的标识符（如自增 ID、UUID）
 * 2. 未验证当前用户是否有权访问请求的资源
 * 3. 攻击者通过修改 URL 中的 ID 参数访问他人资源
 *
 * 攻击类型：
 * - 水平越权：访问同级用户的资源
 * - 垂直越权：普通用户访问管理员资源
 *
 * 常见场景：
 * - /user/profile?id=123 → 改为 id=124
 * - /api/order/456 → 改为 /api/order/457
 * - /admin/config → 普通用户直接访问
 *
 * 修复方案：
 * 1. 验证用户是否有权访问请求的资源
 * 2. 使用不可预测的标识符（如 UUID）
 * 3. 实施基于角色的访问控制 (RBAC)
 * 4. 记录访问日志用于审计
 *
 * @author VulnLab
 */
@RestController
@RequestMapping("/idor")
public class IDORController {

    // 模拟用户数据库
    private static final Map<String, User> USERS = new ConcurrentHashMap<>();
    private static final Map<Integer, Order> ORDERS = new ConcurrentHashMap<>();
    private static final Map<String, String> ADMIN_CONFIG = new ConcurrentHashMap<>();

    // 初始化测试数据
    static {
        // 创建测试用户
        USERS.put("user1", new User(1, "user1", "user1@example.com", "普通用户1", "user"));
        USERS.put("user2", new User(2, "user2", "user2@example.com", "普通用户2", "user"));
        USERS.put("admin", new User(100, "admin", "admin@example.com", "管理员", "admin"));

        // 创建测试订单
        ORDERS.put(1, new Order(1, 1, "iPhone 15 Pro", 9999.00, "已付款"));
        ORDERS.put(2, new Order(2, 1, "MacBook Pro", 19999.00, "已发货"));
        ORDERS.put(3, new Order(3, 2, "iPad Air", 4999.00, "待付款"));
        ORDERS.put(4, new Order(4, 2, "AirPods Pro", 1999.00, "已完成"));

        // 管理员配置
        ADMIN_CONFIG.put("site_name", "Qbenchmark 靶场");
        ADMIN_CONFIG.put("admin_email", "admin@qbench.com");
        ADMIN_CONFIG.put("secret_key", "sk_live_xxxxxxxxxxxx");
        ADMIN_CONFIG.put("debug_mode", "false");
    }

    /**
     * IDOR 漏洞 - 查看用户资料
     *
     * 漏洞代码：未验证当前用户是否有权查看目标用户
     *
     * 正常请求: GET /idor/user/profile?id=1&currentUser=user1
     * 攻击请求: GET /idor/user/profile?id=2&currentUser=user1
     *
     * @param id 目标用户 ID
     * @param currentUser 当前用户
     * @return 用户信息
     */
    @GetMapping("/user/profile")
    public String getUserProfile(@RequestParam("id") int id,
                                  @RequestParam(value = "currentUser", defaultValue = "user1") String currentUser) {
        // 漏洞代码：没有验证 currentUser 是否有权查看 id 的用户信息
        User user = USERS.values().stream()
            .filter(u -> u.id == id)
            .findFirst()
            .orElse(null);

        if (user == null) {
            return "用户不存在: ID=" + id;
        }

        // 返回完整的用户信息（包括敏感信息）
        return String.format(
            "=== 用户资料 ===\n" +
            "用户ID: %d\n" +
            "用户名: %s\n" +
            "邮箱: %s\n" +
            "真实姓名: %s\n" +
            "角色: %s\n" +
            "\n[!] IDOR 漏洞: 用户 %s 正在查看用户 %d 的资料",
            user.id, user.username, user.email, user.realName, user.role, currentUser, id
        );
    }

    /**
     * IDOR 漏洞 - 通过用户名查看资料
     *
     * 漏洞代码：未验证当前用户是否有权查看目标用户
     *
     * 正常请求: GET /idor/user/profile?username=user1&currentUser=user1
     * 攻击请求: GET /idor/user/profile?username=user2&currentUser=user1
     *
     * @param username 目标用户名
     * @param currentUser 当前用户
     * @return 用户信息
     */
    @GetMapping("/user/profile/byname")
    public String getUserProfileByName(@RequestParam("username") String username,
                                       @RequestParam(value = "currentUser", defaultValue = "user1") String currentUser) {
        // 漏洞代码：没有权限验证
        User user = USERS.get(username);

        if (user == null) {
            return "用户不存在: " + username;
        }

        return String.format(
            "=== 用户资料 ===\n" +
            "用户ID: %d\n" +
            "用户名: %s\n" +
            "邮箱: %s\n" +
            "真实姓名: %s\n" +
            "角色: %s\n" +
            "\n[!] IDOR 漏洞: 用户 %s 正在查看用户 %s 的资料",
            user.id, user.username, user.email, user.realName, user.role, currentUser, username
        );
    }

    /**
     * IDOR 漏洞 - 查看订单
     *
     * 漏洞代码：未验证订单是否属于当前用户
     *
     * 正常请求: GET /idor/order?id=1&userId=1
     * 攻击请求: GET /idor/order?id=3&userId=1 （查看 user2 的订单）
     *
     * @param id 订单 ID
     * @param userId 当前用户 ID
     * @return 订单信息
     */
    @GetMapping("/order")
    public String getOrder(@RequestParam("id") int id,
                           @RequestParam(value = "userId", defaultValue = "1") int userId) {
        // 漏洞代码：没有验证订单是否属于当前用户
        Order order = ORDERS.get(id);

        if (order == null) {
            return "订单不存在: ID=" + id;
        }

        // 返回订单详情（包含敏感信息）
        return String.format(
            "=== 订单详情 ===\n" +
            "订单ID: %d\n" +
            "用户ID: %d\n" +
            "商品: %s\n" +
            "金额: %.2f\n" +
            "状态: %s\n" +
            "\n[!] IDOR 漏洞: 用户 %d 正在查看用户 %d 的订单",
            order.id, order.userId, order.product, order.price, order.status, userId, order.userId
        );
    }

    /**
     * IDOR 漏洞 - 修改用户资料
     *
     * 漏洞代码：未验证是否有权修改目标用户
     *
     * 攻击请求: POST /idor/user/update?id=2&email=hacker@evil.com
     *
     * @param id 目标用户 ID
     * @param email 新邮箱
     * @param currentUser 当前用户
     * @return 操作结果
     */
    @PostMapping("/user/update")
    public String updateUser(@RequestParam("id") int id,
                             @RequestParam(value = "email", required = false) String email,
                             @RequestParam(value = "currentUser", defaultValue = "user1") String currentUser) {
        // 漏洞代码：没有验证是否有权修改
        User user = USERS.values().stream()
            .filter(u -> u.id == id)
            .findFirst()
            .orElse(null);

        if (user == null) {
            return "用户不存在: ID=" + id;
        }

        // 修改用户信息
        if (email != null && !email.isEmpty()) {
            user.email = email;
        }

        return String.format(
            "=== 用户资料已更新 ===\n" +
            "用户ID: %d\n" +
            "新邮箱: %s\n" +
            "\n[!] IDOR 漏洞: 用户 %s 成功修改了用户 %d 的资料",
            user.id, user.email, currentUser, id
        );
    }

    /**
     * IDOR 漏洞 - 删除订单
     *
     * 漏洞代码：未验证订单是否属于当前用户
     *
     * 攻击请求: POST /idor/order/delete?id=3&userId=1
     *
     * @param id 订单 ID
     * @param userId 当前用户 ID
     * @return 操作结果
     */
    @PostMapping("/order/delete")
    public String deleteOrder(@RequestParam("id") int id,
                               @RequestParam(value = "userId", defaultValue = "1") int userId) {
        // 漏洞代码：没有验证订单所有权
        Order order = ORDERS.get(id);

        if (order == null) {
            return "订单不存在: ID=" + id;
        }

        ORDERS.remove(id);

        return String.format(
            "=== 订单已删除 ===\n" +
            "订单ID: %d\n" +
            "商品: %s\n" +
            "原拥有者: 用户 %d\n" +
            "\n[!] IDOR 漏洞: 用户 %d 成功删除了用户 %d 的订单",
            order.id, order.product, order.userId, userId, order.userId
        );
    }

    /**
     * IDOR 垂直越权 - 访问管理员配置
     *
     * 漏洞代码：没有验证用户角色
     *
     * 攻击请求: GET /idor/admin/config?currentUser=user1
     *
     * @param currentUser 当前用户
     * @return 管理员配置
     */
    @GetMapping("/admin/config")
    public String getAdminConfig(@RequestParam(value = "currentUser", defaultValue = "user1") String currentUser) {
        // 漏洞代码：没有验证用户是否为管理员

        // 返回敏感的管理员配置
        StringBuilder sb = new StringBuilder();
        sb.append("=== 管理员配置 ===\n");
        sb.append("当前用户: ").append(currentUser).append("\n");
        sb.append("\n配置项:\n");

        for (Map.Entry<String, String> entry : ADMIN_CONFIG.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        sb.append("\n[!] 垂直越权: 普通用户 ").append(currentUser).append(" 访问了管理员配置");

        return sb.toString();
    }

    /**
     * IDOR 垂直越权 - 修改管理员配置
     *
     * 漏洞代码：没有验证用户角色
     *
     * 攻击请求: POST /idor/admin/config?key=secret_key&value=hacker_key
     *
     * @param key 配置键
     * @param value 配置值
     * @param currentUser 当前用户
     * @return 操作结果
     */
    @PostMapping("/admin/config")
    public String updateAdminConfig(@RequestParam("key") String key,
                                     @RequestParam("value") String value,
                                     @RequestParam(value = "currentUser", defaultValue = "user1") String currentUser) {
        // 漏洞代码：没有验证管理员权限

        ADMIN_CONFIG.put(key, value);

        return String.format(
            "=== 管理员配置已更新 ===\n" +
            "操作者: %s\n" +
            "配置项: %s\n" +
            "新值: %s\n" +
            "\n[!] 垂直越权: 普通用户 %s 成功修改了管理员配置",
            currentUser, key, value, currentUser
        );
    }

    /**
     * IDOR 垂直越权 - 提升用户权限
     *
     * 漏洞代码：没有验证操作权限
     *
     * 攻击请求: POST /idor/admin/promote?id=1&role=admin
     *
     * @param id 目标用户 ID
     * @param role 新角色
     * @param currentUser 当前用户
     * @return 操作结果
     */
    @PostMapping("/admin/promote")
    public String promoteUser(@RequestParam("id") int id,
                               @RequestParam("role") String role,
                               @RequestParam(value = "currentUser", defaultValue = "user1") String currentUser) {
        // 漏洞代码：没有验证管理员权限

        User user = USERS.values().stream()
            .filter(u -> u.id == id)
            .findFirst()
            .orElse(null);

        if (user == null) {
            return "用户不存在: ID=" + id;
        }

        String oldRole = user.role;
        user.role = role;

        return String.format(
            "=== 用户权限已提升 ===\n" +
            "操作者: %s\n" +
            "目标用户: %s (ID: %d)\n" +
            "原角色: %s\n" +
            "新角色: %s\n" +
            "\n[!] 垂直越权: 普通用户 %s 成功提升了用户权限",
            currentUser, user.username, user.id, oldRole, role, currentUser
        );
    }

    /**
     * IDOR 垂直越权 - 查看所有用户列表
     *
     * 漏洞代码：没有验证管理员权限
     *
     * @param currentUser 当前用户
     * @return 所有用户列表
     */
    @GetMapping("/admin/users")
    public String getAllUsers(@RequestParam(value = "currentUser", defaultValue = "user1") String currentUser) {
        // 漏洞代码：没有验证管理员权限

        StringBuilder sb = new StringBuilder();
        sb.append("=== 所有用户列表 ===\n");
        sb.append("操作者: ").append(currentUser).append("\n");
        sb.append("\n用户总数: ").append(USERS.size()).append("\n\n");

        for (User user : USERS.values()) {
            sb.append(String.format("ID: %d, 用户名: %s, 邮箱: %s, 角色: %s\n",
                user.id, user.username, user.email, user.role));
        }

        sb.append("\n[!] 垂直越权: 普通用户 ").append(currentUser).append(" 访问了用户列表");

        return sb.toString();
    }

    /**
     * 安全版本 - 查看用户资料（带权限验证）
     *
     * @param id 目标用户 ID
     * @param currentUser 当前用户
     * @return 用户信息
     */
    @GetMapping("/safe/user/profile")
    public String safeGetUserProfile(@RequestParam("id") int id,
                                      @RequestParam(value = "currentUser", defaultValue = "user1") String currentUser) {
        // 安全代码：验证用户是否有权查看
        User requestingUser = USERS.get(currentUser);
        if (requestingUser == null) {
            return "错误: 当前用户不存在";
        }

        // 检查是否为管理员
        boolean isAdmin = "admin".equals(requestingUser.role);

        // 检查是否查看自己的资料
        boolean isSelf = requestingUser.id == id;

        if (!isAdmin && !isSelf) {
            return "访问拒绝: 您无权查看该用户的资料";
        }

        User targetUser = USERS.values().stream()
            .filter(u -> u.id == id)
            .findFirst()
            .orElse(null);

        if (targetUser == null) {
            return "用户不存在: ID=" + id;
        }

        // 管理员可以看到完整信息，普通用户只能看到基本信息
        if (isAdmin) {
            return String.format(
                "=== 用户资料 (管理员视图) ===\n" +
                "用户ID: %d\n" +
                "用户名: %s\n" +
                "邮箱: %s\n" +
                "真实姓名: %s\n" +
                "角色: %s",
                targetUser.id, targetUser.username, targetUser.email, targetUser.realName, targetUser.role
            );
        } else {
            return String.format(
                "=== 用户资料 ===\n" +
                "用户名: %s\n" +
                "真实姓名: %s",
                targetUser.username, targetUser.realName
            );
        }
    }

    /**
     * 安全版本 - 访问管理员配置（带权限验证）
     *
     * @param currentUser 当前用户
     * @return 管理员配置
     */
    @GetMapping("/safe/admin/config")
    public String safeGetAdminConfig(@RequestParam(value = "currentUser", defaultValue = "user1") String currentUser) {
        // 安全代码：验证管理员权限
        User user = USERS.get(currentUser);

        if (user == null) {
            return "错误: 用户不存在";
        }

        if (!"admin".equals(user.role)) {
            return "访问拒绝: 需要管理员权限";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== 管理员配置 ===\n");
        for (Map.Entry<String, String> entry : ADMIN_CONFIG.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        return sb.toString();
    }

    /**
     * 信息端点
     *
     * @return 测试信息
     */
    @GetMapping("/info")
    public String info() {
        return String.format(
            "IDOR (Insecure Direct Object Reference) 漏洞演示%n" +
            "=================================================%n" +
            "Java Version: %s%n" +
            "OS: %s %s%n" +
            "%n" +
            "水平越权端点:%n" +
            "- GET /idor/user/profile?id=1&currentUser=user1%n" +
            "- GET /idor/user/profile/byname?username=user2&currentUser=user1%n" +
            "- GET /idor/order?id=1&userId=1%n" +
            "- POST /idor/user/update?id=2&email=hacker@evil.com%n" +
            "- POST /idor/order/delete?id=3&userId=1%n" +
            "%n" +
            "垂直越权端点:%n" +
            "- GET /idor/admin/config?currentUser=user1%n" +
            "- POST /idor/admin/config?key=secret_key&value=hacker_key%n" +
            "- POST /idor/admin/promote?id=1&role=admin%n" +
            "- GET /idor/admin/users?currentUser=user1%n" +
            "%n" +
            "安全端点:%n" +
            "- GET /idor/safe/user/profile?id=1&currentUser=user1%n" +
            "- GET /idor/safe/admin/config?currentUser=user1",
            System.getProperty("java.version"),
            System.getProperty("os.name"),
            System.getProperty("os.version")
        );
    }

    // ========== 数据模型 ==========

    static class User {
        int id;
        String username;
        String email;
        String realName;
        String role;

        User(int id, String username, String email, String realName, String role) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.realName = realName;
            this.role = role;
        }
    }

    static class Order {
        int id;
        int userId;
        String product;
        double price;
        String status;

        Order(int id, int userId, String product, double price, String status) {
            this.id = id;
            this.userId = userId;
            this.product = product;
            this.price = price;
            this.status = status;
        }
    }
}
