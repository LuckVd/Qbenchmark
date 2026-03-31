package com.vulnlab.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * CORS、CSRF、Cookies 安全漏洞演示
 *
 * 漏洞说明：
 * 1. CORS 配置不当，允许任意源访问
 * 2. CSRF 缺少 token 验证
 * 3. Cookie 缺少 Secure/HttpOnly 标志
 *
 * 修复方案：
 * 1. 严格配置 CORS 白名单
 * 2. 实现 CSRF token 验证
 * 3. 设置 Cookie 安全标志
 *
 * @author VulnLab
 */
@RestController
@RequestMapping("/cors")
class CorsController {

    private static final Logger logger = LoggerFactory.getLogger(CorsController.class);

    /**
     * CORS 漏洞 - 允许任意源访问
     *
     * 漏洞原理：设置 Access-Control-Allow-Origin: *
     * 攻击向量：恶意网站可以读取本站点的敏感数据
     *
     * 测试步骤：
     * 1. 在恶意网站发起跨域请求
     * 2. 读取敏感数据
     *
     * 命令示例:
     * curl -H "Origin: http://evil.com" -H "Access-Control-Request-Method: GET" \
     *   -X OPTIONS http://localhost:8080/cors/simple -v
     *
     * @param response HTTP 响应
     * @return 敏感数据
     */
    @GetMapping("/simple")
    public Map<String, Object> simpleCors(HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();

        // 漏洞代码：允许任意源跨域访问
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        result.put("message", "Sensitive data exposed via CORS");
        result.put("data", "This should not be accessible from arbitrary origins");
        result.put("warning", "CORS misconfigured - allows access from any origin!");

        logger.info("CORS request - Access-Control-Allow-Origin: *");
        return result;
    }

    /**
     * CORS 漏洞 - 反射源
     *
     * 漏洞原理：将请求的 Origin 反射到响应头
     * 攻击向量：任意源可以读取响应
     *
     * 命令示例:
     * curl -H "Origin: http://evil.com" http://localhost:8080/cors/reflect -v
     *
     * @param origin 请求来源
     * @param response HTTP 响应
     * @return 敏感数据
     */
    @GetMapping("/reflect")
    public Map<String, Object> reflectCors(@RequestHeader(value = "Origin", defaultValue = "*") String origin,
                                           HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();

        // 漏洞代码：反射 Origin 到响应头
        response.setHeader("Access-Control-Allow-Origin", origin);
        response.setHeader("Access-Control-Allow-Credentials", "true");

        result.put("message", "Origin reflected in CORS header");
        result.put("origin", origin);
        result.put("warning", "Any origin can access this endpoint!");

        logger.info("CORS request - Origin reflected: {}", origin);
        return result;
    }

    /**
     * CORS 漏洞 - null 源
     *
     * 漏洞原理：允许 null 源访问（本地文件可绕过）
     *
     * 命令示例:
     * curl -H "Origin: null" http://localhost:8080/cors/null
     *
     * @param response HTTP 响应
     * @return 敏感数据
     */
    @GetMapping("/null")
    public Map<String, Object> nullCors(HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();

        // 漏洞代码：允许 null 源
        response.setHeader("Access-Control-Allow-Origin", "null");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        result.put("message", "null origin allowed via CORS");
        result.put("warning", "Local file:// URLs can use 'null' origin to bypass CORS!");

        logger.info("CORS request - null origin allowed");
        return result;
    }
}

/**
 * CSRF 漏洞演示
 */
@RestController
@RequestMapping("/csrf")
class CsrfController {

    private static final Logger logger = LoggerFactory.getLogger(CsrfController.class);

    /**
     * CSRF 漏洞 - 无 Token 验证的敏感操作
     *
     * 漏洞原理：不验证 CSRF token，可直接执行敏感操作
     * 攻击向量：构造恶意页面诱导用户访问
     *
     * 攻击示例：
     * <img src="http://localhost:8080/csrf/vuln?action=transfer&to=attacker&amount=1000">
     *
     * 命令示例:
     * curl "http://localhost:8080/csrf/vuln?action=transfer&to=attacker&amount=1000"
     *
     * @param action 操作类型
     * @param to 目标用户
     * @param amount 金额
     * @return 操作结果
     */
    @GetMapping("/vuln")
    @PostMapping("/vuln")
    public Map<String, Object> csrfVuln(@RequestParam("action") String action,
                                        @RequestParam(value = "to", defaultValue = "unknown") String to,
                                        @RequestParam(value = "amount", defaultValue = "0") String amount) {
        Map<String, Object> result = new HashMap<>();

        // 漏洞代码：不验证 CSRF token，直接执行操作
        logger.info("CSRF vulnerable action: {} to {}, amount: {}", action, to, amount);

        result.put("action", action);
        result.put("to", to);
        result.put("amount", amount);
        result.put("status", "success");
        result.put("warning", "No CSRF token verification - vulnerable to cross-site requests!");

        return result;
    }

