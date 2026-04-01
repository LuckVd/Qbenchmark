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

@RestController
@RequestMapping("/api/v1/xml")
public class XmlDataController {

    private static final Logger logger = LoggerFactory.getLogger(XmlDataController.class);

    @PostMapping("/parse/reader")
    public String parseReader(@RequestBody String xml) {
        logger.info("XML Reader request received");

        try {
            XMLReader reader = XMLReaderFactory.createXMLReader();
            reader.parse(new InputSource(new StringReader(xml)));
            return "XML parsed successfully. Content length: " + xml.length();
        } catch (Exception e) {
            logger.error("XML Reader parsing error", e);
            return "XML parsing error: " + e.getMessage();
        }
    }

    @PostMapping("/parse/builder")
    public String parseBuilder(@RequestBody String xml) {
        logger.info("XML Builder request received");

        try {
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(new StringReader(xml));
            String content = doc.getRootElement().getText();
            return "XML parsed successfully. Root content: " + content;
        } catch (Exception e) {
            logger.error("XML Builder parsing error", e);
            return "XML parsing error: " + e.getMessage();
        }
    }

    @PostMapping("/parse/doc")
    public String parseDoc(@RequestBody String xml) {
        logger.info("XML Doc request received");

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(new InputSource(new StringReader(xml)));
            String content = doc.getDocumentElement().getTextContent();
            return "XML parsed successfully. Root content: " + content;
        } catch (Exception e) {
            logger.error("XML Doc parsing error", e);
            return "XML parsing error: " + e.getMessage();
        }
    }

    @GetMapping("/info")
    public String info() {
        StringBuilder info = new StringBuilder();
        info.append("XML Data Processing\n");
        info.append("==========================================\n");
        info.append("\n");
        info.append("Supported Parsers:\n");
        info.append("1. XMLReader\n");
        info.append("2. SAXBuilder (JDOM2)\n");
        info.append("3. DocumentBuilder\n");
        return info.toString();
    }
}
