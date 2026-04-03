package com.webapp.core.controller.facade;

import com.webapp.core.controller.handler.ExpressionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Xml Controller - XML data import APIs
 */
@RestController
@RequestMapping("/api/v1")
public class XmlController {

    private static final Logger logger = LoggerFactory.getLogger(XmlController.class);

    @Autowired
    private ExpressionHandler expressionHandler;

    @PostMapping("/xml/parse")
    public String parseXml(@RequestBody String xml) {
        return expressionHandler.handleXmlParse(xml);
    }

    @PostMapping("/xml/build")
    public String buildXml(@RequestBody String xml) {
        return expressionHandler.handleXmlSax(xml);
    }

    @PostMapping("/xml/doc")
    public String docXml(@RequestBody String xml) {
        return expressionHandler.handleXmlDoc(xml);
    }
}
