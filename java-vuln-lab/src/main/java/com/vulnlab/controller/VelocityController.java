package com.vulnlab.controller;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.StringWriter;

/**
 * Velocity SSTI (Server-Side Template Injection) 漏洞演示
 *
 * 漏洞说明：
 * Apache Velocity 模板引擎直接渲染用户输入的模板内容，
 * 攻击者可以通过 Velocity 语法执行任意代码或获取敏感信息
 *
 * 修复方案：
 * 1. 使用预定义的模板，不接受用户输入的模板
 * 2. 启用沙箱模式，限制可访问的类和方法
 * 3. 对用户输入进行严格的过滤和验证
 *
 * @author VulnLab
 */
@RestController
@RequestMapping("/ssti/velocity")
public class VelocityController {

    private static final Logger logger = LoggerFactory.getLogger(VelocityController.class);

    /**
     * Velocity SSTI 漏洞
     *
     * 漏洞原理：直接使用 Velocity.evaluate() 渲染用户输入的模板字符串
     * 攻击向量：通过 Velocity 语法访问 Java 类和方法
     *
     * 测试 Payload:
     * - 信息泄露: $math
     * - 信息泄露: $scope
     * - 信息泄露: $class
     * - 基本渲染: ${"hello"}
     * - 变量操作: #set($x=1)$x
     * - RCE (需要构造复杂的 gadget chain)
     *
     * 命令示例:
     * curl "http://localhost:8080/ssti/velocity/vuln?template=\$math"
     *
     * @param template Velocity 模板
     * @return 渲染结果
     */
    @GetMapping("/vuln")
    public String vuln(@RequestParam("template") String template) {
        logger.info("Velocity SSTI vuln called with template: {}", template);

        try {
            // 创建 Velocity 引擎
            VelocityEngine engine = new VelocityEngine();

            // 漏洞代码：使用默认配置，不限制模板内容
            engine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
            engine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class);
            engine.init();

            // 创建上下文
            VelocityContext context = new VelocityContext();

            // 漏洞代码：直接渲染用户输入的模板字符串
            StringWriter writer = new StringWriter();
            engine.evaluate(context, writer, "vulnTemplate", template);

            String result = writer.toString();
            logger.info("Velocity render result: {}", result);
            return "Velocity template executed. Result: " + result;
        } catch (Exception e) {
            logger.error("Velocity render error", e);
            return "Velocity render error: " + e.getMessage();
        }
    }

    /**
     * Velocity SSTI - 带上下文变量版本
     *
     * 漏洞原理：提供一些上下文变量，但仍然允许执行任意模板
     *
     * 测试 Payload:
     * - 使用变量: Hello $name!
     * - 数学计算: #set($x=1+1)$x
     * - 循环: #foreach($i in [1..3])$i#end
     * - 访问工具: $math.PI
     *
     * 命令示例:
     * curl "http://localhost:8080/ssti/velocity/vuln2?template=Hello+\$name!&name=admin"
     *
     * @param template Velocity 模板
     * @param name 上下文变量
     * @return 渲染结果
     */
    @GetMapping("/vuln2")
    public String vuln2(@RequestParam("template") String template,
                        @RequestParam(value = "name", defaultValue = "guest") String name) {
        logger.info("Velocity SSTI vuln2 called with template: {}, name: {}", template, name);

        try {
            VelocityEngine engine = new VelocityEngine();
            engine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
            engine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class);
            engine.init();

            // 创建上下文并添加变量
            VelocityContext context = new VelocityContext();
            context.put("name", name);

            // 漏洞代码：虽然提供了上下文，但未限制模板语法
            StringWriter writer = new StringWriter();
            engine.evaluate(context, writer, "vulnTemplate2", template);

            String result = writer.toString();
            logger.info("Velocity vuln2 result: {}", result);
            return "Velocity template executed. Result: " + result;
        } catch (Exception e) {
            logger.error("Velocity vuln2 render error", e);
            return "Velocity render error: " + e.getMessage();
        }
    }
}
