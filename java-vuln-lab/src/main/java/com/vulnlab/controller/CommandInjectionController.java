package com.vulnlab.controller;

import com.vulnlab.util.CommandUtil;
import groovy.lang.GroovyShell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 命令注入漏洞演示
 *
 * 漏洞说明：
 * - 应用程序执行系统命令时，直接拼接用户输入
 * - 攻击者可以通过特殊字符注入恶意命令
 *
 * 常用注入字符：
 * - ; 命令分隔符 (Unix)
 * - | 管道符
 * - & 后台运行
 * - && 逻辑与
 * - || 逻辑或
 * - \n 换行符
 * - ` 反引号 (命令替换)
 * - $() 命令替换
 *
 * 修复方案：
 * 1. 使用白名单验证输入
 * 2. 使用参数化API（如ProcessBuilder配合数组参数）
 * 3. 避免直接拼接命令字符串
 *
 * @author VulnLab
 */
@RestController
@RequestMapping("/cmd")
public class CommandInjectionController {

    private static final Logger logger = LoggerFactory.getLogger(CommandInjectionController.class);

    /**
     * 命令注入漏洞 - Runtime.exec方式
     *
     * 测试用例：
     * - 正常: http://localhost:8080/cmd/runtime/vuln?filename=test.txt
     * - 注入: http://localhost:8080/cmd/runtime/vuln?filename=test.txt;cat%20/etc/passwd
     * - 注入: http://localhost:8080/cmd/runtime/vuln?filename=test.txt|whoami
     *
     * @param filename 文件名
     * @return 命令执行结果
     */
    @GetMapping("/runtime/vuln")
    public String runtimeVuln(@RequestParam("filename") String filename) {
        String result;

        try {
            // 漏洞代码：直接拼接用户输入到命令中
            String cmd = "ls -la /tmp/" + filename;
            logger.info("Command: {}", cmd);

            result = CommandUtil.executeByRuntime(cmd);
        } catch (Exception e) {
            logger.error("Command execution error", e);
            result = "Error: " + e.getMessage();
        }

        return result;
    }

    /**
     * 命令注入漏洞 - ProcessBuilder方式
     *
     * 测试用例：
     * - 正常: http://localhost:8080/cmd/processbuilder/vuln?dir=/var/log
     * - 注入: http://localhost:8080/cmd/processbuilder/vuln?dir=/var/log;cat%20/etc/passwd
     *
     * @param dir 目录名
     * @return 命令执行结果
     */
    @GetMapping("/processbuilder/vuln")
    public String processBuilderVuln(@RequestParam("dir") String dir) {
        String result;

        try {
            // 漏洞代码：在shell中执行拼接的命令
            String[] cmd = {"/bin/sh", "-c", "ls -la " + dir};
            logger.info("Command: /bin/sh -c ls -la {}", dir);

            result = CommandUtil.executeByProcessBuilder(cmd);
        } catch (Exception e) {
            logger.error("Command execution error", e);
            result = "Error: " + e.getMessage();
        }

        return result;
    }

    /**
     * 命令注入漏洞 - ping命令
     *
     * 漏洞原理：通过 sh -c 执行命令，使特殊字符可以被 shell 解析
     *
     * 测试用例：
     * - 正常: http://localhost:8080/cmd/ping/vuln?host=8.8.8.8
     * - 注入: http://localhost:8080/cmd/ping/vuln?host=8.8.8.8;ls%20-la
     * - 注入: http://localhost:8080/cmd/ping/vuln?host=8.8.8.8%26%26whoami
     * - 注入: http://localhost:8080/cmd/ping/vuln?host=8.8.8.8%7Cwhoami
     *
     * @param host 主机地址
     * @return ping结果
     */
    @GetMapping("/ping/vuln")
    public String pingVuln(@RequestParam("host") String host) {
        String result;

        try {
            // 漏洞代码：使用 sh -c 执行命令，允许 shell 解析特殊字符
            String[] cmd = {"/bin/sh", "-c", "ping -c 3 " + host};
            logger.info("Command: /bin/sh -c ping -c 3 {}", host);

            result = CommandUtil.executeByProcessBuilder(cmd);
        } catch (Exception e) {
            logger.error("Command execution error", e);
            result = "Error: " + e.getMessage();
        }

        return result;
    }

    /**
     * 命令注入漏洞 - 通过HTTP Header注入
     *
     * 测试用例：
     * - 使用curl: curl -H "X-Command:;whoami" http://localhost:8080/cmd/header/vuln
     * - 使用curl: curl -H "User-Agent: () { :; }; echo; cat /etc/passwd" http://localhost:8080/cmd/header/vuln
     *
     * @param request HTTP请求
     * @return 执行结果
     */
    @GetMapping("/header/vuln")
    public String headerVuln(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String xCommand = request.getHeader("X-Command");

        logger.info("User-Agent: {}", userAgent);
        logger.info("X-Command: {}", xCommand);

        String result = "";

        // 漏洞代码：将User-Agent直接用于命令执行
        try {
            String cmd = "echo " + (xCommand != null ? xCommand : userAgent);
            result = CommandUtil.executeByProcessBuilder(new String[]{"/bin/sh", "-c", cmd});
        } catch (Exception e) {
            logger.error("Command execution error", e);
            result = "Error: " + e.getMessage();
        }

        return result;
    }

    /**
     * 命令注入安全代码 - 使用白名单验证
     *
     * 测试用例：
     * - 正常: http://localhost:8080/cmd/ping/sec?host=8.8.8.8
     * - 被拒绝: http://localhost:8080/cmd/ping/sec?host=8.8.8.8;cat%20/etc/passwd
     *
     * @param host 主机地址
     * @return ping结果
     */
    @GetMapping("/ping/sec")
    public String pingSecure(@RequestParam("host") String host) {
        String result;

        // 安全验证：IP格式白名单
        if (!CommandUtil.isValidIp(host)) {
            return "Invalid IP address format";
        }

        try {
            String cmd = "ping -c 3 " + host;
            logger.info("Command: {}", cmd);
            result = CommandUtil.executeByRuntime(cmd);
        } catch (Exception e) {
            logger.error("Command execution error", e);
            result = "Error: " + e.getMessage();
        }

        return result;
    }

    /**
     * Groovy 脚本引擎注入漏洞
     *
     * 漏洞原理：使用 GroovyShell.evaluate() 直接执行用户输入的 Groovy 代码
     * 攻击向量：通过 Groovy 脚本语法执行任意系统命令
     *
     * 测试用例：
     * - 正常: http://localhost:8080/cmd/groovy?cmd=1+1
     * - 注入: http://localhost:8080/cmd/groovy?cmd="whoami".execute().text
     * - 注入: http://localhost:8080/cmd/groovy?cmd="ls -la".execute().text
     * - 注入: http://localhost:8080/cmd/groovy?cmd="calc.exe".execute()
     *
     * @param cmd Groovy 代码
     * @return 执行结果
     */
    @GetMapping("/groovy")
    public String groovyVuln(@RequestParam("cmd") String cmd) {
        logger.info("Groovy command: {}", cmd);

        try {
            // 漏洞代码：直接执行用户输入的 Groovy 代码
            GroovyShell shell = new GroovyShell();
            Object result = shell.evaluate(cmd);

            logger.info("Groovy result: {}", result);
            return "Groovy executed: " + result;
        } catch (Exception e) {
            logger.error("Groovy execution error", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Groovy 脚本引擎安全版本示例（用于对比）
     *
     * @param cmd Groovy 代码
     * @return 执行结果或错误提示
     */
    @GetMapping("/groovy/safe")
    public String groovySafe(@RequestParam("cmd") String cmd) {
        logger.info("Groovy safe command: {}", cmd);

        // 安全代码：使用白名单限制可执行的命令
        String[] allowedCommands = {"1+1", "2*2", "3-1", "\"test\"", "Math.PI"};

        for (String allowed : allowedCommands) {
            if (allowed.equals(cmd)) {
                try {
                    GroovyShell shell = new GroovyShell();
                    Object result = shell.evaluate(cmd);
                    return "Groovy safe executed: " + result;
                } catch (Exception e) {
                    return "Error: " + e.getMessage();
                }
            }
        }

        return "Command not allowed. Only safe commands are permitted.";
    }
}
