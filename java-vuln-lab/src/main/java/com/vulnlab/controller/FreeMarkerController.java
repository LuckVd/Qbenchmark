package com.vulnlab.controller;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * FreeMarker SSTI (Server-Side Template Injection) 漏洞演示
 *
 * 漏洞说明：
 * FreeMarker 模板引擎直接处理用户输入的模板内容，
 * 攻击者可以通过 FreeMarker 语法执行任意代码或获取敏感信息
 *
 * 修复方案：
 * 1. 使用预定义的模板，不接受用户输入的模板
 * 2. 禁用危险的内置函数和 API
 * 3. 对用户输入进行严格的过滤和验证
 *
 * @author VulnLab
 */
@RestController
@RequestMapping("/ssti/freemarker")
public class FreeMarkerController {

    private static final Logger logger = LoggerFactory.getLogger(FreeMarkerController.class);

    /**
     * FreeMarker SSTI 漏洞
     *
     * 漏洞原理：直接使用 FreeMarker 处理用户输入的模板字符串
     * 攻击向量：通过 FreeMarker 语法访问 API 和执行操作
     *
     * 测试 Payload (GET):
     * - 信息泄露: "test"?api (可能被 Spring 拦截，建议用 POST)
     * - 基本渲染: hello
     * - 变量: name
     *
     * 测试 Payload (POST):
     * - API 访问: ${"test"?api}
     * - ClassLoader: ${"test"?api.class.getClassLoader()}
     * - 高级利用: <#assign classloader=object?api.class.getClassLoader()>
     *
     * 命令示例 (GET):
     * curl "http://localhost:8080/ssti/freemarker/vuln?template=hello"
     *
     * 命令示例 (POST):
     * curl -X POST "http://localhost:8080/ssti/freemarker/vuln" \
     *   -H "Content-Type: text/plain" \
     *   -d '${"test"?api}'
     *
     * @param template FreeMarker 模板
     * @return 渲染结果
     */
    @GetMapping("/vuln")
    public String vuln(@RequestParam("template") String template) {
        logger.info("FreeMarker SSTI vuln called with template: {}", template);

        try {
            // 创建 FreeMarker 配置
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);

            // 漏洞代码：使用不安全的配置
            cfg.setNumberFormat("0.######");
            cfg.setClassicCompatible(true);  // 允许访问更多 API
            cfg.setAPIBuiltinEnabled(true);  // 启用 ?api 内置函数

            // 创建模板
            freemarker.template.Template tpl = new Template(
                "vulnTemplate",
                new StringReader(template),
                cfg
            );

            // 创建数据模型
            Map<String, Object> dataModel = new HashMap<>();
            // 添加 object 变量用于 ?api 访问
            dataModel.put("object", "test");

            // 漏洞代码：直接渲染用户输入的模板字符串
            StringWriter writer = new StringWriter();
            tpl.process(dataModel, writer);

            String result = writer.toString();
            logger.info("FreeMarker render result: {}", result);
            return "FreeMarker template executed. Result: " + result;
        } catch (Exception e) {
            logger.error("FreeMarker render error", e);
            return "FreeMarker render error: " + e.getMessage();
        }
    }

    /**
     * FreeMarker SSTI 漏洞 (POST) - 绕过 Spring URL 参数拦截
     *
     * 使用 POST 请求可以发送包含特殊字符（如 ${}、?api 等）的模板
     *
     * 测试 Payload:
     * - API 信息泄露: ${"test"?api}
     * - ClassLoader: ${"test"?api.class.getClassLoader()}
     * - 变量: ${name!"guest"}
     * - 条件: <#if true>true</#if>
     * - 高级利用: <#assign ex=classloader.loadClass("java.lang.Runtime")>
     *
     * 命令示例:
     * curl -X POST "http://localhost:8080/ssti/freemarker/vuln" \
     *   -H "Content-Type: text/plain" \
     *   -d '${"test"?api}'
     *
     * @param template FreeMarker 模板 (通过 RequestBody)
     * @return 渲染结果
     */
    @PostMapping(value = "/vuln", consumes = "text/plain")
    public String vulnPost(@RequestBody String template) {
        logger.info("FreeMarker SSTI vuln POST called with template: {}", template);

        try {
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);

            // 漏洞代码：使用不安全的配置
            cfg.setNumberFormat("0.######");
            cfg.setClassicCompatible(true);
            cfg.setAPIBuiltinEnabled(true);  // 关键：启用 ?api 内置函数

            freemarker.template.Template tpl = new Template(
                "vulnTemplatePost",
                new StringReader(template),
                cfg
            );

            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("object", "test");

            StringWriter writer = new StringWriter();
            tpl.process(dataModel, writer);

            String result = writer.toString();
            logger.info("FreeMarker POST render result: {}", result);
            return "FreeMarker template executed. Result: " + result;
        } catch (Exception e) {
            logger.error("FreeMarker POST render error", e);
            return "FreeMarker render error: " + e.getMessage();
        }
    }

    /**
     * FreeMarker SSTI - 带上下文变量版本 (GET)
     *
     * 漏洞原理：提供上下文变量，但仍然允许执行任意模板
     *
     * 测试 Payload:
     * - 使用变量: Hello name! (不含特殊字符)
     *
     * 命令示例:
     * curl "http://localhost:8080/ssti/freemarker/vuln2?template=Hello&name=admin"
     *
     * 注意: 复杂模板请使用 POST 方法
     *
     * @param template FreeMarker 模板
     * @param name 上下文变量
     * @return 渲染结果
     */
    @GetMapping("/vuln2")
    public String vuln2(@RequestParam("template") String template,
                        @RequestParam(value = "name", defaultValue = "guest") String name) {
        logger.info("FreeMarker SSTI vuln2 called with template: {}, name: {}", template, name);

        try {
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
            cfg.setNumberFormat("0.######");
            cfg.setClassicCompatible(true);
            cfg.setAPIBuiltinEnabled(true);

            freemarker.template.Template tpl = new Template(
                "vulnTemplate2",
                new StringReader(template),
                cfg
            );

            // 创建数据模型并添加变量
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("name", name);

            // 漏洞代码：虽然提供了上下文，但未限制模板语法
            StringWriter writer = new StringWriter();
            tpl.process(dataModel, writer);

            String result = writer.toString();
            logger.info("FreeMarker vuln2 result: {}", result);
            return "FreeMarker template executed. Result: " + result;
        } catch (Exception e) {
            logger.error("FreeMarker vuln2 render error", e);
            return "FreeMarker render error: " + e.getMessage();
        }
    }

    /**
     * FreeMarker SSTI - 带上下文变量版本 (POST)
     *
     * 使用 POST 请求绕过 Spring URL 拦截，可以发送复杂模板
     *
     * 测试 Payload:
     * - 使用变量: Hello ${name!"guest"}!
     * - 条件: <#if name=="admin">Admin<#else>Guest</#if>
     * - API 访问: ${name?api}
     * - 内置函数: ${name?upper_case}
     *
     * 命令示例:
     * curl -X POST "http://localhost:8080/ssti/freemarker/vuln2?name=admin" \
     *   -H "Content-Type: text/plain" \
     *   -d 'Hello ${name}!'
     *
     * @param template FreeMarker 模板
     * @param name 上下文变量
     * @return 渲染结果
     */
    @PostMapping(value = "/vuln2", consumes = "text/plain")
    public String vuln2Post(@RequestBody String template,
                            @RequestParam(value = "name", defaultValue = "guest") String name) {
        logger.info("FreeMarker SSTI vuln2 POST called with template: {}, name: {}", template, name);

        try {
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
            cfg.setNumberFormat("0.######");
            cfg.setClassicCompatible(true);
            cfg.setAPIBuiltinEnabled(true);

            freemarker.template.Template tpl = new Template(
                "vulnTemplate2Post",
                new StringReader(template),
                cfg
            );

            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("name", name);

            StringWriter writer = new StringWriter();
            tpl.process(dataModel, writer);

            String result = writer.toString();
            logger.info("FreeMarker vuln2 POST result: {}", result);
            return "FreeMarker template executed. Result: " + result;
        } catch (Exception e) {
            logger.error("FreeMarker vuln2 POST render error", e);
            return "FreeMarker render error: " + e.getMessage();
        }
    }
}
