package com.vulnlab.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/token")
public class TokenAuthController {

    private static final Logger logger = LoggerFactory.getLogger(TokenAuthController.class);

    private static final String WEAK_SECRET = "mySecretKey";
    private static final String ADMIN_KEY = "admin123";

    @GetMapping("/generate")
    public Map<String, Object> generate(@RequestParam("username") String username,
                                        @RequestParam(value = "key", defaultValue = WEAK_SECRET) String key) {
        Map<String, Object> result = new HashMap<>();

        try {
            SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
            byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(
                java.util.Base64.getEncoder().encodeToString(key.getBytes())
            );

            Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

            long nowMillis = System.currentTimeMillis();
            Date now = new Date(nowMillis);

            String jwt = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .claim("role", username.equals("admin") ? "administrator" : "user")
                .signWith(signatureAlgorithm, signingKey)
                .compact();

            result.put("token", jwt);
            result.put("username", username);
            result.put("key", key);
            result.put("algorithm", signatureAlgorithm);

            logger.info("Generated token for user: {} with key: {}", username, key);
        } catch (Exception e) {
            logger.error("Token generation error", e);
            result.put("error", "Generation failed: " + e.getMessage());
        }

        return result;
    }

    @GetMapping("/verify")
    public Map<String, Object> verify(@RequestParam("token") String token) {
        Map<String, Object> result = new HashMap<>();

        try {
            String[] weakKeys = {WEAK_SECRET, ADMIN_KEY, "secret", "password", "123456"};

            for (String key : weakKeys) {
                try {
                    SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
                    byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(
                        java.util.Base64.getEncoder().encodeToString(key.getBytes())
                    );

                    Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

                    io.jsonwebtoken.Claims claims = Jwts.parser()
                        .setSigningKey(signingKey)
                        .parseClaimsJws(token)
                        .getBody();

                    result.put("valid", true);
                    result.put("username", claims.getSubject());
                    result.put("role", claims.get("role"));
                    result.put("issuedAt", claims.getIssuedAt());
                    result.put("keyUsed", key);

                    logger.info("Token verified for user: {} using key: {}", claims.getSubject(), key);
                    return result;
                } catch (Exception e) {
                }
            }

            result.put("valid", false);
            result.put("error", "Invalid token");
        } catch (Exception e) {
            logger.error("Token verification error", e);
            result.put("error", "Verification failed: " + e.getMessage());
        }

        return result;
    }

    @GetMapping("/none")
    public Map<String, Object> noneAlgorithm(@RequestParam("token") String token) {
        Map<String, Object> result = new HashMap<>();

        try {
            String[] parts = token.split("\\.");

            if (parts.length >= 2) {
                String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                result.put("valid", true);
                result.put("payload", payload);
            } else {
                result.put("valid", false);
                result.put("error", "Invalid token format");
            }

            logger.info("None algorithm token received");
        } catch (Exception e) {
            logger.error("None algorithm error", e);
            result.put("error", "Processing failed: " + e.getMessage());
        }

        return result;
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> result = new HashMap<>();

        result.put("secret", WEAK_SECRET);
        result.put("adminKey", ADMIN_KEY);

        logger.warn("Secret keys leaked via endpoint");
        return result;
    }
}
