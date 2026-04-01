package com.vulnlab.controller;

import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/resource")
public class ResourceAccessController {

    private static final Map<String, User> USERS = new ConcurrentHashMap<>();
    private static final Map<Integer, Order> ORDERS = new ConcurrentHashMap<>();
    private static final Map<String, String> ADMIN_CONFIG = new ConcurrentHashMap<>();

    static {
        USERS.put("user1", new User(1, "user1", "user1@example.com", "User 1", "user"));
        USERS.put("user2", new User(2, "user2", "user2@example.com", "User 2", "user"));
        USERS.put("admin", new User(100, "admin", "admin@example.com", "Administrator", "admin"));

        ORDERS.put(1, new Order(1, 1, "iPhone 15 Pro", 9999.00, "paid"));
        ORDERS.put(2, new Order(2, 1, "MacBook Pro", 19999.00, "shipped"));
        ORDERS.put(3, new Order(3, 2, "iPad Air", 4999.00, "pending"));

        ADMIN_CONFIG.put("site_name", "Qbenchmark");
        ADMIN_CONFIG.put("admin_email", "admin@qbench.com");
        ADMIN_CONFIG.put("secret_key", "sk_live_xxxxxxxxxxxx");
    }

    @GetMapping("/user/profile")
    public String getUserProfile(@RequestParam("id") int id,
                                 @RequestParam(value = "currentUser", defaultValue = "user1") String currentUser) {
        User user = USERS.values().stream()
            .filter(u -> u.id == id)
            .findFirst()
            .orElse(null);

        if (user == null) {
            return "User not found: ID=" + id;
        }

        return String.format(
            "User ID: %d\nUsername: %s\nEmail: %s\nName: %s\nRole: %s\n[Note: User %s is viewing user %d]",
            user.id, user.username, user.email, user.realName, user.role, currentUser, id
        );
    }

    @GetMapping("/user/profile/byname")
    public String getUserProfileByName(@RequestParam("username") String username,
                                       @RequestParam(value = "currentUser", defaultValue = "user1") String currentUser) {
        User user = USERS.get(username);

        if (user == null) {
            return "User not found: " + username;
        }

        return String.format(
            "User ID: %d\nUsername: %s\nEmail: %s\nName: %s\nRole: %s\n[Note: User %s is viewing user %s]",
            user.id, user.username, user.email, user.realName, user.role, currentUser, username
        );
    }

    @GetMapping("/order")
    public String getOrder(@RequestParam("id") int id,
                           @RequestParam(value = "currentUser", defaultValue = "user1") String currentUser) {
        Order order = ORDERS.get(id);

        if (order == null) {
            return "Order not found: ID=" + id;
        }

        return String.format(
            "Order ID: %d\nUser ID: %d\nProduct: %s\nAmount: %.2f\nStatus: %s\n[Note: User %s is viewing order %d]",
            order.id, order.userId, order.product, order.amount, order.status, currentUser, id
        );
    }

    @PostMapping("/user/update")
    public String updateUser(@RequestParam("id") int id,
                             @RequestParam(value = "currentUser", defaultValue = "user1") String currentUser) {
        User user = USERS.values().stream()
            .filter(u -> u.id == id)
            .findFirst()
            .orElse(null);

        if (user == null) {
            return "User not found: ID=" + id;
        }

        return String.format(
            "User %d updated by %s\n[Warning: No authorization check performed]",
            id, currentUser
        );
    }

    @PostMapping("/order/delete")
    public String deleteOrder(@RequestParam("id") int id,
                              @RequestParam(value = "currentUser", defaultValue = "user1") String currentUser) {
        Order order = ORDERS.get(id);

        if (order == null) {
            return "Order not found: ID=" + id;
        }

        return String.format(
            "Order %d deleted by %s\n[Warning: No authorization check performed]",
            id, currentUser
        );
    }

    @GetMapping("/admin/config")
    public String getAdminConfig(@RequestParam(value = "currentUser", defaultValue = "user1") String currentUser) {
        return String.format(
            "Site Name: %s\nAdmin Email: %s\nSecret Key: %s\n[Warning: User %s accessed admin config]",
            ADMIN_CONFIG.get("site_name"),
            ADMIN_CONFIG.get("admin_email"),
            ADMIN_CONFIG.get("secret_key"),
            currentUser
        );
    }

    static class User {
        int id;
        String username;
        String email;
        String realName;
        String role;

        public User(int id, String username, String email, String realName, String role) {
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
        double amount;
        String status;

        public Order(int id, int userId, String product, double amount, String status) {
            this.id = id;
            this.userId = userId;
            this.product = product;
            this.amount = amount;
            this.status = status;
        }
    }
}
