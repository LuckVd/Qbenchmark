package com.vulnlab.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.crypto.AesCipherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * Shiro 反序列化漏洞演示 (Shiro-550)
 *
 * 漏洞说明：
 * - Shiro <= 1.2.4 存在反序列化漏洞
 * - rememberMe Cookie 使用 AES 加密，但密钥硬编码
 * - 攻击者可以使用默认密钥构造恶意 Cookie
 *
 * 修复方案：
 * - 升级 Shiro 到安全版本
 * - 使用随机密钥
 * - 禁用 rememberMe 功能
 *
 * @author VulnLab
 */
@Slf4j
@RestController
@RequestMapping("/shiro")
public class ShiroController {

    private static final Logger logger = LoggerFactory.getLogger(ShiroController.class);

    // Shiro 默认密钥 (Base64 decoded)
    private static final byte[] DEFAULT_KEY = java.util.Base64.getDecoder().decode("kPH+bIxk5D2deZiIxcaaaA==");

    private static final String DELETE_ME = "deleteMe";
    private static final String REMEMBER_ME_COOKIE = "rememberMe";

    private AesCipherService aesCipherService = new AesCipherService();

    /**
     * Shiro rememberMe 反序列化漏洞 (Shiro-550)
     *
     * 漏洞原理：
     * 1. Shiro 使用 AES 加密 rememberMe Cookie
     * 2. 默认密钥为: kPH+bIxk5D2deZiIxcaaaA==
     * 3. 解密后直接反序列化，导致 RCE
     *
     * 攻击步骤：
     * 1. 使用 ysoserial 生成 payload: java -jar ysoserial.jar CommonsCollections5 "calc.exe"
     * 2. 使用默认密钥 AES 加密 payload
     * 3. Base64 编码后设置为 rememberMe Cookie
     *
     * 测试工具：
     * - https://github.com/longofo/Shiro-550
     * - https://github.com/j1anFen/shiro_tool
     *
     * 命令示例:
     * curl http://localhost:8080/shiro/deserialize \
     *   -H "Cookie: rememberMe=<base64_encrypted_payload>"
     *
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @return 反序列化结果
     */
    @GetMapping("/deserialize")
    public String shiroDeserialize(HttpServletRequest request, HttpServletResponse response) {
        // 获取 rememberMe Cookie
        Cookie cookie = getCookie(request, REMEMBER_ME_COOKIE);
        if (cookie == null) {
            return "No rememberMe cookie. Right?";
        }

        String rememberMe = cookie.getValue();
        logger.info("Shiro rememberMe cookie found, length: {}", rememberMe.length());

        try {
            // Base64 解码
            byte[] b64DecodeRememberMe = java.util.Base64.getDecoder().decode(rememberMe);

            // AES 解密 (使用默认密钥)
            byte[] aesDecrypt = aesCipherService.decrypt(b64DecodeRememberMe, DEFAULT_KEY).getBytes();

            // 漏洞代码：直接反序列化解密后的数据
            ByteArrayInputStream bytes = new ByteArrayInputStream(aesDecrypt);
            ObjectInputStream in = new ObjectInputStream(bytes);
            Object obj = in.readObject();
            in.close();

            logger.info("Shiro deserialization completed. Object: {}", obj.getClass().getName());
            return "Shiro deserialization completed. Object: " + obj.getClass().getName();

        } catch (Exception e) {
            logger.error("Shiro deserialization error", e);

            // 添加 deleteMe Cookie (Shiro 的错误处理方式)
            addCookie(response, REMEMBER_ME_COOKIE, DELETE_ME);

            return "RememberMe cookie decrypt error. Set deleteMe cookie success. Error: " + e.getMessage();
        }
    }

    /**
     * 获取指定名称的 Cookie
     */
    private Cookie getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }
        return null;
    }

    /**
     * 添加 Cookie
     */
    private boolean addCookie(HttpServletResponse response, String name, String value) {
        try {
            Cookie cookie = new Cookie(name, value);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
            return true;
        } catch (Exception e) {
            logger.error("Failed to add cookie", e);
            return false;
        }
    }

    /**
     * Shiro 信息端点 - 显示漏洞信息
     *
     * @return 漏洞说明
     */
    @GetMapping("/info")
    public String info() {
        StringBuilder info = new StringBuilder();
        info.append("Shiro-550 反序列化漏洞演示\n");
        info.append("==========================================\n");
        info.append("Shiro Version: 1.2.4 (漏洞版本)\n");
        info.append("Default Key: kPH+bIxk5D2deZiIxcaaaA==\n");
        info.append("Cipher: AES\n");
        info.append("\n");
        info.append("测试步骤:\n");
        info.append("1. 使用 ysoserial 生成 payload\n");
        info.append("2. 使用默认密钥 AES 加密\n");
        info.append("3. Base64 编码后设置为 rememberMe Cookie\n");
        info.append("4. 发送请求到此端点\n");
        return info.toString();
    }
}
