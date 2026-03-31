package com.vulnlab.controller;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DoS (Denial of Service) 拒绝服务漏洞演示
 *
 * 漏洞说明：
 * - DoS 攻击通过耗尽系统资源使服务不可用
 * - 常见类型: ReDoS、内存耗尽、CPU 耗尽
 * - 危险等级: 中危 (Medium)
 *
 * 漏洞原理：
 * 1. ReDoS (Regular Expression DoS): 恶意正则导致回溯爆炸
 * 2. Memory DoS: 分配过多内存导致 OOM
 * 3. CPU DoS: 复杂计算或死循环导致 CPU 100%
 *
 * ReDoS 原理详解：
 * - 正则引擎使用回溯算法匹配
 * - 某些正则模式在特定输入下会产生指数级回溯
 * - 例如: ^(a+)+$ 匹配 aaaaaaaaaaaaa...! 时
 * - 引擎会尝试所有可能的分组组合，导致 CPU 耗尽
 *
 * 危险正则模式：
 * - 包含重复的复杂嵌套: (a+)+, (a*)*, (a|a*)+
 * - 包含重叠的字符类: [a-z]+.*[a-z]+
 * - 包含多个量词嵌套: (a+)*, (a*)+
 *
 * 修复方案：
 * 1. 使用非回溯正则引擎 (如 RE2)
 * 2. 避免嵌套量词和重复分组
 * 3. 限制正则匹配时间 (超时机制)
 * 4. 限制输入长度
 * 5. 使用原子组 (?>...) 或占有量词 (?+)
 *
 * @author VulnLab
 */
@RestController
@RequestMapping("/dos")
public class DosController {

    // ==================== ReDoS 漏洞 ====================

    /**
     * ReDoS 漏洞 - 经典的回溯攻击正则
     *
     * 正则: ^(a+)+$
     * 恶意输入: aaaaaa...! (30个a后面跟!)
     *
     * 漏洞原理：
     * - (a+)+ 会创建多个分组
     * - 引擎需要尝试所有可能的分组方式
     * - 复杂度呈指数级增长: O(2^n)
     *
     * @param input 用户输入
     * @return 匹配结果
     */
    @GetMapping("/regex")
    public String regexVuln(@RequestParam(value = "input", defaultValue = "") String input) {
        // 漏洞代码：包含嵌套量词的危险正则
        Pattern pattern = Pattern.compile("^(a+)+$");
        long startTime = System.currentTimeMillis();

        try {
            Matcher matcher = pattern.matcher(input);
            boolean matches = matcher.matches();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            return String.format(
                "=== ReDoS 漏洞测试 ===\n" +
                "正则表达式: ^(a+)+$\n" +
                "输入: %s\n" +
                "匹配结果: %s\n" +
                "耗时: %d ms\n" +
                "\n[!] 如果耗时超过3秒，存在 ReDoS 漏洞",
                input.isEmpty() ? "(空)" : input,
                matches,
                duration
            );
        } catch (Exception e) {
            return "匹配出错: " + e.getMessage();
        }
    }

