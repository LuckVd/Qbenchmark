package com.vulnlab.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * XSS (跨站脚本攻击) 漏洞演示
 *
 * 漏洞说明：
 * - 应用程序未对用户输入进行过滤直接输出到页面
 * - 攻击者可注入恶意JavaScript代码
 *
 * XSS类型：
 * 1. 反射型XSS (Reflected XSS) - 恶意代码通过URL参数反射
 * 2. 存储型XSS (Stored XSS) - 恶意代码存储在服务器，每次访问都会执行
 * 3. DOM型XSS - 恶意代码通过DOM操作执行
 *
 * 常用payload：
 * - <script>alert(1)</script>
 * - <img src=x onerror=alert(1)>
 * - <svg onload=alert(1)>
 * - "autofocus onfocus=alert(1) //
 *
 * 修复方案：
 * 1. 对输出进行HTML转义
 * 2. 设置Content-Type: application/json
 * 3. 使用CSP (Content Security Policy)
 *
 * @author VulnLab
 */
@Controller
@RequestMapping("/xss")
public class XSSController {

    private static final Logger logger = LoggerFactory.getLogger(XSSController.class);

    /**
     * 反射型XSS漏洞 - GET方式
     *
     * 测试用例：
     * - http://localhost:8080/xss/reflect?vuln=<script>alert(1)</script>
     * - http://localhost:8080/xss/reflect?vuln=<img%20src=x%20onerror=alert(1)>
     *
     * @param vuln 输入内容
     * @return 原样返回
     */
    @GetMapping("/reflect")
    @ResponseBody
    public String reflectXss(@RequestParam(value = "vuln", defaultValue = "test") String vuln) {
        logger.info("XSS input: {}", vuln);
        // 漏洞代码：直接返回用户输入
        return vuln;
    }

    /**
     * 反射型XSS漏洞 - POST方式
     *
     * 测试用例：
     * curl -X POST -d "content=<script>alert(1)</script>" http://localhost:8080/xss/reflect/post
     *
     * @param content 输入内容
     * @return 原样返回
     */
    @PostMapping("/reflect/post")
    @ResponseBody
    public String reflectXssPost(@RequestParam("content") String content) {
        logger.info("XSS input: {}", content);
        // 漏洞代码：直接返回用户输入
        return content;
    }

    /**
     * 存储型XSS漏洞 - 第一步：存储
     *
     * 测试用例：
     * - http://localhost:8080/xss/stored/store?data=<script>alert(document.cookie)</script>
     *
     * @param data 要存储的数据
     * @param response HTTP响应
     * @return 提示信息
     */
    @GetMapping("/stored/store")
    @ResponseBody
    public String storeXss(@RequestParam("data") String data, HttpServletResponse response) {
        logger.info("Storing XSS payload: {}", data);

        // 漏洞代码：将用户输入存储到Cookie中
        Cookie cookie = new Cookie("xss_data", data);
        cookie.setMaxAge(3600); // 1小时
        cookie.setPath("/");
        response.addCookie(cookie);

        return "Data stored in cookie. Access /xss/stored/show to retrieve.";
    }

    /**
     * 存储型XSS漏洞 - 第二步：读取
     *
     * 测试用例：
     * - 先访问: http://localhost:8080/xss/stored/store?data=<script>alert(1)</script>
     * - 再访问: http://localhost:8080/xss/stored/show
     *
     * @param xssData Cookie中的数据
     * @return 原样返回存储的数据
     */
    @GetMapping("/stored/show")
    @ResponseBody
    public String showXss(@CookieValue(value = "xss_data", defaultValue = "No data found") String xssData) {
        logger.info("Retrieved XSS data: {}", xssData);
        // 漏洞代码：直接输出存储的数据
        return xssData;
    }

    /**
     * XSS漏洞 - 搜索框场景
     *
     * 测试用例：
     * - http://localhost:8080/xss/search?q=<script>alert(1)</script>
     *
     * @param q 搜索关键词
     * @return 搜索结果页面
     */
    @GetMapping("/search")
    @ResponseBody
    public String searchXss(@RequestParam(value = "q", defaultValue = "") String q) {
        logger.info("Search query: {}", q);

        // 漏洞代码：搜索关键词直接显示在结果中
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Search Results</title></head><body>");
        html.append("<h1>Search Results for: ").append(q).append("</h1>");
        html.append("<p>No results found.</p>");
        html.append("</body></html>");

        return html.toString();
    }

    /**
     * XSS安全代码 - HTML转义
     *
     * 测试用例：
     * - http://localhost:8080/xss/reflect/sec?vuln=<script>alert(1)</script>
     * - 输出: &lt;script&gt;alert(1)&lt;/script&gt;
     *
     * @param vuln 输入内容
     * @return 转义后的内容
     */
    @GetMapping("/reflect/sec")
    @ResponseBody
    public String reflectXssSecure(@RequestParam(value = "vuln", defaultValue = "test") String vuln) {
        logger.info("XSS input (secure): {}", vuln);
        // 安全代码：HTML转义
        return htmlEncode(vuln);
    }

    /**
     * HTML实体编码
     *
     * @param input 原始字符串
     * @return 转义后的字符串
     */
    private String htmlEncode(String input) {
        if (input == null) {
            return "";
        }
        input = StringUtils.replace(input, "&", "&amp;");
        input = StringUtils.replace(input, "<", "&lt;");
        input = StringUtils.replace(input, ">", "&gt;");
        input = StringUtils.replace(input, "\"", "&quot;");
        input = StringUtils.replace(input, "'", "&#x27;");
        input = StringUtils.replace(input, "/", "&#x2F;");
        return input;
    }
}
