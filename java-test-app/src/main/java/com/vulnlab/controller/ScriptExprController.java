package com.vulnlab.controller;

import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/script")
public class ScriptExprController {

    private static final Logger logger = LoggerFactory.getLogger(ScriptExprController.class);

    @GetMapping("/express")
    public String express(@RequestParam("expression") String expression) {
        logger.info("Expression: {}", expression);

        try {
            ExpressRunner runner = new ExpressRunner();
            Object result = runner.execute(expression, null, null, true, false);

            logger.info("Result: {}", result);
            return "Executed. Result: " + result;
        } catch (Exception e) {
            logger.error("Execution error", e);
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/express/v2")
    public String expressV2(@RequestParam("expression") String expression,
                            @RequestParam(value = "name", defaultValue = "guest") String name) {
        logger.info("Expression V2: {}, name: {}", expression, name);

        try {
            ExpressRunner runner = new ExpressRunner();

            DefaultContext<String, Object> context = new DefaultContext<>();
            context.put("name", name);

            Object result = runner.execute(expression, context, null, true, false);

            logger.info("Result V2: {}", result);
            return "Executed. Result: " + result;
        } catch (Exception e) {
            logger.error("Execution V2 error", e);
            return "Error: " + e.getMessage();
        }
    }
}
