package com.webapp.core.controller.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * IDOR Service - Insecure Direct Object Reference
 */
@Component
public class IdorService {

    private static final Logger logger = LoggerFactory.getLogger(IdorService.class);

    private static final Map<String, Map<String, String>> users = new HashMap<>();
    private static final Map<String, Map<String, String>> orders = new HashMap<>();

    static {
        // Initialize with some test data
        Map<String, String> user1 = new HashMap<>();
        user1.put("id", "1");
        user1.put("username", "user1");
        user1.put("email", "user1@example.com");
        user1.put("role", "user");
        users.put("1", user1);

        Map<String, String> user2 = new HashMap<>();
        user2.put("id", "2");
        user2.put("username", "user2");
        user2.put("email", "user2@example.com");
        user2.put("role", "user");
        users.put("2", user2);

        Map<String, String> admin = new HashMap<>();
        admin.put("id", "100");
        admin.put("username", "admin");
        admin.put("email", "admin@example.com");
        admin.put("role", "admin");
        admin.put("ssn", "123-45-6789");
        users.put("100", admin);

        Map<String, String> order1 = new HashMap<>();
        order1.put("orderId", "ORD-001");
        order1.put("userId", "1");
        order1.put("amount", "100.00");
        order1.put("items", "item1,item2");
        orders.put("ORD-001", order1);

        Map<String, String> order2 = new HashMap<>();
        order2.put("orderId", "ORD-002");
        order2.put("userId", "2");
        order2.put("amount", "250.00");
        order2.put("items", "item3");
        orders.put("ORD-002", order2);
    }

    public String getUserById(String id) {
        // No authentication check - vulnerable to IDOR
        Map<String, String> user = users.get(id);
        if (user != null) {
            return "User Profile:\n" +
                   "ID: " + user.get("id") + "\n" +
                   "Username: " + user.get("username") + "\n" +
                   "Email: " + user.get("email") + "\n" +
                   "Role: " + user.get("role") + "\n" +
                   (user.containsKey("ssn") ? "SSN: " + user.get("ssn") + "\n" : "");
        }
        return "User not found";
    }

    public String getOrderById(String orderId) {
        // No ownership check - vulnerable to IDOR
        Map<String, String> order = orders.get(orderId);
        if (order != null) {
            return "Order Details:\n" +
                   "Order ID: " + order.get("orderId") + "\n" +
                   "User ID: " + order.get("userId") + "\n" +
                   "Amount: $" + order.get("amount") + "\n" +
                   "Items: " + order.get("items");
        }
        return "Order not found";
    }

    public String deleteOrderById(String orderId) {
        // No authorization check - vulnerable to IDOR
        Map<String, String> order = orders.get(orderId);
        if (order != null) {
            orders.remove(orderId);
            return "Order deleted: " + orderId;
        }
        return "Order not found";
    }

    public String updateUserEmail(String userId, String email) {
        // No authentication check - vulnerable to IDOR
        Map<String, String> user = users.get(userId);
        if (user != null) {
            String oldEmail = user.get("email");
            user.put("email", email);
            return "Email updated for user " + userId + "\nOld: " + oldEmail + "\nNew: " + email;
        }
        return "User not found";
    }
}
