package com.webapp.core.controller.executor;

import com.webapp.core.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Expression Service - Dynamic expression evaluation
 *
 * Provides expression evaluation capabilities for dynamic configuration
 * and data processing using SpEL and custom expression engines.
 */
@Component
public class ExpressionService {

    private static final Logger logger = LoggerFactory.getLogger(ExpressionService.class);

    /**
     * Evaluate SpEL expression with full context
     */
    public String evaluateSpel(String expr) {
        try {
            ExpressionParser parser = new SpelExpressionParser();

            StandardEvaluationContext context = new StandardEvaluationContext();

            Expression expression = parser.parseExpression(expr);
            Object result = expression.getValue(context);

            logger.debug("SpEL result: {}", result);
            return "Result: " + result;

        } catch (Exception e) {
            logger.error("Expression evaluation error", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Evaluate SpEL expression with default context
     */
    public String evaluateSpelSimple(String expr) {
        try {
            ExpressionParser parser = new SpelExpressionParser();
            Expression expression = parser.parseExpression(expr);
            Object result = expression.getValue();

            return "Result: " + result;

        } catch (Exception e) {
            logger.error("Simple expression error", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Evaluate QLExpress expression
     */
    public String evaluateQLExpress(String expr) {
        try {
            String expressClass = "com.ql.util.express.DefaultExpressRunner";
            Object runner = ReflectionUtil.newInstance(expressClass, null);

            Object result = ReflectionUtil.invokeMethod(runner, "execute",
                    new Class[]{String.class}, expr);

            return "QLExpress result: " + result;

        } catch (Exception e) {
            logger.error("QLExpress error", e);
            return "Error: " + e.getMessage();
        }
    }
}
