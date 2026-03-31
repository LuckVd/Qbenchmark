package com.vulnlab.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathConstants;
import org.xml.sax.InputSource;

import java.io.StringReader;

/**
 * XPath 注入漏洞控制器
 *
 * 漏洞说明：
 * - 应用程序使用 XPath 查询 XML 文档时，直接拼接用户输入
 * - 攻击者可以通过特殊字符注入恶意 XPath 表达式
 *
 * 常用注入字符：
 * - ' 单引号闭合
 * - or 逻辑或
 * - and 逻辑与
 * - () 括号分组
 *
 * 修复方案：
 * 1. 使用参数化 XPath 查询
 * 2. 对用户输入进行转义
 * 3. 使用白名单验证
 *
 * @author VulnLab
 */
@RestController
@RequestMapping("/xpath")
public class XPathController {

    private static final Logger logger = LoggerFactory.getLogger(XPathController.class);

    /**
     * XPath 注入漏洞 - 登录绕过
     *
     * 漏洞原理：直接拼接用户输入到 XPath 查询中
     * 攻击向量：通过 XPath 注入绕过认证
     *
     * 测试用例：
     * - 正常: username=admin, password=secret123
     * - 注入: username=admin' or '1'='1, password=anything
     * - 注入: username=' or '1'='1, password=anything
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录结果
     */
    @PostMapping("/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password) {
        logger.info("XPath login attempt - username: {}", username);

        try {
            // 构造 XML 文档（模拟用户数据库）
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new StringReader("<users>"
                            + "<user>"
                            + "<username>admin</username>"
                            + "<password>secret123</password>"
                            + "<role>administrator</role>"
                            + "</user>"
                            + "<user>"
                            + "<username>user</username>"
                            + "<password>userpass</password>"
                            + "<role>normal</role>"
                            + "</user>"
                            + "</users>")));

            // 漏洞代码：直接拼接用户输入到 XPath 查询中
            XPath xpath = XPathFactory.newInstance().newXPath();
            String query = "/users/user[username='" + username + "' and password='" + password + "']";
            logger.info("XPath query: {}", query);

            NodeList nodes = (NodeList) xpath.evaluate(query, doc, XPathConstants.NODESET);

            // 检查查询结果
            if (nodes.getLength() > 0) {
                logger.warn("XPath injection successful - authentication bypassed!");
                return "登录成功！欢迎回来，" + username + "。<br>（注意：这可能是一个 XPath 注入漏洞）";
            } else {
                return "登录失败：用户名或密码错误";
            }
        } catch (Exception e) {
            logger.error("XPath error", e);
            return "发生错误：" + e.getMessage();
        }
    }

    /**
     * XPath 注入安全版本示例（用于对比）
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录结果
     */
    @PostMapping("/login/safe")
    public String loginSafe(@RequestParam("username") String username,
                            @RequestParam("password") String password) {
        logger.info("XPath safe login attempt - username: {}", username);

        // 安全验证：简单的输入验证，阻止特殊字符
        if (username.contains("'") || username.contains("\"") ||
            username.contains("or ") || username.contains("and ") ||
            password.contains("'") || password.contains("\"")) {
            return "登录失败：检测到非法字符";
        }

        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new StringReader("<users>"
                            + "<user>"
                            + "<username>admin</username>"
                            + "<password>secret123</password>"
                            + "<role>administrator</role>"
                            + "</user>"
                            + "<user>"
                            + "<username>user</username>"
                            + "<password>userpass</password>"
                            + "<role>normal</role>"
                            + "</user>"
                            + "</users>")));

            XPath xpath = XPathFactory.newInstance().newXPath();
            String query = "/users/user[username='" + username + "' and password='" + password + "']";
            NodeList nodes = (NodeList) xpath.evaluate(query, doc, XPathConstants.NODESET);

            if (nodes.getLength() > 0) {
                return "登录成功！欢迎回来，" + username;
            } else {
                return "登录失败：用户名或密码错误";
            }
        } catch (Exception e) {
            logger.error("XPath error", e);
            return "发生错误：" + e.getMessage();
        }
    }

    /**
     * XPath 注入信息端点
     *
     * @return 漏洞说明
     */
    @GetMapping("/info")
    public String info() {
        return "XPath 注入漏洞演示\n" +
               "==================\n" +
               "漏洞端点：POST /xpath/login\n" +
               "\n" +
               "测试 Payload：\n" +
               "1. 正常登录：\n" +
               "   username=admin&password=secret123\n" +
               "\n" +
               "2. XPath 注入绕过：\n" +
               "   username=admin' or '1'='1&password=anything\n" +
               "   username=' or '1'='1&password=anything\n" +
               "\n" +
               "安全版本：POST /xpath/login/safe\n";
    }
}
