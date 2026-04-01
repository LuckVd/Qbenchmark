package com.vulnobf.controller.facade;

import com.vulnobf.controller.executor.TemplateOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Template Controller - SSTI endpoints
 */
@RestController
@RequestMapping("/api/v1")
public class TemplateController {

    private static final Logger logger = LoggerFactory.getLogger(TemplateController.class);

    @Autowired
    private TemplateOps templateOps;

    /**
     * Velocity template evaluation
     * Path: /api/v1/template/velocity
     * Original: /ssti/velocity
     */
    @PostMapping("/template/velocity")
    public String velocity(@RequestParam("template") String template) {
        logger.info("Velocity template eval");

        Map<String, Object> params = new HashMap<>();
        params.put("user", "test");

        return templateOps.evaluateVelocity(template, params);
    }

    /**
     * FreeMarker template evaluation
     * Path: /api/v1/template/freemarker
     * Original: /ssti/freemarker
     */
    @PostMapping("/template/freemarker")
    public String freemarker(@RequestParam("template") String template) {
        logger.info("FreeMarker template eval");

        Map<String, Object> params = new HashMap<>();
        params.put("user", "test");

        return templateOps.evaluateFreeMarker(template, params);
    }
}
