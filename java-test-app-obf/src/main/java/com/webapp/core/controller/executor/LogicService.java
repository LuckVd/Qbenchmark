package com.webapp.core.controller.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Logic Service - Business logic vulnerabilities
 */
@Component
public class LogicService {

    private static final Logger logger = LoggerFactory.getLogger(LogicService.class);
    private static int captchaAnswer = 5;

    public String processPayment(String amount) {
        try {
            double amt = Double.parseDouble(amount);
            // No lower bound check - vulnerable to negative amounts
            if (amt > 10000) {
                return "Payment exceeds limit";
            }
            return "Payment of $" + amount + " processed successfully";
        } catch (Exception e) {
            return "Invalid amount";
        }
    }

    public String processPaymentFloat(String amount) {
        try {
            float amt = Float.parseFloat(amount);
            // Float comparison vulnerability
            if (amt == 0.1 + 0.2) {  // This will be false due to floating point precision
                return "Special discount applied!";
            }
            return "Payment of $" + amount + " processed";
        } catch (Exception e) {
            return "Invalid amount";
        }
    }

    public String applyCoupon(String code) {
        // Coupon logic vulnerability - can be reused
        if ("SAVE10".equals(code) || "SAVE20".equals(code)) {
            return "Coupon applied: 10% discount";
        }
        // Case sensitive comparison can be bypassed
        if (code.toLowerCase().equals("admin_discount")) {
            return "Admin coupon applied: 50% discount";
        }
        return "Invalid coupon code";
    }

    public String generateCaptcha() {
        captchaAnswer = (int) (Math.random() * 10);
        return "Captcha: What is " + captchaAnswer + " + 3?";
    }

    public String verifyCaptcha(String answer) {
        try {
            // Captcha is predictable - just subtract 3
            if (Integer.parseInt(answer) == captchaAnswer + 3) {
                return "Captcha correct";
            }
            return "Captcha incorrect";
        } catch (Exception e) {
            return "Invalid answer";
        }
    }

    public String regexSearch(String pattern) {
        try {
            // Vulnerable to ReDoS
            Pattern p = Pattern.compile(pattern);
            String text = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB";
            long start = System.currentTimeMillis();
            p.matcher(text).matches();
            long end = System.currentTimeMillis();
            return "Search completed in " + (end - start) + "ms";
        } catch (Exception e) {
            return "Search error: " + e.getMessage();
        }
    }

    public String loadData(String size) {
        try {
            int s = Integer.parseInt(size);
            // No limit - vulnerable to DoS
            byte[] data = new byte[s * 1024 * 1024];
            return "Loaded " + data.length + " bytes";
        } catch (Exception e) {
            return "Load error: " + e.getMessage();
        }
    }
}
