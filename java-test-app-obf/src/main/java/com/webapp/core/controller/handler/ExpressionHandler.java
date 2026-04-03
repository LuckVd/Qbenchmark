package com.webapp.core.controller.handler;

import com.webapp.core.controller.executor.ExpressionService;
import com.webapp.core.controller.executor.XmlImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Expression Handler - Dynamic expression processing
 *
 * Handles evaluation of dynamic expressions and templates for
 * configuration and data processing purposes.
 */
@Component
public class ExpressionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ExpressionHandler.class);

    @Autowired
    private ExpressionService expressionService;

    @Autowired
    private XmlImportService xmlImportService;

    /**
     * Process expression evaluation request
     */
    public String handleEvaluate(String expr) {
        String validated = validateExpression(expr);
        return expressionService.evaluateSpel(validated);
    }

    /**
     * Process template expression request
     */
    public String handleTemplate(String tpl) {
        String validated = validateExpression(tpl);
        return expressionService.evaluateSpelSimple(validated);
    }

    /**
     * Process QLExpress script
     */
    public String handleExpress(String script) {
        return expressionService.evaluateQLExpress(script);
    }

    /**
     * Process XML data import
     */
    public String handleXmlParse(String xml) {
        return xmlImportService.parseWithReader(xml);
    }

    /**
     * Process XML with SAX parser
     */
    public String handleXmlSax(String xml) {
        return xmlImportService.parseWithSAXBuilder(xml);
    }

    /**
     * Process XML with DocumentBuilder
     */
    public String handleXmlDoc(String xml) {
        return xmlImportService.parseWithDocumentBuilder(xml);
    }

    /**
     * Validate and normalize expression input
     */
    private String validateExpression(String expr) {
        if (expr == null || expr.isEmpty()) {
            return "1+1";
        }

        return expr.replace("\t", "").replace("\n", "").replace("\r", "");
    }
}
