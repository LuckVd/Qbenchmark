package com.vulnlab.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/expr")
public class ExprEvalController {

    private static final Logger logger = LoggerFactory.getLogger(ExprEvalController.class);

    @GetMapping("/eval")
    public String eval(@RequestParam("expression") String expression) {
        logger.info("Expression eval called with: {}", expression);

        try {
            ExpressionParser parser = new SpelExpressionParser();
            StandardEvaluationContext context = new StandardEvaluationContext();
            Expression exp = parser.parseExpression(expression);
            Object result = exp.getValue(context);

            logger.info("Expression result: {}", result);
            return "Expression executed. Result: " + result;
        } catch (Exception e) {
            logger.error("Expression error", e);
            return "Expression error: " + e.getMessage();
        }
    }

    @GetMapping("/template")
    public String template(@RequestParam("expression") String expression) {
        logger.info("Expression template called with: {}", expression);

        try {
            ExpressionParser parser = new SpelExpressionParser();
            Expression exp = parser.parseExpression(expression);
            Object result = exp.getValue();

            logger.info("Template result: {}", result);
            return "Template executed. Result: " + result;
        } catch (Exception e) {
            logger.error("Template error", e);
            return "Expression error: " + e.getMessage();
        }
    }
}
