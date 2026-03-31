package com.vulnlab.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.web.bind.annotation.*;

/**
 * SpEL (Spring Expression Language) 表达式注入漏洞演示
 *
 * 漏洞说明：
 * 使用 StandardEvaluationContext 允许访问任意类，可实现 RCE
 *
 * 修复方案：
 * 使用 SimpleEvaluationContext 替代 StandardEvaluationContext
 *
 * @author VulnLab
 */
@RestController
@RequestMapping("/spel")
public class SpelController {

    private static final Logger logger = LoggerFactory.getLogger(SpelController.class);

    /**
     * SpEL 表达式注入漏洞 - 直接解析
     *
     * 漏洞原理：使用 StandardEvaluationContext 解析用户输入的 SpEL 表达式
     * 攻击向量：通过 T() 调用静态方法，通过 new 实例化对象
     *
     * 测试 Payload:
     * - RCE: T(java.lang.Runtime).getRuntime().exec('whoami')
     * - 文件读取: new java.io.BufferedReader(new java.io.FileReader('/etc/passwd')).readLine()
     * - 文件检测: new java.io.File('/etc/passwd').exists()
     * - 系统属性: T(java.lang.System).getProperty('user.name')
     * - 计算表达式: 1+1*2
     *
     * 命令示例:
     * curl "http://localhost:8080/spel/vuln1?expression=T(java.lang.Runtime).getRuntime().exec('whoami')"
     *
     * @param expression SpEL 表达式
     * @return 执行结果
     */
    @GetMapping("/vuln1")
    public String vuln1(@RequestParam("expression") String expression) {
        logger.info("SpEL vuln1 called with expression: {}", expression);

        try {
            // 创建 SpEL 解析器
            ExpressionParser parser = new SpelExpressionParser();

            // 漏洞代码：使用 StandardEvaluationContext，允许访问任意类
            StandardEvaluationContext context = new StandardEvaluationContext();

            // 解析并执行表达式
            Expression exp = parser.parseExpression(expression);
            Object result = exp.getValue(context);

            logger.info("SpEL vuln1 result: {}", result);
            return "SpEL expression executed. Result: " + result;
        } catch (Exception e) {
            logger.error("SpEL vuln1 error", e);
            return "SpEL expression error: " + e.getMessage();
        }
    }

    /**
     * SpEL 表达式注入漏洞 - 模板解析
     *
     * 漏洞原理：解析包含 SpEL 表达式的模板字符串
     * 攻击向量：在模板中嵌入恶意表达式
     *
     * 测试 Payload:
     * - RCE: #{T(java.lang.Runtime).getRuntime().exec('whoami')}
     * - 信息泄露: #{T(java.lang.System).getProperty('os.name')}
     * - 计算表达式: #{1+1*2}
     *
     * 命令示例:
     * curl "http://localhost:8080/spel/vuln2?expression=#{T(java.lang.Runtime).getRuntime().exec('whoami')}"
     *
     * @param expression 包含 SpEL 的表达式
     * @return 执行结果
     */
    @GetMapping("/vuln2")
    public String vuln2(@RequestParam("expression") String expression) {
        logger.info("SpEL vuln2 called with expression: {}", expression);

        try {
            ExpressionParser parser = new SpelExpressionParser();

            // 漏洞代码：直接解析用户提供的表达式，未做任何限制
            Expression exp = parser.parseExpression(expression);
            Object result = exp.getValue();

            logger.info("SpEL vuln2 result: {}", result);
            return "SpEL template executed. Result: " + result;
        } catch (Exception e) {
            logger.error("SpEL vuln2 error", e);
            return "SpEL expression error: " + e.getMessage();
        }
    }
}
