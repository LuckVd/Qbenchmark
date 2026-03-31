package com.envtest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * 命令注入漏洞控制器
 *
 * 漏洞端点: /ping
 * 漏洞类型: 命令注入
 * 危害等级: 高危
 *
 * 测试 payload:
 * - ?ip=127.0.0.1;whoami
 * - ?ip=127.0.0.1|cat /etc/passwd
 * - ?ip=127.0.0.1&&ls -la
 */
@RestController
public class CmdInjectionController {

    /**
     * 命令注入漏洞端点
     *
     * @param ip 要 ping 的 IP 地址（存在命令注入）
     * @return ping 命令执行结果
     */
    @GetMapping("/ping")
    public String ping(@RequestParam(defaultValue = "127.0.0.1") String ip) {
        StringBuilder result = new StringBuilder();

        try {
            // 漏洞代码: 直接拼接用户输入到命令中
            String command = "ping -c 1 " + ip;

            // 使用 sh -c 执行命令，支持 ; | && 等命令分隔符
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("sh", "-c", command);

            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            result.append("\nExit code: ").append(exitCode);

        } catch (Exception e) {
            result.append("Error: ").append(e.getMessage());
        }

        return result.toString();
    }

    /**
     * 环境检测端点 - 用于扫描器识别
     *
     * @return 环境信息
     */
    @GetMapping("/info")
    public String info() {
        return "Java Simple Vulnerability Lab v1.0.0\n" +
               "Purpose: Scanner environment validation\n" +
               "Vulnerability: Command Injection at /ping\n" +
               "Java: " + System.getProperty("java.version") + "\n" +
               "OS: " + System.getProperty("os.name");
    }
}
