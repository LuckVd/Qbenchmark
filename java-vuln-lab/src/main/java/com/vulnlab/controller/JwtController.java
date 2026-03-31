package com.vulnlab.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT (JSON Web Token) 漏洞演示
 *
 * 漏洞说明：
 * 1. 使用弱密钥或硬编码密钥签名 JWT
 * 2. 允许算法降级攻击（none algorithm）
 * 3. 不验证 token 签名或过期时间
 *
 * 修复方案：
 * 1. 使用强随机密钥
 * 2. 固定签名算法，拒绝算法降级
 * 3. 验证 token 签名和过期时间
 *
 * @author VulnLab
 */
@RestController
@RequestMapping("/jwt")
public class JwtController {

    private static final Logger logger = LoggerFactory.getLogger(JwtController.class);

    // 漏洞代码：硬编码的弱密钥
    private static final String WEAK_SECRET = "mySecretKey";
    private static final String ADMIN_KEY = "admin123";

    /**
     * JWT 漏洞 - 生成弱签名 JWT
     *
     * 漏洞原理：使用弱密钥或硬编码密钥生成 JWT
     * 攻击向量：攻击者可以伪造任意用户身份的 JWT
     *
     * 测试步骤：
     * 1. 生成普通用户 token
     * 2. 使用弱密钥伪造 admin token
     * 3. 用伪造的 token 访问管理员资源
     *
     * 命令示例:
     * curl "http://localhost:8080/jwt/generate?username=user"
     * curl "http://localhost:8080/jwt/generate?username=admin&key=admin123"
     *
     * @param username 用户名
     * @param key 密钥（可选，默认使用弱密钥）
     * @return JWT token
     */
    @GetMapping("/generate")
    public Map<String, Object> generate(@RequestParam("username") String username,
                                        @RequestParam(value = "key", defaultValue = WEAK_SECRET) String key) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 漏洞代码：使用用户提供的或弱硬编码密钥
            SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
            byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(
                java.util.Base64.getEncoder().encodeToString(key.getBytes())
            );

            Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

            long nowMillis = System.currentTimeMillis();
            Date now = new Date(nowMillis);

            // 创建 JWT
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
            result.put("info", "Weak key used - token can be forged!");

            logger.info("Generated JWT for user: {} with key: {}", username, key);
        } catch (Exception e) {
            logger.error("JWT generation error", e);
            result.put("error", "Generation failed: " + e.getMessage());
        }

        return result;
    }

    /**
     * JWT 漏洞 - 验证 JWT（可伪造）
     *
     * 漏洞原理：使用已知弱密钥验证，或验证逻辑有缺陷
     * 攻击向量：使用伪造的 JWT 获取管理员权限
     *
     * 伪造示例：
     * 1. 使用 jwt_tool 或在线工具伪造 token
     * 2. 修改 subject 为 admin
     * 3. 使用已知密钥重新签名
     *
     * 命令示例:
     * curl "http://localhost:8080/jwt/verify?token=YOUR_TOKEN"
     *
     * @param token JWT token
     * @return 验证结果
     */
    @GetMapping("/verify")
    public Map<String, Object> verify(@RequestParam("token") String token) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 漏洞代码：尝试多个弱密钥验证
            String[] weakKeys = {WEAK_SECRET, ADMIN_KEY, "secret", "password", "123456"};

            for (String key : weakKeys) {
                try {
                    SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
                    byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(
                        java.util.Base64.getEncoder().encodeToString(key.getBytes())
                    );

                    Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

                    // 验证 JWT
                    io.jsonwebtoken.Claims claims = Jwts.parser()
                        .setSigningKey(signingKey)
                        .parseClaimsJws(token)
                        .getBody();

                    result.put("valid", true);
                    result.put("username", claims.getSubject());
                    result.put("role", claims.get("role"));
                    result.put("issuedAt", claims.getIssuedAt());
                    result.put("keyUsed", key);
                    result.put("warning", "Token verified with weak key!");

                    logger.info("JWT verified for user: {} using key: {}", claims.getSubject(), key);
                    return result;
                } catch (Exception e) {
                    // 尝试下一个密钥
                }
            }

            result.put("valid", false);
            result.put("error", "Invalid token (tried multiple weak keys)");
        } catch (Exception e) {
            logger.error("JWT verification error", e);
            result.put("error", "Verification failed: " + e.getMessage());
        }

        return result;
    }

    /**
     * JWT 漏洞 - 算法降级攻击
     *
     * 漏洞原理：接受 "none" 算法，不验证签名
     * 攻击向量：将算法改为 none，伪造任意内容
     *
     * 测试步骤：
     * 1. 获取有效 token
     * 2. 将头部 alg 改为 none
     * 3. 修改 payload 中的 role 为 admin
     * 4. 移除签名部分
     *
     * 命令示例:
     * curl "http://localhost:8080/jwt/none?token=eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJhZG1pbmlzdHJhdG9yIn0."
     *
     * @param token JWT token（可能使用 none 算法）
     * @return 验证结果
     */
    @GetMapping("/none")
    public Map<String, Object> noneAlgorithm(@RequestParam("token") String token) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 漏洞代码：允许 none 算法（实际场景中库可能已修复，但演示漏洞原理）
            String[] parts = token.split("\\.");

            if (parts.length >= 2) {
                // 解码 payload
                String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                result.put("valid", true);
                result.put("payload", payload);
                result.put("warning", "None algorithm accepted - signature not verified!");
                result.put("info", "In real vulnerable implementations, this would accept any forged token");
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

    /**
     * JWT 漏洞 - 密钥泄露
     *
     * 漏洞原理：在错误信息或响应中泄露签名密钥
     *
     * 命令示例:
     * curl "http://localhost:8080/jwt/leak"
     *
     * @return 密钥信息
     */
    @GetMapping("/leak")
    public Map<String, Object> leakKey() {
        Map<String, Object> result = new HashMap<>();

        // 漏洞代码：泄露签名密钥
        result.put("secret", WEAK_SECRET);
        result.put("adminKey", ADMIN_KEY);
        result.put("warning", "Secret keys leaked - use these to forge tokens!");

        logger.warn("Secret keys leaked via endpoint");
        return result;
    }
}
