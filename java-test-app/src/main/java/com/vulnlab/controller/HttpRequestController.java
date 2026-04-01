package com.vulnlab.controller;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;

@RestController
@RequestMapping("/api/v1/http")
public class HttpRequestController {

    @PostMapping("/clte")
    public String clte(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();

        sb.append("=== Request Headers ===\n");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            sb.append(name).append(": ").append(request.getHeader(name)).append("\n");
        }

        String contentLength = request.getHeader("Content-Length");
        String transferEncoding = request.getHeader("Transfer-Encoding");

        sb.append("\n=== Analysis ===\n");
        sb.append("Content-Length: ").append(contentLength != null ? contentLength : "not set").append("\n");
        sb.append("Transfer-Encoding: ").append(transferEncoding != null ? transferEncoding : "not set").append("\n");

        String body = getRequestBody(request);
        sb.append("\n=== Body ===\n");
        sb.append(body);

        if (body != null && body.contains("GET /")) {
            sb.append("\n[!] Suspicious pattern detected");
        }

        return sb.toString();
    }

    @PostMapping("/tecl")
    public String tecl(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();

        sb.append("=== Request Headers ===\n");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            sb.append(name).append(": ").append(request.getHeader(name)).append("\n");
        }

        String contentLength = request.getHeader("Content-Length");
        String transferEncoding = request.getHeader("Transfer-Encoding");

        sb.append("\n=== Analysis ===\n");
        sb.append("Content-Length: ").append(contentLength != null ? contentLength : "not set").append("\n");
        sb.append("Transfer-Encoding: ").append(transferEncoding != null ? transferEncoding : "not set").append("\n");

        String body = getRequestBody(request);
        sb.append("\n=== Body ===\n");
        sb.append(body);

        if (body != null && body.contains("GET /")) {
            sb.append("\n[!] Suspicious pattern detected");
        }

        return sb.toString();
    }

    @PostMapping("/clcl")
    public String clcl(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();

        sb.append("=== Request Headers ===\n");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if (name.equalsIgnoreCase("Content-Length")) {
                Enumeration<String> values = request.getHeaders(name);
                while (values.hasMoreElements()) {
                    sb.append(name).append(": ").append(values.nextElement()).append("\n");
                }
            } else {
                sb.append(name).append(": ").append(request.getHeader(name)).append("\n");
            }
        }

        String body = getRequestBody(request);
        sb.append("\n=== Body ===\n");
        sb.append(body);

        int clCount = 0;
        Enumeration<String> clValues = request.getHeaders("Content-Length");
        while (clValues.hasMoreElements()) {
            clValues.nextElement();
            clCount++;
        }

        if (clCount > 1) {
            sb.append("\n[!] Multiple Content-Length headers detected");
        }

        return sb.toString();
    }

    @PostMapping("/cache")
    public String cache(HttpServletRequest request) {
        String body = getRequestBody(request);

        return String.format(
            "Cache Request Processed\n" +
            "User-Agent: %s\n" +
            "X-Forwarded-Host: %s\n" +
            "Body: %s",
            request.getHeader("User-Agent"),
            request.getHeader("X-Forwarded-Host"),
            body
        );
    }

    @GetMapping("/admin")
    public String admin(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        String originalUri = request.getHeader("X-Original-URI");

        return String.format(
            "=== Admin Panel ===\n" +
            "X-Forwarded-For: %s\n" +
            "X-Original-URI: %s\n",
            forwardedFor != null ? forwardedFor : "not set",
            originalUri != null ? originalUri : "not set"
        );
    }

    @GetMapping("/info")
    public String info() {
        return "HTTP Request Processing\n" +
               "Endpoints:\n" +
               "- POST /api/v1/http/clte\n" +
               "- POST /api/v1/http/tecl\n" +
               "- POST /api/v1/http/cache\n" +
               "- GET /api/v1/http/admin\n";
    }

    private String getRequestBody(HttpServletRequest request) {
        try {
            BufferedReader reader = request.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            return "Error reading body: " + e.getMessage();
        }
    }
}
