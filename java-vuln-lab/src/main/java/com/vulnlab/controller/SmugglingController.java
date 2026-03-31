package com.vulnlab.controller;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;

/**
 * HTTP 请求走私漏洞演示
 *
 * 漏洞说明：
 * - HTTP Smuggling 是一种利用前端代理和后端服务器对 HTTP 请求解析不一致的攻击
 * - 攻击者可以走私恶意请求来绕过安全控制、缓存投毒或劫持会话
 * - CLTE/TECL: Content-Length 和 Transfer-Encoding 处理不一致
 *
 * 漏洞原理：
 * 1. 前端代理（如 Nginx、HAProxy）和后端应用对请求边界的处理方式不同
 * 2. CL.TE: 前端使用 CL，后端使用 TE
 * 3. TE.CL: 前端使用 TE，后端使用 CL
 * 4. 攻击者通过精心构造的请求走私额外的 HTTP 请求
 *
 * 常见攻击场景：
 * - 绕过 WAF 规则
 * - 缓存投毒
 * - 会话劫持
 * - 权限提升
 * - XSS 攻击
 *
 * 修复方案：
 * 1. 统一请求边界处理方式（优先使用 Content-Length）
 * 2. 禁用 Transfer-Encoding: chunked
 * 3. 验证请求格式
 * 4. 使用 HTTP/2（更严格的格式要求）
 * 5. 配置前端代理和后端服务器一致
 *
 * @author VulnLab
 */
@RestController
@RequestMapping("/smuggling")
public class SmugglingController {

    /**
     * CL.TE Smuggling - Content-Length / Transfer-Encoding 不匹配
     *
     * 漏洞场景：
     * - 前端代理优先使用 Content-Length
     * - 后端服务器优先使用 Transfer-Encoding
     *
     * 测试方法：
     * ```
     * POST /smuggling/clte HTTP/1.1
     * Host: localhost
     * Content-Length: 50
     * Transfer-Encoding: chunked
     *
     * 0
     *
     * GET /admin HTTP/1.1
     * Host: localhost
     *
     * ```
     *
     * @param request HTTP 请求
     * @return 处理结果
     */
    @PostMapping("/clte")
    public String clteSmuggling(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();

        // 记录请求头
        sb.append("=== Request Headers ===\n");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            sb.append(name).append(": ").append(request.getHeader(name)).append("\n");
        }

        // 记录 Content-Length 和 Transfer-Encoding
        String contentLength = request.getHeader("Content-Length");
        String transferEncoding = request.getHeader("Transfer-Encoding");

        sb.append("\n=== Smuggling Analysis ===\n");
        sb.append("Content-Length: ").append(contentLength != null ? contentLength : "not set").append("\n");
        sb.append("Transfer-Encoding: ").append(transferEncoding != null ? transferEncoding : "not set").append("\n");

        // 漏洞代码：同时处理两种编码方式
        // 前端代理可能使用 Content-Length，后端使用 Transfer-Encoding
        String body = getRequestBody(request);
        sb.append("\n=== Request Body ===\n");
        sb.append(body);

        // 检测是否有 Smuggling 尝试
        if (body != null && body.contains("GET /") || body.contains("POST /")) {
            sb.append("\n[!] 检测到可能的请求走私尝试！");
        }

