package com.vulnlab.controller;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tpl/data")
public class TplDataController {

    private static final Logger logger = LoggerFactory.getLogger(TplDataController.class);

    @GetMapping("/render")
    public String render(@RequestParam("template") String template) {
        logger.info("Template render: {}", template);

        try {
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
            cfg.setNumberFormat("0.######");
            cfg.setClassicCompatible(true);
            cfg.setAPIBuiltinEnabled(true);

            freemarker.template.Template tpl = new Template(
                "tplTemplate",
                new StringReader(template),
                cfg
            );

            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("object", "test");

            StringWriter writer = new StringWriter();
            tpl.process(dataModel, writer);

            String result = writer.toString();
            return "Template executed. Result: " + result;
        } catch (Exception e) {
            logger.error("Template error", e);
            return "Template error: " + e.getMessage();
        }
    }

    @PostMapping(value = "/render", consumes = "text/plain")
    public String renderPost(@RequestBody String template) {
        logger.info("Template POST render: {}", template);

        try {
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
            cfg.setNumberFormat("0.######");
            cfg.setClassicCompatible(true);
            cfg.setAPIBuiltinEnabled(true);

            freemarker.template.Template tpl = new Template(
                "tplTemplatePost",
                new StringReader(template),
                cfg
            );

            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("object", "test");

            StringWriter writer = new StringWriter();
            tpl.process(dataModel, writer);

            String result = writer.toString();
            return "Template executed. Result: " + result;
        } catch (Exception e) {
            logger.error("Template POST error", e);
            return "Template error: " + e.getMessage();
        }
    }
}
