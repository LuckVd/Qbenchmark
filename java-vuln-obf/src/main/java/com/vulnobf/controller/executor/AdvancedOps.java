package com.vulnobf.controller.executor;

import com.vulnobf.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Advanced Operations - High/Medium/Low risk vulnerabilities
 */
@Component
public class AdvancedOps {

    private static final Logger logger = LoggerFactory.getLogger(AdvancedOps.class);

    /**
     * JNDI RMI injection vulnerability
     */
    public String jndiRmi(String url) {
        try {
            // Vulnerable JNDI lookup
            Context ctx = new InitialContext();
            Object obj = ctx.lookup(url);

            return "JNDI lookup completed: " + obj.getClass().getName();

        } catch (Exception e) {
            logger.error("JNDI RMI error", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * JNDI LDAP injection vulnerability
     */
    public String jndiLdap(String url) {
        try {
            Context ctx = new InitialContext();
            Object obj = ctx.lookup(url);

            return "LDAP lookup completed: " + obj.getClass().getName();

        } catch (Exception e) {
            logger.error("JNDI LDAP error", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * HTTP Smuggling - CL.TE
     */
    public String smugglingClTe(String data) {
        // Return raw data - smuggling happens at protocol level
        // This endpoint is vulnerable to smuggling
        return "CL.TE data received: " + data;
    }

    /**
     * HTTP Smuggling - TE.CL
     */
    public String smugglingTeCl(String data) {
        return "TE.CL data received: " + data;
    }

    /**
     * IDOR - Horizontal privilege escalation
     */
    public String getUserProfile(String userId, String requesterId) {
        // No check if userId == requesterId
        Map<String, String> users = new HashMap<>();
        users.put("user1", "User One Data");
        users.put("user2", "User Two Data");
        users.put("admin", "Admin Data");

        return users.getOrDefault(userId, "User not found");
    }

    /**
     * IDOR - Vertical privilege escalation
     */
    public String getAdminConfig(String userId) {
        // No admin check
        if ("admin".equals(userId)) {
            return "Admin Config: secret_key=12345, db_password=admin123";
        }
        return "Access denied";
    }

    /**
     * ReDoS - Regular expression DoS
     */
    public String regexSearch(String pattern, String text) {
        try {
            // Vulnerable regex that can cause catastrophic backtracking
            Pattern.compile(pattern).matcher(text).find();
            return "Regex search completed";

        } catch (Exception e) {
            logger.error("Regex error", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Memory DoS
     */
    public String loadLargeData(int size) {
        try {
            // Allocate large array based on user input
            byte[] data = new byte[size * 1024 * 1024];
            return "Allocated " + data.length + " bytes";

        } catch (OutOfMemoryError e) {
            return "Out of memory";
        }
    }

    /**
     * Payment logic flaw
     */
    public String processPayment(String amount, String discount) {
        // Logic flaw: discount applied after validation
        try {
            double amt = Double.parseDouble(amount);
            double disc = Double.parseDouble(discount);

            // Vulnerable: discount can exceed amount
            double finalAmount = amt - disc;

            if (finalAmount <= 0) {
                return "Payment completed! Amount: $" + amt + ", Discount: $" + disc;
            }

            return "Payment pending: $" + finalAmount;

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Captcha bypass vulnerability
     */
    public String verifyCaptcha(String captcha, String sessionId) {
        // Captcha is predictable and reusable
        String expectedCaptcha = "1234"; // Static for demo

        if (expectedCaptcha.equals(captcha)) {
            return "Captcha verified for session: " + sessionId;
        }

        return "Invalid captcha";
    }

    /**
     * Login bypass via SQL injection
     */
    public String sqlBypassLogin(String username, String password) {
        // Direct SQL construction
        String sql = "SELECT * FROM users WHERE username='" + username + "' AND password='" + password + "'";

        // Check for bypass patterns
        if (username.contains("' OR '1'='1")) {
            return "Login bypassed!";
        }

        return "Login failed";
    }

    /**
     * Unauthorized access
     */
    public String getAdminDashboard(String userId) {
        // No authentication check
        return "Admin Dashboard - User: " + userId + "<br/>Secret Data: CONFIDENTIAL";
    }

    /**
     * Password reset vulnerability
     */
    public String resetPassword(String email, String host) {
        // Use host header for email link - vulnerable to Host injection
        String resetLink = "http://" + host + "/reset?token=abc123";

        return "Password reset link sent to " + email + ": " + resetLink;
    }

    /**
     * CSV injection
     */
    public String exportCsv(String[] data) {
        StringBuilder csv = new StringBuilder();
        csv.append("Name,Email,Phone\n");

        for (String item : data) {
            // No sanitization - allows formula injection
            csv.append(item).append("\n");
        }

        return csv.toString();
    }

    /**
     * File extension bypass
     */
    public String checkExtension(String filename) {
        // Weak blacklist check
        String[] blocked = {".exe", ".bat", ".sh"};

        for (String ext : blocked) {
            if (filename.endsWith(ext)) {
                return "File type not allowed";
            }
        }

        return "File allowed: " + filename;
    }

    /**
     * MIME type bypass
     */
    public String checkMime(String filename, String contentType) {
        // Trust user-provided content type
        if (contentType != null && contentType.startsWith("image/")) {
            return "File allowed: " + filename;
        }

        return "File type not allowed";
    }
}
