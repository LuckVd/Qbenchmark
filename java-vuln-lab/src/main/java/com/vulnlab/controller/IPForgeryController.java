package com.vulnlab.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * IP 伪造漏洞控制器
 *
 * 漏洞说明：
 * - 应用程序直接信任 X-Forwarded-For 等 HTTP 头获取客户端 IP
 * - 攻击者可以伪造这些头来绕过 IP 限制
 *
 * 常用伪造头：
 * - X-Forwarded-For
 * - X-Real-IP
 * - Client-IP
 * - CF-Connecting-IP
 *
 * 修复方案：
 * 1. 不信任 HTTP 头中的 IP
 * 2. 使用 request.getRemoteAddr() 获取真实 IP
 * 3. 在反向代理层面正确配置 IP 传递
 *
 * @author VulnLab
 */
@RestController
@RequestMapping("/ip")
public class IPForgeryController {

    private static final Logger logger = LoggerFactory.getLogger(IPForgeryController.class);

    /**
     * IP 伪造漏洞 - X-Forwarded-For 伪造
     *
     * 漏洞原理：直接信任 X-Forwarded-For 头获取客户端 IP
     * 攻击向量：伪造 X-Forwarded-For 头绕过 IP 限制
     *
     * 测试用例：
     * - 正常: 直接访问，需要真实 IP 为 127.0.0.1
     * - 伪造: curl -H "X-Forwarded-For: 127.0.0.1" http://localhost:8080/ip/spoof
     *
     * @param request HTTP 请求
     * @return 访问结果
     */
    @GetMapping("/spoof")
    public Map<String, Object> spoof(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        // 漏洞代码：直接信任 X-Forwarded-For 头，可能被伪造
        String xff = request.getHeader("X-Forwarded-For");
        String ip = (xff != null) ? xff : request.getRemoteAddr();

        logger.info("IP spoof attempt - X-Forwarded-For: {}, RemoteAddr: {}", xff, request.getRemoteAddr());

        result.put("detected_ip", ip);
        result.put("xff_header", xff);
        result.put("remote_addr", request.getRemoteAddr());

        // 假设只允许本地 IP 访问敏感资源
        if (Objects.equals(ip, "127.0.0.1") || Objects.equals(ip, "::1") || Objects.equals(ip, "localhost")) {
            result.put("access", "GRANTED");
            result.put("message", "访问成功！IP 被识别为可信地址。");
            result.put("flag", "FLAG{X_FORWARDED_FOR_SPOOF_SUCCESS}");
            logger.warn("IP spoof successful - IP restriction bypassed!");
        } else {
            result.put("access", "DENIED");
            result.put("message", "访问被拒绝：只允许本地 IP (127.0.0.1) 访问此资源");
        }

        return result;
    }

    /**
     * IP 获取安全版本示例（用于对比）
     *
     * @param request HTTP 请求
     * @return 客户端 IP 信息
     */
    @GetMapping("/safe")
    public Map<String, Object> safe(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        // 修复：使用 getRemoteAddr() 获取真实 IP，不信任 HTTP 头
        String realIp = request.getRemoteAddr();
        String xff = request.getHeader("X-Forwarded-For");

        logger.info("IP safe check - RemoteAddr: {}, X-Forwarded-For: {}", realIp, xff);

        result.put("real_ip", realIp);
        result.put("xff_header", xff);
        result.put("xff_ignored", true);
        result.put("message", "使用 RemoteAddr 获取真实 IP，忽略可伪造的 HTTP 头");

        // 基于真实 IP 进行访问控制
        if (Objects.equals(realIp, "127.0.0.1") || Objects.equals(realIp, "::1") || Objects.equals(realIp, "localhost")) {
            result.put("access", "GRANTED");
            result.put("message", "访问成功！来自可信地址。");
        } else {
            result.put("access", "DENIED");
            result.put("message", "访问被拒绝：您的 IP (" + realIp + ") 不在白名单中");
        }

        return result;
    }

    /**
     * 显示所有与 IP 相关的 HTTP 头
     *
     * @param request HTTP 请求
     * @return IP 相关头信息
     */
    @GetMapping("/headers")
    public Map<String, String> showHeaders(HttpServletRequest request) {
        Map<String, String> result = new HashMap<>();

        result.put("RemoteAddr", request.getRemoteAddr());
        result.put("RemoteHost", request.getRemoteHost());
        result.put("X-Forwarded-For", request.getHeader("X-Forwarded-For"));
        result.put("X-Real-IP", request.getHeader("X-Real-IP"));
        result.put("Client-IP", request.getHeader("Client-IP"));
        result.put("CF-Connecting-IP", request.getHeader("CF-Connecting-IP"));
        result.put("True-Client-IP", request.getHeader("True-Client-IP"));

        return result;
    }

    /**
     * IP 伪造信息端点
     *
     * @return 漏洞说明
     */
    @GetMapping("/info")
    public String info() {
        return "IP 伪造漏洞演示\n" +
               "==============\n" +
               "漏洞端点：GET /ip/spoof\n" +
               "\n" +
               "测试 Payload：\n" +
               "1. 直接访问（被拒绝）:\n" +
               "   curl http://localhost:8080/ip/spoof\n" +
               "\n" +
               "2. IP 伪造（绕过限制）:\n" +
               "   curl -H \"X-Forwarded-For: 127.0.0.1\" http://localhost:8080/ip/spoof\n" +
               "\n" +
               "安全版本：GET /ip/safe\n" +
               "\n" +
               "查看所有 IP 头：GET /ip/headers\n";
    }
}