    /**
     * ReDoS 漏洞 - Email 验证正则
     *
     * 常见的 Email 正则存在 ReDoS 问题
     * 恶意输入: aaaaaaaaaaaaaaaaaaaa@b....................
     *
     * @param email Email 地址
     * @return 验证结果
     */
    @GetMapping("/regex/email")
    public String emailRegexVuln(@RequestParam(value = "email", defaultValue = "") String email) {
        // 漏洞代码：危险的 Email 验证正则
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        // 更危险的版本: ^([a-zA-Z0-9._%+-]+)*@([a-zA-Z0-9.-]+)*$

        long startTime = System.currentTimeMillis();

        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(email);
            boolean valid = matcher.matches();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            return String.format(
                "=== Email 验证 ReDoS ===\n" +
                "Email: %s\n" +
                "验证结果: %s\n" +
                "耗时: %d ms",
                email.isEmpty() ? "(空)" : email,
                valid ? "有效" : "无效",
                duration
            );
        } catch (Exception e) {
            return "验证出错: " + e.getMessage();
        }
    }

    /**
     * ReDoS 漏洞 - URL 验证正则
     *
     * 恶意输入: aaaaaaaaaaaaaaaaaaaaa.com/a...............
     *
     * @param url URL 地址
     * @return 验证结果
     */
    @GetMapping("/regex/url")
    public String urlRegexVuln(@RequestParam(value = "url", defaultValue = "") String url) {
        // 漏洞代码：危险的 URL 验证正则
        String regex = "^(https?://)?([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}(/.*)?$";

        long startTime = System.currentTimeMillis();

        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(url);
            boolean valid = matcher.matches();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            return String.format(
                "=== URL 验证 ReDoS ===\n" +
                "URL: %s\n" +
                "验证结果: %s\n" +
                "耗时: %d ms",
                url.isEmpty() ? "(空)" : url,
                valid ? "有效" : "无效",
                duration
            );
        } catch (Exception e) {
            return "验证出错: " + e.getMessage();
        }
    }

    /**
     * ReDoS 漏洞 - 多个重叠字符类
     *
     * 正则: ^([a-z]+.*)+$
     * 恶意输入: aaaaaaaaaaaaaaaa....................!
     *
     * @param input 用户输入
     * @return 匹配结果
     */
    @GetMapping("/regex/overlap")
    public String overlapRegexVuln(@RequestParam(value = "input", defaultValue = "") String input) {
        // 漏洞代码：包含重叠字符类的正则
        Pattern pattern = Pattern.compile("^([a-z]+.*)+$");

        long startTime = System.currentTimeMillis();

        try {
            Matcher matcher = pattern.matcher(input);
            boolean matches = matcher.matches();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            return String.format(
                "=== 重叠字符类 ReDoS ===\n" +
                "正则: ^([a-z]+.*)+$\n" +
                "输入: %s\n" +
                "匹配结果: %s\n" +
                "耗时: %d ms",
                input.isEmpty() ? "(空)" : input,
                matches,
                duration
            );
        } catch (Exception e) {
            return "匹配出错: " + e.getMessage();
        }
    }

    /**
     * ReDoS 漏洞 - 交替分组
     *
     * 正则: ^(a|a*)+$
     * 恶意输入: aaaaaaaaaaaaaaaaaaaaaaa!
     *
     * @param input 用户输入
     * @return 匹配结果
     */
    @GetMapping("/regex/alt")
    public String alternateRegexVuln(@RequestParam(value = "input", defaultValue = "") String input) {
        // 漏洞代码：包含交替的空匹配
        Pattern pattern = Pattern.compile("^(a|a*)+$");

        long startTime = System.currentTimeMillis();

        try {
            Matcher matcher = pattern.matcher(input);
            boolean matches = matcher.matches();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            return String.format(
                "=== 交替分组 ReDoS ===\n" +
                "正则: ^(a|a*)+$\n" +
                "输入: %s\n" +
                "匹配结果: %s\n" +
                "耗时: %d ms",
                input.isEmpty() ? "(空)" : input,
                matches,
                duration
            );
        } catch (Exception e) {
            return "匹配出错: " + e.getMessage();
        }
    }

    // ==================== Memory DoS 漏洞 ====================

    /**
     * Memory DoS - 大数组分配
     *
     * 漏洞代码：根据用户输入分配内存
     *
     * @param size 数组大小
     * @return 操作结果
     */
    @GetMapping("/memory/array")
    public String memoryArray(@RequestParam(value = "size", defaultValue = "1000") int size) {
        // 限制最大大小防止真的崩溃服务
        int maxSize = 1000000;
        if (size > maxSize) {
            size = maxSize;
        }

        long startTime = System.currentTimeMillis();
        long startMem = Runtime.getRuntime().freeMemory();

        try {
            // 漏洞代码：根据用户输入分配大数组
            List<byte[]> arrays = new ArrayList<>();
            for (int i = 0; i < size / 1000; i++) {
                arrays.add(new byte[1024 * 100]); // 100KB
            }

            long endTime = System.currentTimeMillis();
            long endMem = Runtime.getRuntime().freeMemory();
            long duration = endTime - startTime;
            long memUsed = startMem - endMem;

            return String.format(
                "=== Memory DoS ===\n" +
                "分配数组: %d\n" +
                "实际大小: %d\n" +
                "内存使用: %d bytes\n" +
                "耗时: %d ms\n" +
                "\n[!] 恶意用户可以通过大尺寸值耗尽内存",
                size,
                arrays.size(),
                memUsed,
                duration
            );
        } catch (OutOfMemoryError e) {
            return "内存耗尽: " + e.getMessage();
        }
    }

    /**
     * Memory DoS - 字符串重复
     *
     * 漏洞代码：根据用户输入创建大字符串
     *
     * @param str 基础字符串
     * @param times 重复次数
     * @return 操作结果
     */
    @GetMapping("/memory/string")
    public String memoryString(@RequestParam(value = "str", defaultValue = "a") String str,
                               @RequestParam(value = "times", defaultValue = "1000") int times) {
        // 限制最大重复次数
        int maxTimes = 100000;
        if (times > maxTimes) {
            times = maxTimes;
        }

        long startTime = System.currentTimeMillis();

        try {
            // 漏洞代码：根据用户输入创建大字符串
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < times; i++) {
                sb.append(str);
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            return String.format(
                "=== String Memory DoS ===\n" +
                "基础字符串: %s\n" +
                "重复次数: %d\n" +
                "结果长度: %d\n" +
                "耗时: %d ms\n" +
                "\n[!] 恶意用户可以创建超大字符串耗尽内存",
                str,
                times,
                sb.length(),
                duration
            );
        } catch (OutOfMemoryError e) {
            return "内存耗尽: " + e.getMessage();
        }
    }

    // ==================== CPU DoS 漏洞 ====================

    /**
     * CPU DoS - 复杂计算
     *
     * 漏洞代码：根据用户输入进行复杂计算
     *
     * @param n 斐波那契数列项数
     * @return 计算结果
     */
    @GetMapping("/cpu/fib")
    public String cpuFibonacci(@RequestParam(value = "n", defaultValue = "30") int n) {
        // 限制最大值
        int maxN = 45;
        if (n > maxN) {
            n = maxN;
        }

        long startTime = System.currentTimeMillis();

        // 漏洞代码：递归计算斐波那契数列（无缓存）
        long result = fibonacci(n);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        return String.format(
            "=== CPU DoS - 斐波那契 ===\n" +
            "计算项数: %d\n" +
            "结果: %d\n" +
            "耗时: %d ms\n" +
            "\n[!] 复杂度 O(2^n)，大数值会导致 CPU 耗尽",
            n,
            result,
            duration
        );
    }

    /**
     * CPU DoS - 嵌套循环
     *
     * 漏洞代码：根据用户输入进行嵌套循环
     *
     * @param n 循环次数
     * @return 计算结果
     */
    @GetMapping("/cpu/loop")
    public String cpuLoop(@RequestParam(value = "n", defaultValue = "100") int n) {
        // 限制最大值
        int maxN = 1000;
        if (n > maxN) {
            n = maxN;
        }

        long startTime = System.currentTimeMillis();

        // 漏洞代码：O(n^3) 嵌套循环
        long count = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    count++;
                }
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        return String.format(
            "=== CPU DoS - 嵌套循环 ===\n" +
            "循环次数: %d\n" +
            "计算结果: %d\n" +
            "耗时: %d ms\n" +
            "\n[!] 复杂度 O(n^3)，大数值会导致 CPU 耗尽",
            n,
            count,
            duration
        );
    }

    // ==================== 安全版本 ====================

    /**
     * 安全版本 - 优化的正则匹配
     *
     * @param input 用户输入
     * @return 匹配结果
     */
    @GetMapping("/safe/regex")
    public String safeRegex(@RequestParam(value = "input", defaultValue = "") String input) {
        // 安全代码：使用非回溯正则或限制输入长度

        // 限制输入长度
        if (input.length() > 100) {
            return "输入过长: 最大100字符";
        }

        // 使用简化正则（避免嵌套量词）
        Pattern pattern = Pattern.compile("^a+$");

        long startTime = System.currentTimeMillis();

        try {
            Matcher matcher = pattern.matcher(input);
            boolean matches = matcher.matches();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            return String.format(
                "=== 安全版本 ===\n" +
                "输入: %s\n" +
                "匹配结果: %s\n" +
                "耗时: %d ms\n" +
                "\n[✓] 输入长度受限，正则已优化",
                input.isEmpty() ? "(空)" : input,
                matches,
                duration
            );
        } catch (Exception e) {
            return "匹配出错: " + e.getMessage();
        }
    }

    /**
     * 安全版本 - 带缓存的斐波那契
     *
     * @param n 斐波那契项数
     * @return 计算结果
     */
    @GetMapping("/safe/cpu/fib")
    public String safeCpuFib(@RequestParam(value = "n", defaultValue = "30") int n) {
        // 安全代码：限制输入大小，使用迭代而非递归

        if (n > 45) {
            return "输入过大: 最大45";
        }

        long startTime = System.currentTimeMillis();

        // 使用迭代算法（O(n) 而非 O(2^n)）
        long result = fibonacciIterative(n);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        return String.format(
            "=== 安全版本 ===\n" +
            "计算项数: %d\n" +
            "结果: %d\n" +
            "耗时: %d ms\n" +
            "\n[✓] 使用迭代算法，复杂度 O(n)",
            n,
            result,
            duration
        );
    }

    /**
     * 信息端点
     *
     * @return 测试信息
     */
    @GetMapping("/info")
    public String info() {
        return String.format(
            "DoS (Denial of Service) 漏洞演示%n" +
            "=====================================%n" +
            "Java Version: %s%n" +
            "OS: %s %s%n" +
            "%n" +
            "ReDoS 漏洞端点:%n" +
            "- GET /dos/regex?input=aaa...! (经典回溯)%n" +
            "- GET /dos/regex/email?email=aaaaa@b....%n" +
            "- GET /dos/regex/url?url=aaaa.com/....%n" +
            "- GET /dos/regex/overlap?input=aaa...!%n" +
            "- GET /dos/regex/alt?input=aaa...!%n" +
            "%n" +
            "Memory DoS 端点:%n" +
            "- GET /dos/memory/array?size=10000%n" +
            "- GET /dos/memory/string?str=a&times=1000%n" +
            "%n" +
            "CPU DoS 端点:%n" +
            "- GET /dos/cpu/fib?n=40%n" +
            "- GET /dos/cpu/loop?n=100%n" +
            "%n" +
            "安全端点:%n" +
            "- GET /dos/safe/regex?input=aaa%n" +
            "- GET /dos/safe/cpu/fib?n=40%n" +
            "%n" +
            "警告: DoS 测试可能导致服务暂时不可用！",
            System.getProperty("java.version"),
            System.getProperty("os.name"),
            System.getProperty("os.version")
        );
    }

    // ==================== 辅助方法 ====================

    /**
     * 递归计算斐波那契数列（易受攻击）
     * 复杂度: O(2^n)
     */
    private long fibonacci(int n) {
        if (n <= 1) {
            return n;
        }
        return fibonacci(n - 1) + fibonacci(n - 2);
    }

    /**
     * 迭代计算斐波那契数列（安全）
     * 复杂度: O(n)
     */
    private long fibonacciIterative(int n) {
        if (n <= 1) {
            return n;
        }
        long a = 0, b = 1;
        for (int i = 2; i <= n; i++) {
            long temp = a + b;
            a = b;
            b = temp;
        }
        return b;
    }
}
