package com.vulnlab.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/network")
public class NetworkInfoController {

    private static final Logger logger = LoggerFactory.getLogger(NetworkInfoController.class);

    @GetMapping("/client")
    public Map<String, Object> client(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        String xff = request.getHeader("X-Forwarded-For");
        String ip = (xff != null) ? xff : request.getRemoteAddr();

        logger.info("Client IP - X-Forwarded-For: {}, RemoteAddr: {}", xff, request.getRemoteAddr());

        result.put("detected_ip", ip);
        result.put("xff_header", xff);
        result.put("remote_addr", request.getRemoteAddr());

        if (Objects.equals(ip, "127.0.0.1") || Objects.equals(ip, "::1") || Objects.equals(ip, "localhost")) {
            result.put("access", "GRANTED");
            result.put("message", "Access granted from trusted address");
            logger.warn("Access granted for IP: {}", ip);
        } else {
            result.put("access", "DENIED");
            result.put("message", "Access denied for IP: " + ip);
        }

        return result;
    }

    @GetMapping("/client/sec")
    public Map<String, Object> clientSecure(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        String realIp = request.getRemoteAddr();
        String xff = request.getHeader("X-Forwarded-For");

        logger.info("Secure client check - RemoteAddr: {}, X-Forwarded-For: {}", realIp, xff);

        result.put("real_ip", realIp);
        result.put("xff_header", xff);
        result.put("xff_ignored", true);

        if (Objects.equals(realIp, "127.0.0.1") || Objects.equals(realIp, "::1")) {
            result.put("access", "GRANTED");
            result.put("message", "Access granted from trusted address");
        } else {
            result.put("access", "DENIED");
            result.put("message", "Access denied for IP: " + realIp);
        }

        return result;
    }

    @GetMapping("/headers")
    public Map<String, String> headers(HttpServletRequest request) {
        Map<String, String> result = new HashMap<>();

        result.put("RemoteAddr", request.getRemoteAddr());
        result.put("RemoteHost", request.getRemoteHost());
        result.put("X-Forwarded-For", request.getHeader("X-Forwarded-For"));
        result.put("X-Real-IP", request.getHeader("X-Real-IP"));
        result.put("Client-IP", request.getHeader("Client-IP"));

        return result;
    }

    @GetMapping("/info")
    public String info() {
        return "Network Info\n" +
               "Endpoints:\n" +
               "- GET /api/v1/network/client\n" +
               "- GET /api/v1/network/headers\n";
    }
}
