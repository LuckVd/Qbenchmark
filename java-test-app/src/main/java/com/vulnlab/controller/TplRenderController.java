package com.vulnlab.controller;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.StringWriter;

@RestController
@RequestMapping("/api/v1/tpl")
public class TplRenderController {

    private static final Logger logger = LoggerFactory.getLogger(TplRenderController.class);
    private final VelocityEngine engine = new VelocityEngine();

    @GetMapping("/velocity")
    public String velocity(@RequestParam("template") String template) {
        logger.info("Velocity template: {}", template);

        try {
            VelocityContext context = new VelocityContext();
            context.put("msg", "Hello!");
            context.put("name", "guest");

            StringWriter writer = new StringWriter();
            engine.evaluate(context, writer, "log", template);

            String result = writer.toString();
            return "Template executed. Result: " + result;
        } catch (Exception e) {
            logger.error("Velocity error", e);
            return "Velocity error: " + e.getMessage();
        }
    }

    @PostMapping(value = "/velocity", consumes = "text/plain")
    public String velocityPost(@RequestBody String template) {
        logger.info("Velocity POST template: {}", template);

        try {
            VelocityContext context = new VelocityContext();
            context.put("name", "guest");

            StringWriter writer = new StringWriter();
            engine.evaluate(context, writer, "log", template);

            String result = writer.toString();
            return "Template executed. Result: " + result;
        } catch (Exception e) {
            logger.error("Velocity POST error", e);
            return "Velocity error: " + e.getMessage();
        }
    }
}
