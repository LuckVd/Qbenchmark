package com.vulnlab.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * CRLF 注入（HTTP Response Splitting）漏洞演示
 *
 * 漏洞说明：
 * CRLF (%0d%0a 或 \r\n) 注入允许攻击者：
 * 1. 注入任意 HTTP 响应头
 * 2. 设置 Cookie (XSS 配合)
 * 3. 响应拆分攻击
 * 4. HTTP 缓存投毒
 *
 * 修复方案：
 * 1. 验证和过滤用户输入
 * 2. 移除 CRLF 字符
 * 3. 使用安全的 API 设置响应头
 *
 * @author VulnLab
 */
@RestController
@RequestMapping("/crlf")
public class CrlfInjectionController {

    private static final Logger logger = LoggerFactory.getLogger(CrlfInjectionController.class);

    /**
     * CRLF 注入漏洞 - 响应头注入
     *
     * 漏洞原理：将用户输入直接设置到响应头，不过滤 CRLF 字符
     * 攻击向量：注入 Set-Cookie、Location 等头
     *
     * 测试 Payload:
     * - admin\r\nSet-Cookie: admin=true
     * - admin\r\nLocation: http://evil.com
     *
     * URL 编码后的 Payload:
     * - admin%0D%0ASet-Cookie:%20admin=true
     * - admin%0D%0ALocation:%20http://evil.com
     *
     * 命令示例:
     * curl -i "http://localhost:8080/crlf/injection?name=admin%0d%0aSet-Cookie:%20admin=true"
     *
     * @param name 用户输入
     * @param response HTTP 响应
     * @return 响应结果
     */
    @GetMapping("/injection")
    public Map<String, Object> crlfInjection(@RequestParam("name") String name,
                                              HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();

        logger.info("CRLF injection called with name: {}", name);

        // 漏洞代码：直接将用户输入设置到响应头，不过滤 CRLF
        response.setHeader("X-User-Name", name);

        result.put("message", "Header set");
        result.put("name", name);
        result.put("warning", "User input directly set in header - CRLF injection possible!");
        result.put("example", "Try: name=admin%0d%0aSet-Cookie:%20admin=true");

        logger.warn("Potential CRLF injection in header: {}", name);
        return result;
    }

    /**
     * CRLF 注入漏洞 - Cookie 注入
     *
     * 漏洞原理：通过 CRLF 注入设置任意 Cookie
     * 攻击向量：配合 XSS 窃取 Session
     *
     * 测试 Payload:
     * - user\r\nSet-Cookie: admin=true
     * - user\r\nSet-Cookie: session=stolen
     *
     * 命令示例:
     * curl -i -c - "http://localhost:8080/crlf/cookie?name=user%0d%0aSet-Cookie:%20admin=true"
     *
     * @param name 用户输入
     * @param response HTTP 响应
     * @return 响应结果
     */
    @GetMapping("/cookie")
    public Map<String, Object> crlfCookie(@RequestParam("name") String name,
                                           HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();

        logger.info("CRLF cookie injection called with name: {}", name);

        // 漏洞代码：通过 X-Username 头注入，可设置任意 Cookie
        response.setHeader("X-Username", name);

        result.put("message", "Cookie injection possible");
        result.put("name", name);
        result.put("warning", "Can inject Set-Cookie header via CRLF!");
        result.put("example", "Try: name=user%0d%0aSet-Cookie:%20admin=true");

        logger.warn("Cookie injection via CRLF: {}", name);
        return result;
    }

    /**
     * CRLF 注入漏洞 - 响应体注入
     *
     * 漏洞原理：CRLF 可以拆分 HTTP 响应
     * 攻击向量：响应拆分、缓存投毒
     *
     * 测试 Payload:
     * - test\r\n\r\n<script>alert(1)</script>
     * - test\r\nContent-Length:%200\r\n\r\nHTTP/1.1%20200%20OK...
     *
     * 命令示例:
     * curl "http://localhost:8080/crlf/body?content=test%0d%0a%0d%0a<script>alert(1)</script>"
     *
     * @param content 用户输入
     * @return 响应结果
     */
    @GetMapping("/body")
    public Map<String, Object> crlfBody(@RequestParam("content") String content) {
        Map<String, Object> result = new HashMap<>();

        logger.info("CRLF body injection called with content: {}", content);

        // 漏洞代码：不处理 CRLF，可能导致响应拆分
        result.put("message", "Content received");
        result.put("content", content);
        result.put("warning", "CRLF in body can lead to response splitting!");
        result.put("example", "Try: content=test%0d%0a%0d%0a<script>alert(1)</script>");

        logger.warn("Response splitting attempt: {}", content);
        return result;
    }

    /**
     * CRLF 注入漏洞 - Location 注入
     *
     * 漏洞原理：通过 CRLF 注入修改重定向目标
     * 攻击向量：钓鱼攻击
     *
     * 测试 Payload:
     * - /home\r\nLocation: http://evil.com
     *
     * 命令示例:
     * curl -i "http://localhost:8080/crlf/redirect?path=/home%0d%0aLocation:%20http://evil.com"
     *
     * @param path 重定向路径
     * @param response HTTP 响应
     * @return 响应结果
     */
    @GetMapping("/redirect")
    public Map<String, Object> crlfRedirect(@RequestParam("path") String path,
                                             HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();

        logger.info("CRLF redirect called with path: {}", path);

        // 漏洞代码：在 Location 头中注入 CRLF
        response.setHeader("Location", path);

        result.put("message", "Redirect header set");
        result.put("path", path);
        result.put("warning", "Can inject arbitrary headers via CRLF in Location!");
        result.put("example", "Try: path=/home%0d%0aLocation:%20http://evil.com");

        logger.warn("Location header injection: {}", path);
        return result;
    }

    /**
     * CRLF 注入漏洞 - 反射型注入
     *
     * 漏洞原理：将用户输入反射到响应中
     *
     * 命令示例:
     * curl "http://localhost:8080/crlf/reflected?input=test%0d%0aSet-Cookie:%20admin=true"
     *
     * @param input 用户输入
     * @param response HTTP 响应
     * @return 响应结果
     */
    @GetMapping("/reflected")
    public Map<String, Object> crlfReflected(@RequestParam("input") String input,
                                              HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();

        logger.info("CRLF reflected input: {}", input);

        // 漏洞代码：反射用户输入到自定义头
        response.setHeader("X-Input", input);

        result.put("message", "Input reflected");
        result.put("input", input);
        result.put("warning", "User input reflected without CRLF filtering!");

        logger.warn("Reflected CRLF injection: {}", input);
        return result;
    }

    /**
     * CRLF 安全演示 - 修复后的版本
     *
     * 修复方案：过滤 CRLF 字符
     *
     * @param name 用户输入
     * @param response HTTP 响应
     * @return 响应结果
     */
    @GetMapping("/safe")
    public Map<String, Object> crlfSafe(@RequestParam("name") String name,
                                         HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();

        logger.info("CRLF safe called with name: {}", name);

        // 修复：过滤 CRLF 字符
        String safeName = name.replaceAll("[\r\n]", "");
        response.setHeader("X-User-Name", safeName);

        result.put("message", "Header set safely");
        result.put("original", name);
        result.put("sanitized", safeName);
        result.put("info", "CRLF characters filtered");

        logger.info("Safe header set: {}", safeName);
        return result;
    }
}
