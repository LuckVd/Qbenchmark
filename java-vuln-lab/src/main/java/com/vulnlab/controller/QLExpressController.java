package com.vulnlab.controller;

import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * QLExpress 表达式注入漏洞演示
 *
 * 漏洞说明：
 * 阿里巴巴 QLExpress 表达式引擎未启用沙箱时可执行任意代码
 *
 * 修复方案：
 * 启用沙箱模式，限制可访问的类
 *
 * @author VulnLab
 */
@RestController
@RequestMapping("/qlexpress")
public class QLExpressController {

    private static final Logger logger = LoggerFactory.getLogger(QLExpressController.class);

    /**
     * QLExpress 表达式注入漏洞
     *
     * 漏洞原理：未启用沙箱模式，允许执行任意 Java 代码
     * 攻击向量：直接执行 Java 代码片段
     *
     * 测试 Payload:
     * - RCE: Runtime.getRuntime().exec("whoami")
     * - 信息泄露: System.getProperty("user.name")
     * - 计算表达式: 1+1*2
     * - 调用方法: "hello".toUpperCase()
     *
     * 命令示例:
     * curl "http://localhost:8080/qlexpress/vuln?expression=Runtime.getRuntime().exec(%22whoami%22)"
     *
     * @param expression QLExpress 表达式
     * @return 执行结果
     */
    @GetMapping("/vuln")
    public String vuln(@RequestParam("expression") String expression) {
        logger.info("QLExpress vuln called with expression: {}", expression);

        try {
            // 创建 QLExpress 引擎
            ExpressRunner runner = new ExpressRunner();

            // 漏洞代码：未启用沙箱模式，直接执行用户输入的表达式
            Object result = runner.execute(expression, null, null, true, false);

            logger.info("QLExpress vuln result: {}", result);
            return "QLExpress executed. Result: " + result;
        } catch (Exception e) {
            logger.error("QLExpress vuln error", e);
            return "QLExpress error: " + e.getMessage();
        }
    }

    /**
     * QLExpress 表达式注入 - 带上下文版本
     *
     * 漏洞原理：提供自定义上下文，但仍允许访问危险类
     *
     * 测试 Payload:
     * - RCE: Runtime.getRuntime().exec("whoami")
     * - 访问上下文变量: name.toUpperCase()
     *
     * 命令示例:
     * curl "http://localhost:8080/qlexpress/vuln2?expression=name.toUpperCase()&name=admin"
     *
     * @param expression QLExpress 表达式
     * @param name 上下文变量
     * @return 执行结果
     */
    @GetMapping("/vuln2")
    public String vuln2(@RequestParam("expression") String expression,
                        @RequestParam(value = "name", defaultValue = "guest") String name) {
        logger.info("QLExpress vuln2 called with expression: {}, name: {}", expression, name);

        try {
            ExpressRunner runner = new ExpressRunner();

            // 创建上下文并添加变量
            DefaultContext<String, Object> context = new DefaultContext<>();
            context.put("name", name);

            // 漏洞代码：虽然提供了上下文，但未限制沙箱
            Object result = runner.execute(expression, context, null, true, false);

            logger.info("QLExpress vuln2 result: {}", result);
            return "QLExpress executed. Result: " + result;
        } catch (Exception e) {
            logger.error("QLExpress vuln2 error", e);
            return "QLExpress error: " + e.getMessage();
        }
    }
}
