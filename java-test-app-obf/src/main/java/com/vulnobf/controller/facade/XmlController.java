package com.vulnobf.controller.facade;

import com.vulnobf.controller.handler.ExpressionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * XML Controller - XXE endpoints
 */
@RestController
@RequestMapping("/api/v1")
public class XmlController {

    private static final Logger logger = LoggerFactory.getLogger(XmlController.class);

    @Autowired
    private ExpressionHandler expressionHandler;

    /**
     * Parse XML - XXE via XMLReader
     * Path: /api/v1/xml/parse
     * Original: /xxe/xmlReader/vuln
     */
    @PostMapping("/xml/parse")
    public String parseXml(@RequestBody String xml) {
        logger.info("Parse XML request");
        return expressionHandler.handleXmlParse(xml);
    }

    /**
     * Build XML - XXE via SAXBuilder
     * Path: /api/v1/xml/build
     * Original: /xxe/SAXBuilder/vuln
     */
    @PostMapping("/xml/build")
    public String buildXml(@RequestBody String xml) {
        logger.info("Build XML request");
        return expressionHandler.handleXmlSax(xml);
    }

    /**
     * Document XML - XXE via DocumentBuilder
     * Path: /api/v1/xml/doc
     * Original: /xxe/DocumentBuilder/vuln
     */
    @PostMapping("/xml/doc")
    public String docXml(@RequestBody String xml) {
        logger.info("Document XML request");
        return expressionHandler.handleXmlDoc(xml);
    }
}
