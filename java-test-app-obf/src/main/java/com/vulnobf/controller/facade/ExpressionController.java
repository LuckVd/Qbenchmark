package com.vulnobf.controller.facade;

import com.vulnobf.controller.executor.ExpressionOps;
import com.vulnobf.controller.handler.ExpressionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Expression Controller - Expression injection endpoints
 */
@RestController
@RequestMapping("/api/v1")
public class ExpressionController {

    private static final Logger logger = LoggerFactory.getLogger(ExpressionController.class);

    @Autowired
    private ExpressionHandler expressionHandler;

    /**
     * Evaluate expression - SpEL injection
     * Path: /api/v1/expression/eval
     * Original: /spel/vuln1
     */
    @GetMapping("/expression/eval")
    public String evalExpr(@RequestParam("expr") String expr) {
        logger.info("Evaluate expression: {}", expr.substring(0, Math.min(20, expr.length())));
        return expressionHandler.handleEvaluate(expr);
    }

    /**
     * Template expression - SpEL injection
     * Path: /api/v1/expression/template
     * Original: /spel/vuln2
     */
    @GetMapping("/expression/template")
    public String templateExpr(@RequestParam("tpl") String tpl) {
        logger.info("Template expression");
        return expressionHandler.handleTemplate(tpl);
    }

    /**
     * Script execute - QLExpress injection
     * Path: /api/v1/script/express
     * Original: /qlexpress/vuln
     */
    @PostMapping("/script/express")
    public String expressScript(@RequestBody String script) {
        logger.info("Express script");
        return expressionHandler.handleExpress(script);
    }
}
