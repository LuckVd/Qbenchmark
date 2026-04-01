package com.vulnobf.controller.executor;

import com.vulnobf.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Expression Operations - Expression injection vulnerabilities
 */
@Component
public class ExpressionOps {

    private static final Logger logger = LoggerFactory.getLogger(ExpressionOps.class);

    /**
     * Evaluate SpEL expression with StandardEvaluationContext
     * This is vulnerable - allows access to any class
     */
    public String evaluateSpel(String expr) {
        try {
            // Create parser
            ExpressionParser parser = new SpelExpressionParser();

            // Create vulnerable context
            StandardEvaluationContext context = new StandardEvaluationContext();

            // Parse and evaluate
            Expression expression = parser.parseExpression(expr);
            Object result = expression.getValue(context);

            logger.debug("SpEL result: {}", result);
            return "Result: " + result;

        } catch (Exception e) {
            logger.error("SpEL error", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Evaluate SpEL without explicit context
     * Still vulnerable with certain expressions
     */
    public String evaluateSpelSimple(String expr) {
        try {
            ExpressionParser parser = new SpelExpressionParser();
            Expression expression = parser.parseExpression(expr);
            Object result = expression.getValue();

            return "Result: " + result;

        } catch (Exception e) {
            logger.error("SpEL simple error", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Evaluate QLExpress expression
     */
    public String evaluateQLExpress(String expr) {
        try {
            // Create QLExpress via reflection
            String expressClass = "com.ql.util.express.DefaultExpressRunner";
            Object runner = ReflectionUtil.newInstance(expressClass, null);

            // Execute expression
            Object result = ReflectionUtil.invokeMethod(runner, "execute",
                    new Class[]{String.class}, expr);

            return "QLExpress result: " + result;

        } catch (Exception e) {
            logger.error("QLExpress error", e);
            return "Error: " + e.getMessage();
        }
    }
}
