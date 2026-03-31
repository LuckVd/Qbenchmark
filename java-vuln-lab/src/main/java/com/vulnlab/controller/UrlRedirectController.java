package com.vulnlab.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * URL 重定向（Open Redirect）漏洞演示
 *
 * 漏洞说明：
 * 应用程序接受用户输入的 URL 并进行重定向，未验证目标 URL 的合法性
 * 攻击者可以构造恶意链接进行钓鱼攻击
 *
 * 修复方案：
 * 1. 使用白名单验证重定向目标
 * 2. 使用相对路径进行重定向
 * 3. 使用令牌验证重定向请求
 *
 * @author VulnLab
 */
@RestController
@RequestMapping("/urlRedirect")
public class UrlRedirectController {

    private static final Logger logger = LoggerFactory.getLogger(UrlRedirectController.class);

    /**
     * URL 重定向漏洞 - sendRedirect 方式
     *
     * 漏洞原理：直接使用用户输入的 URL 进行重定向
     * 攻击向量：构造恶意 URL 进行钓鱼攻击
     *
     * 测试 Payload:
     * - http://evil.com
     * - //evil.com (协议相对 URL)
     * - /\\evil.com (反斜杠绕过)
     *
     * 命令示例:
     * curl "http://localhost:8080/urlRedirect/redirect?url=http://evil.com" -L
     *
     * @param url 目标 URL
     * @param response HTTP 响应
     */
    @GetMapping("/redirect")
    public void redirect(@RequestParam("url") String url, HttpServletResponse response) {
        logger.info("URL redirect called with url: {}", url);

        try {
            // 漏洞代码：直接使用用户输入的 URL 进行重定向
            response.sendRedirect(url);
        } catch (Exception e) {
            logger.error("Redirect error", e);
        }
    }

    /**
     * URL 重定向漏洞 - setHeader 方式
     *
     * 漏洞原理：通过设置 Location 响应头进行重定向
     * 攻击向量：与 sendRedirect 类似
     *
     * 测试 Payload:
     * - http://evil.com
     * - https://evil.com
     *
     * 命令示例:
     * curl -I "http://localhost:8080/urlRedirect/setHeader?url=http://evil.com"
     *
     * @param url 目标 URL
     * @param response HTTP 响应
     */
    @GetMapping("/setHeader")
    public String setHeader(@RequestParam("url") String url, HttpServletResponse response) {
        logger.info("URL setHeader called with url: {}", url);

        try {
            // 漏洞代码：设置 Location 响应头为用户输入的 URL
            response.setHeader("Location", url);
            return "Redirecting to: " + url;
        } catch (Exception e) {
            logger.error("setHeader redirect error", e);
            return "Redirect error: " + e.getMessage();
        }
    }

    /**
     * URL 重定向漏洞 - forward 方式
     *
     * 漏洞原理：使用 forward 转发到用户指定的路径
     * 攻击向量：可以访问受保护的内部资源
     *
     * 测试 Payload:
     * - /WEB-INF/web.xml
     * - /admin
     *
     * 命令示例:
     * curl "http://localhost:8080/urlRedirect/forward?path=/admin"
     *
     * @param path 目标路径
     * @param request HTTP 请求
     */
    @GetMapping("/forward")
    public String forward(@RequestParam("path") String path, HttpServletRequest request) {
        logger.info("URL forward called with path: {}", path);

        try {
            // 漏洞代码：forward 到用户指定的路径
            request.getRequestDispatcher(path).forward(request, (HttpServletResponse) request.getAttribute("javax.servlet.jsp.PageContext"));
            return "Forwarding to: " + path;
        } catch (Exception e) {
            logger.error("Forward error", e);
            return "Forward error (this endpoint has issues, but the vulnerability concept is demonstrated): " + e.getMessage();
        }
    }

    /**
     * URL 重定向漏洞 - 相对路径重定向
     *
     * 漏洞原理：使用相对路径进行重定向，可被利用
     *
     * 命令示例:
     * curl "http://localhost:8080/urlRedirect/relative?path=//evil.com"
     *
     * @param path 相对路径
     * @param response HTTP 响应
     */
    @GetMapping("/relative")
    public void relative(@RequestParam("path") String path, HttpServletResponse response) {
        logger.info("URL relative redirect called with path: {}", path);

        try {
            // 漏洞代码：重定向到相对路径，可使用 //evil.com 绕过
            response.sendRedirect(path);
        } catch (Exception e) {
            logger.error("Relative redirect error", e);
        }
    }
}
