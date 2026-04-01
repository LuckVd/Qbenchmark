package com.vulnlab.controller;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/misc")
public class MiscFeatureController {

    private static final Map<String, User> USERS = new ConcurrentHashMap<>();
    private static final Map<String, ResetToken> RESET_TOKENS = new ConcurrentHashMap<>();

    static {
        USERS.put("user1", new User("user1", "user1@example.com", "password123"));
        USERS.put("admin", new User("admin", "admin@example.com", "admin123"));
    }

    @GetMapping("/csv/export")
    public void csvExport(@RequestParam("username") String username,
                          @RequestParam("email") String email,
                          @RequestParam(value = "amount", defaultValue = "100") String amount,
                          HttpServletResponse response) throws Exception {

        String csv = String.format("username,email,amount\n%s,%s,%s", username, email, amount);

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=users.csv");

        PrintWriter writer = response.getWriter();
        writer.write(csv);
        writer.flush();
    }

    @GetMapping("/csv/orders")
    public void csvOrders(@RequestParam("orderId") String orderId,
                           HttpServletResponse response) throws Exception {

        String csv = String.format("orderId,product,amount\n%s,Product A,100", orderId);

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=orders.csv");

        PrintWriter writer = response.getWriter();
        writer.write(csv);
        writer.flush();
    }

    @PostMapping("/reset/request")
    public String resetRequest(@RequestParam("username") String username,
                                @RequestHeader(value = "Host", defaultValue = "localhost:8080") String host) {

        User user = USERS.get(username);
        if (user == null) {
            return "User not found";
        }

        String token = generateToken();
        RESET_TOKENS.put(token, new ResetToken(username, System.currentTimeMillis() + 3600000));

        String resetLink = String.format("http://%s/reset/confirm?token=%s", host, token);

        return String.format(
            "Reset email sent\nUser: %s\nEmail: %s\nReset link: %s",
            username, user.email, resetLink
        );
    }

    @GetMapping("/reset/token")
    public String getResetToken(@RequestParam("username") String username) {
        String token = Base64.getEncoder().encodeToString((username + ":" + System.currentTimeMillis()).getBytes());

        return String.format(
            "Reset token\nUser: %s\nToken: %s",
            username, token
        );
    }

    @PostMapping("/reset/confirm")
    public String resetConfirm(@RequestParam("token") String token,
                                @RequestParam("newPassword") String newPassword) {

        ResetToken resetToken = RESET_TOKENS.get(token);
        if (resetToken == null) {
            return "Invalid token";
        }

        User user = USERS.get(resetToken.username);
        if (user == null) {
            return "User not found";
        }

        user.password = newPassword;
        RESET_TOKENS.remove(token);

        return "Password reset successful for user: " + resetToken.username;
    }

    @PostMapping("/auth/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password) {

        User user = USERS.get(username);
        if (user != null && user.password.equals(password)) {
            return "Login successful for: " + username;
        }

        return "Login failed";
    }

    @PostMapping("/auth/bypass")
    public String loginBypass(@RequestParam("username") String username,
                               @RequestParam("password") String password) {

        User user = USERS.get(username);

        // Direct SQL construction
        String sql = "SELECT * FROM users WHERE username='" + username + "' AND password='" + password + "'";

        if (user != null) {
            return String.format("Login bypass\nSQL: %s\nResult: Success", sql);
        }

        return "Login bypass failed";
    }

    @GetMapping("/admin/config")
    public String adminConfig() {
        return String.format(
            "Admin Config\nSite: Qbenchmark\nEmail: admin@qbench.com\nKey: sk_live_xxxxxxxxxxxx"
        );
    }

    @GetMapping("/bypass/extension")
    public String bypassExtension(@RequestParam("file") String file) {
        String[] allowedExtensions = {".jpg", ".png", ".gif"};
        boolean allowed = false;

        for (String ext : allowedExtensions) {
            if (file.endsWith(ext)) {
                allowed = true;
                break;
            }
        }

        if (allowed) {
            return "File extension allowed: " + file;
        } else {
            return "File extension not allowed";
        }
    }

    @GetMapping("/bypass/mime")
    public String bypassMime(@RequestParam("file") String file,
                              @RequestParam("type") String mimeType) {
        return String.format(
            "File upload\nFile: %s\nMIME type: %s\n[Note: MIME type can be spoofed]",
            file, mimeType
        );
    }

    static class User {
        String username;
        String email;
        String password;

        public User(String username, String email, String password) {
            this.username = username;
            this.email = email;
            this.password = password;
        }
    }

    static class ResetToken {
        String username;
        long expiryTime;

        public ResetToken(String username, long expiryTime) {
            this.username = username;
            this.expiryTime = expiryTime;
        }
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }
}
