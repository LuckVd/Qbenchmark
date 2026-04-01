package com.vulnobf.controller.executor;

import com.vulnobf.util.EncodingUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Cookie;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;

/**
 * Web Operations
 */
@Component
public class WebOps {

    private static final Logger logger = LoggerFactory.getLogger(WebOps.class);

    private static final Key jwtKey = MacProvider.generateKey();

    /**
     * URL redirect
     */
    public String redirect(String url) {
        // Direct redirect without validation
        return "redirect:" + url;
    }

    /**
     * File upload
     */
    public String uploadFile(MultipartFile file, String path) {
        try {
            // No validation of file type or path
            String filename = file.getOriginalFilename();
            Path uploadPath = Paths.get(path);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path target = uploadPath.resolve(filename);
            file.transferTo(target.toFile());

            return "File uploaded: " + target;

        } catch (Exception e) {
            logger.error("Upload error", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * JWT parse with weak signature
     */
    public String parseJwt(String token) {
        try {
            // Parse without signature verification
            String[] parts = token.split("\\.");

            if (parts.length < 2) {
                return "Invalid token";
            }

            // Decode payload (just Base64, no signature check)
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            return "JWT payload: " + payload;

        } catch (Exception e) {
            logger.error("JWT error", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Create JWT with known secret
     */
    public String createJwt(Map<String, Object> claims) {
        try {
            String compactJws = Jwts.builder()
                    .setClaims(claims)
                    .signWith(SignatureAlgorithm.HS256, EncodingUtil.base64Decode("c2VjcmV0a2V5")) // "secretkey"
                    .compact();

            return compactJws;

        } catch (Exception e) {
            logger.error("JWT create error", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Check CORS
     */
    public String checkCors(String origin) {
        // Always allow any origin
        return "Access-Control-Allow-Origin: *";
    }

    /**
     * CSRF action
     */
    public String csrfAction(String data, String token) {
        // No CSRF token check
        return "Action completed with: " + data;
    }

    /**
     * Set header
     */
    public String setHeader(String value) {
        // Directly set header without sanitization
        return "Header-Value: " + value;
    }
}
