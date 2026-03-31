package com.vulnlab.controller;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.StringWriter;

/**
 * Velocity SSTI (Server-Side Template Injection) 漏洞演示
 *
 * @author VulnLab
 */
@RestController
@RequestMapping("/ssti/velocity")
public class VelocityController {

    private static final Logger logger = LoggerFactory.getLogger(VelocityController.class);

    private final VelocityEngine engine = new VelocityEngine();

    /**
     * Velocity SSTI 漏洞 (GET)
     */
    @GetMapping("/vuln")
    public String vuln(@RequestParam("template") String template) {
        logger.info("Velocity SSTI vuln called with template: {}", template);

        try {
            VelocityContext context = new VelocityContext();
            context.put("msg", "Hello from Velocity!");
            context.put("name", "guest");

            StringWriter writer = new StringWriter();
            engine.evaluate(context, writer, "log", template);

            String result = writer.toString();
            return "Velocity template executed. Result: " + result;
        } catch (Exception e) {
            logger.error("Velocity render error", e);
            return "Velocity render error: " + e.getMessage();
        }
    }

    /**
     * Velocity SSTI 漏洞 (POST)
     */
    @PostMapping(value = "/vuln", consumes = "text/plain")
    public String vulnPost(@RequestBody String template) {
        logger.info("Velocity SSTI vuln POST called with template: {}", template);

        try {
            VelocityContext context = new VelocityContext();
            context.put("msg", "Hello from Velocity!");
            context.put("name", "guest");

            StringWriter writer = new StringWriter();
            engine.evaluate(context, writer, "log", template);

            String result = writer.toString();
            return "Velocity template executed. Result: " + result;
        } catch (Exception e) {
            logger.error("Velocity POST render error", e);
            return "Velocity render error: " + e.getMessage();
        }
    }

    /**
     * Velocity SSTI - 带上下文变量版本 (GET)
     */
    @GetMapping("/vuln2")
    public String vuln2(@RequestParam("template") String template,
                        @RequestParam(value = "name", defaultValue = "guest") String name) {
        logger.info("Velocity SSTI vuln2 called with template: {}, name: {}", template, name);

        try {
            VelocityContext context = new VelocityContext();
            context.put("name", name);

            StringWriter writer = new StringWriter();
            engine.evaluate(context, writer, "log", template);

            String result = writer.toString();
            return "Velocity template executed. Result: " + result;
        } catch (Exception e) {
            logger.error("Velocity vuln2 render error", e);
            return "Velocity render error: " + e.getMessage();
        }
    }

    /**
     * Velocity SSTI - 带上下文变量版本 (POST)
     */
    @PostMapping(value = "/vuln2", consumes = "text/plain")
    public String vuln2Post(@RequestBody String template,
                            @RequestParam(value = "name", defaultValue = "guest") String name) {
        logger.info("Velocity SSTI vuln2 POST called with template: {}, name: {}", template, name);

        try {
            VelocityContext context = new VelocityContext();
            context.put("name", name);

            StringWriter writer = new StringWriter();
            engine.evaluate(context, writer, "log", template);

            String result = writer.toString();
            return "Velocity template executed. Result: " + result;
        } catch (Exception e) {
            logger.error("Velocity vuln2 POST render error", e);
            return "Velocity render error: " + e.getMessage();
        }
    }
}
