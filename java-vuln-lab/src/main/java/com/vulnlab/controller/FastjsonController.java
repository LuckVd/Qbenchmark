package com.vulnlab.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * Fastjson 反序列化漏洞演示
 *
 * 漏洞说明：
 * - Fastjson autoType 允许反序列化任意类
 * - 使用 @type 指定恶意类进行 RCE
 *
 * 修复方案：
 * - 禁用 autoType 或使用 SafeMode
 * - 升级到安全版本
 * - 使用白名单机制
 *
 * @author VulnLab
 */
@RestController
@RequestMapping("/fastjson")
public class FastjsonController {

    private static final Logger logger = LoggerFactory.getLogger(FastjsonController.class);

    /**
     * Fastjson 反序列化漏洞
     *
     * 漏洞原理：Fastjson 1.2.24 版本默认开启 autoType，允许反序列化任意类
     * 攻击向量：使用 @type 指定 TemplatesImpl 类加载恶意字节码
     *
     * 测试 Payload (TemplatesImpl):
     * {
     *   "@type": "com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl",
     *   "_bytecodes": ["base64编码的字节码"],
     *   "_name": "lightless",
     *   "_tfactory": {},
     *   "_outputProperties": {}
     * }
     *
     * 命令示例:
     * curl -X POST http://localhost:8080/fastjson/deserialize \
     *   -H "Content-Type: application/json" \
     *   -d '{"@type":"java.lang.Runtime","cmd":"calc.exe"}'
     *
     * @param params JSON payload
     * @return 反序列化结果
     */
    @PostMapping("/deserialize")
    public String deserialize(@RequestBody String params) {
        logger.info("Fastjson deserialize request: {}", params);

        try {
            // 漏洞代码：直接解析 JSON，autoType 默认开启
            JSONObject obj = JSON.parseObject(params, Feature.SupportNonPublicField);
            logger.info("Fastjson deserialize result: {}", obj);

            if (obj != null && obj.containsKey("name")) {
                return "Fastjson deserialization completed. Name: " + obj.get("name");
            }
            return "Fastjson deserialization completed. Object: " + obj.getClass().getName();
        } catch (Exception e) {
            logger.error("Fastjson deserialization error", e);
            return "Fastjson deserialization error: " + e.getMessage();
        }
    }

    /**
     * Fastjson 反序列化漏洞 - 简化版端点
     *
     * 用于快速测试的端点，返回解析结果
     *
     * @param params JSON payload
     * @return 解析结果
     */
    @PostMapping("/vuln")
    public String vuln(@RequestBody String params) {
        logger.info("Fastjson vuln request: {}", params);

        try {
            Object obj = JSON.parse(params);
            logger.info("Fastjson parse result: {}", obj);
            return "Parsed object: " + obj.getClass().getName() + " -> " + obj.toString();
        } catch (Exception e) {
            logger.error("Fastjson parse error", e);
            return "Parse error: " + e.getMessage();
        }
    }

    /**
     * Fastjson 反序列化漏洞 - 带返回值的版本
     *
     * 这个端点与 java-sec-code 的行为保持一致
     *
     * @param params JSON payload
     * @return 解析后的字段值
     */
    @PostMapping("/deserialize/return")
    @ResponseBody
    public String deserializeWithReturn(@RequestBody String params) {
        logger.info("Fastjson deserialize request: {}", params);

        try {
            JSONObject ob = JSON.parseObject(params);
            if (ob != null && ob.containsKey("name")) {
                return ob.get("name").toString();
            }
            return "Fastjson deserialization completed";
        } catch (Exception e) {
            logger.error("Fastjson deserialization error", e);
            return e.toString();
        }
    }
}
