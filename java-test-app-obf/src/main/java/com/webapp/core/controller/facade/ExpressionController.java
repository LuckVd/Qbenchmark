package com.webapp.core.controller.facade;

import com.webapp.core.controller.handler.ExpressionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Expression Controller - Dynamic expression evaluation APIs
 */
@RestController
@RequestMapping("/api/v1")
public class ExpressionController {

    private static final Logger logger = LoggerFactory.getLogger(ExpressionController.class);

    @Autowired
    private ExpressionHandler expressionHandler;

    @GetMapping("/expression/eval")
    public String evaluate(@RequestParam("expr") String expr) {
        logger.info("Expression evaluation request");
        return expressionHandler.handleEvaluate(expr);
    }

    @GetMapping("/expression/template")
    public String template(@RequestParam("expr") String expr) {
        logger.info("Template evaluation request");
        return expressionHandler.handleTemplate(expr);
    }

    @GetMapping("/script/express")
    public String express(@RequestParam("cmd") String cmd) {
        logger.info("Express script request");
        return expressionHandler.handleExpress(cmd);
    }
}
