package com.vulnobf.controller.handler;

import com.vulnobf.controller.executor.ExpressionOps;
import com.vulnobf.controller.executor.XMLOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Expression Handler - Expression processing
 */
@Component
public class ExpressionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ExpressionHandler.class);

    @Autowired
    private ExpressionOps expressionOps;

    @Autowired
    private XMLOps xmlOps;

    /**
     * Handle expression evaluation
     */
    public String handleEvaluate(String expr) {
        // "Validate" expression
        String validated = validateExpression(expr);
        return expressionOps.evaluateSpel(validated);
    }

    /**
     * Handle template expression
     */
    public String handleTemplate(String tpl) {
        String validated = validateExpression(tpl);
        return expressionOps.evaluateSpelSimple(validated);
    }

    /**
     * Handle QLExpress
     */
    public String handleExpress(String script) {
        return expressionOps.evaluateQLExpress(script);
    }

    /**
     * Handle XML parsing
     */
    public String handleXmlParse(String xml) {
        return xmlOps.parseWithReader(xml);
    }

    /**
     * Handle XML with SAXBuilder
     */
    public String handleXmlSax(String xml) {
        return xmlOps.parseWithSAXBuilder(xml);
    }

    /**
     * Handle XML with DocumentBuilder
     */
    public String handleXmlDoc(String xml) {
        return xmlOps.parseWithDocumentBuilder(xml);
    }

    /**
     * Validate expression - fake validation
     */
    private String validateExpression(String expr) {
        if (expr == null || expr.isEmpty()) {
            return "1+1";
        }

        // Remove some harmless patterns but leave the dangerous ones
        return expr.replace("\t", "").replace("\n", "").replace("\r", "");
    }
}