    /**
     * CSRF 漏洞 - GET 请求修改状态
     *
     * 漏洞原理：使用 GET 请求执行状态修改操作
     * 攻击向量：更容易构造 CSRF 攻击
     *
     * 命令示例:
     * curl "http://localhost:8080/csrf/delete?id=1"
     *
     * @param id 删除的目标 ID
     * @return 操作结果
     */
    @GetMapping("/delete")
    public Map<String, Object> csrfDelete(@RequestParam("id") String id) {
        Map<String, Object> result = new HashMap<>();

        // 漏洞代码：GET 请求修改状态
        logger.warn("CSRF delete action for id: {}", id);

        result.put("action", "delete");
        result.put("id", id);
        result.put("status", "deleted");
        result.put("warning", "State-changing operation via GET request - CSRF vulnerable!");

        return result;
    }
}

/**
 * Cookies 安全漏洞演示
 */
@RestController
@RequestMapping("/cookies")
class CookiesController {

    private static final Logger logger = LoggerFactory.getLogger(CookiesController.class);

    /**
     * Cookie 漏洞 - 不安全的 Cookie 设置
     *
     * 漏洞原理：Cookie 缺少 Secure/HttpOnly 标志
     * 攻击向量：XSS 可以窃取 Cookie
     *
     * 命令示例:
     * curl -c - http://localhost:8080/cookies/set?name=session&value=sensitive123
     *
     * @param name Cookie 名称
     * @param value Cookie 值
     * @param response HTTP 响应
     * @return 设置结果
     */
    @GetMapping("/set")
    public Map<String, Object> setInsecureCookie(@RequestParam("name") String name,
                                                  @RequestParam("value") String value,
                                                  HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();

        // 漏洞代码：设置 Cookie 时不使用 Secure 和 HttpOnly 标志
        Cookie cookie = new Cookie(name, value);
        // 注释掉了安全标志
        // cookie.setSecure(true);
        // cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        result.put("message", "Cookie set");
        result.put("name", name);
        result.put("value", value);
        result.put("warning", "Cookie set without Secure/HttpOnly flags - vulnerable to XSS!");

        logger.info("Insecure cookie set: {}={}", name, value);
        return result;
    }

    /**
     * Cookie 漏洞 - 敏感信息存储在 Cookie
     *
     * 漏洞原理：将敏感信息直接存储在 Cookie 中
     * 攻击向量：客户端可修改 Cookie 数据
     *
     * 命令示例:
     * curl -b "user=admin" http://localhost:8080/cookies/profile
     *
     * @param user 用户 Cookie（从请求读取）
     * @return 用户信息
     */
    @GetMapping("/profile")
    public Map<String, Object> getProfile(@CookieValue(value = "user", defaultValue = "guest") String user) {
        Map<String, Object> result = new HashMap<>();

        // 漏洞代码：直接使用 Cookie 中的值作为用户身份
        result.put("username", user);
        result.put("role", user.equals("admin") ? "administrator" : "user");
        result.put("data", "Sensitive data for " + user);
        result.put("warning", "User identity taken from cookie - can be forged!");

        logger.info("Profile accessed via cookie: {}", user);
        return result;
    }

    /**
     * Cookie 漏洞 - 明文存储敏感数据
     *
     * 命令示例:
     * curl -c - "http://localhost:8080/cookies/login?username=admin&password=secret"
     *
     * @param username 用户名
     * @param password 密码
     * @param response HTTP 响应
     * @return 登录结果
     */
    @GetMapping("/login")
    public Map<String, Object> insecureLogin(@RequestParam("username") String username,
                                              @RequestParam("password") String password,
                                              HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();

        // 漏洞代码：将凭证明文存储在 Cookie 中
        Cookie sessionCookie = new Cookie("session", username + ":" + password);
        sessionCookie.setPath("/");
        response.addCookie(sessionCookie);

        Cookie userCookie = new Cookie("user", username);
        userCookie.setPath("/");
        response.addCookie(userCookie);

        result.put("message", "Logged in");
        result.put("username", username);
        result.put("warning", "Credentials stored in plaintext cookie!");

        logger.warn("Insecure login - credentials in cookie: {}:{}", username, password);
        return result;
    }
}