        return sb.toString();
    }

    /**
     * TE.CL Smuggling - Transfer-Encoding / Content-Length 不匹配
     *
     * 漏洞场景：
     * - 前端代理优先使用 Transfer-Encoding
     * - 后端服务器优先使用 Content-Length
     *
     * 测试方法：
     * ```
     * POST /smuggling/tecl HTTP/1.1
     * Host: localhost
     * Transfer-Encoding: chunked
     * Content-Length: 10
     *
     * 5
     * hello
     * 0
     *
     * GET /admin HTTP/1.1
     * Host: localhost
     *
     * ```
     *
     * @param request HTTP 请求
     * @return 处理结果
     */
    @PostMapping("/tecl")
    public String teclSmuggling(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();

        // 记录请求头
        sb.append("=== Request Headers ===\n");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            sb.append(name).append(": ").append(request.getHeader(name)).append("\n");
        }

        // 记录 Content-Length 和 Transfer-Encoding
        String contentLength = request.getHeader("Content-Length");
        String transferEncoding = request.getHeader("Transfer-Encoding");

        sb.append("\n=== Smuggling Analysis ===\n");
        sb.append("Content-Length: ").append(contentLength != null ? contentLength : "not set").append("\n");
        sb.append("Transfer-Encoding: ").append(transferEncoding != null ? transferEncoding : "not set").append("\n");

        // 漏洞代码：同时处理两种编码方式
        String body = getRequestBody(request);
        sb.append("\n=== Request Body ===\n");
        sb.append(body);

        // 检测是否有 Smuggling 尝试
        if (body != null && body.contains("GET /") || body.contains("POST /")) {
            sb.append("\n[!] 检测到可能的请求走私尝试！");
        }

        return sb.toString();
    }

    /**
     * CL.CL Smuggling - 双 Content-Length
     *
     * 漏洞场景：
     * - 前端和后端对多个 Content-Length 的处理方式不同
     *
     * 测试方法：
     * ```
     * POST /smuggling/clcl HTTP/1.1
     * Host: localhost
     * Content-Length: 10
     * Content-Length: 5
     *
     * helloGET /admin HTTP/1.1
     *
     * ```
     *
     * @param request HTTP 请求
     * @return 处理结果
     */
    @PostMapping("/clcl")
    public String clclSmuggling(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();

        // 记录所有 Content-Length 头
        sb.append("=== Request Headers ===\n");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if (name.equalsIgnoreCase("Content-Length")) {
                // 获取所有 Content-Length 值
                Enumeration<String> values = request.getHeaders(name);
                while (values.hasMoreElements()) {
                    sb.append(name).append(": ").append(values.nextElement()).append("\n");
                }
            } else {
                sb.append(name).append(": ").append(request.getHeader(name)).append("\n");
            }
        }

        String body = getRequestBody(request);
        sb.append("\n=== Request Body ===\n");
        sb.append(body);

        // 检测多个 Content-Length
        int clCount = 0;
        Enumeration<String> clValues = request.getHeaders("Content-Length");
        while (clValues.hasMoreElements()) {
            clValues.nextElement();
            clCount++;
        }

        if (clCount > 1) {
            sb.append("\n[!] 检测到多个 Content-Length 头（走私风险）！");
        }

        return sb.toString();
    }

    /**
     * 漏洞场景 - 缓存投毒
     *
     * 攻击者通过走私请求污染前端缓存，让其他用户访问恶意内容
     *
     * @param request HTTP 请求
     * @return 处理结果
     */
    @PostMapping("/cache")
    public String cachePoisoning(HttpServletRequest request) {
        String body = getRequestBody(request);

        // 漏洞代码：没有验证请求来源，直接处理
        // 可能被用于缓存投毒
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

    /**
     * 漏洞场景 - 绕过认证
     *
     * 通过走私请求访问受保护端点
     *
     * @param request HTTP 请求
     * @return 处理结果
     */
    @GetMapping("/admin")
    public String bypassAuth(HttpServletRequest request) {
        // 漏洞代码：没有检查请求是否由走私产生
        // 攻击者可能通过走私请求绕过前端认证

        String forwardedFor = request.getHeader("X-Forwarded-For");
        String originalUri = request.getHeader("X-Original-URI");

        return String.format(
            "=== Admin Panel ===\n" +
            "This endpoint should be protected!\n" +
            "X-Forwarded-For: %s\n" +
            "X-Original-URI: %s\n" +
            "\n" +
            "[!] 如果你能看到这个消息，可能存在请求走私漏洞",
            forwardedFor != null ? forwardedFor : "not set",
            originalUri != null ? originalUri : "not set"
        );
    }

    /**
     * 安全版本 - CL.TE
     *
     * 修复方案：只使用 Content-Length，忽略 Transfer-Encoding
     *
     * @param request HTTP 请求
     * @return 处理结果
     */
    @PostMapping("/safe/clte")
    public String safeClte(HttpServletRequest request) {
        // 安全代码：只使用 Content-Length
        String contentLength = request.getHeader("Content-Length");
        String transferEncoding = request.getHeader("Transfer-Encoding");

        // 如果同时存在两个头，优先使用 Content-Length
        if (transferEncoding != null) {
            return "Security Policy: Transfer-Encoding is ignored. Only Content-Length is accepted.";
        }

        String body = getRequestBody(request);
        return String.format(
            "Safe Request Processing\n" +
            "Content-Length: %s\n" +
            "Body: %s",
            contentLength,
            body != null ? body.substring(0, Math.min(100, body.length())) : "empty"
        );
    }

    /**
     * 安全版本 - TE.CL
     *
     * 修复方案：统一使用一种编码方式
     *
     * @param request HTTP 请求
     * @return 处理结果
     */
    @PostMapping("/safe/tecl")
    public String safeTecl(HttpServletRequest request) {
        // 安全代码：拒绝包含冲突的请求
        String contentLength = request.getHeader("Content-Length");
        String transferEncoding = request.getHeader("Transfer-Encoding");

        // 如果同时存在两个头，拒绝请求
        if (contentLength != null && transferEncoding != null) {
            return "Security Policy: Request rejected - conflicting Content-Length and Transfer-Encoding headers.";
        }

        String body = getRequestBody(request);
        return String.format(
            "Safe Request Processing\n" +
            "Content-Length: %s\n" +
            "Transfer-Encoding: %s\n" +
            "Body: %s",
            contentLength,
            transferEncoding,
            body != null ? body.substring(0, Math.min(100, body.length())) : "empty"
        );
    }

    /**
     * 安全版本 - 检测请求走私
     *
     * 修复方案：检测可疑的请求模式
     *
     * @param request HTTP 请求
     * @return 处理结果
     */
    @PostMapping("/safe/detect")
    public String safeDetect(HttpServletRequest request) {
        // 检测多个 Content-Length
        int clCount = 0;
        Enumeration<String> clValues = request.getHeaders("Content-Length");
        while (clValues.hasMoreElements()) {
            clValues.nextElement();
            clCount++;
        }

        // 检测 Transfer-Encoding 中的 chunked
        String transferEncoding = request.getHeader("Transfer-Encoding");
        boolean hasChunked = transferEncoding != null && transferEncoding.toLowerCase().contains("chunked");

        // 检测请求体中的可疑模式
        String body = getRequestBody(request);
        boolean hasEmbeddedRequest = body != null && (body.contains("\r\n\r\n") || body.contains("GET /") || body.contains("POST /"));

        if (clCount > 1) {
            return "Security Alert: Multiple Content-Length headers detected!";
        }

        if (hasChunked && request.getHeader("Content-Length") != null) {
            return "Security Alert: Both Content-Length and Transfer-Encoding detected!";
        }

        if (hasEmbeddedRequest) {
            return "Security Alert: Possible request smuggling detected!";
        }

        return "Safe Request: No smuggling patterns detected.";
    }

    /**
     * 获取请求体
     */
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

    /**
     * 信息端点 - 显示 Smuggling 测试信息
     *
     * @return 测试信息
     */
    @GetMapping("/info")
    public String info() {
        return String.format(
            "HTTP Request Smuggling Vulnerability Demo%n" +
            "==========================================%n" +
            "Java Version: %s%n" +
            "OS: %s %s%n" +
            "%n" +
            "Vulnerability Endpoints:%n" +
            "- CL.TE: POST /smuggling/clte%n" +
            "- TE.CL: POST /smuggling/tecl%n" +
            "- CL.CL: POST /smuggling/clcl%n" +
            "- Cache Poisoning: POST /smuggling/cache%n" +
            "- Auth Bypass: GET /smuggling/admin%n" +
            "%n" +
            "Safe Endpoints:%n" +
            "- Safe CL.TE: POST /smuggling/safe/clte%n" +
            "- Safe TE.CL: POST /smuggling/safe/tecl%n" +
            "- Detection: POST /smuggling/safe/detect%n" +
            "%n" +
            "Testing Tools:%n" +
            "- smuggler: https://github.com/defparam/smuggler%n" +
            "- httpsmuggle: https://github.com/Hadesy2k1/httpsmuggle%n" +
            "- burp-request-smuggler: https://github.com/narfindividuals/burp-request-smuggler",
            System.getProperty("java.version"),
            System.getProperty("os.name"),
            System.getProperty("os.version")
        );
    }
}
