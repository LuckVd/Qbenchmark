package com.webapp.core.controller.facade;

import com.webapp.core.controller.executor.TemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Template Controller - Template processing APIs
 */
@RestController
@RequestMapping("/api/v1")
public class TemplateController {

    private static final Logger logger = LoggerFactory.getLogger(TemplateController.class);

    @Autowired
    private TemplateService templateService;

    @GetMapping("/template/velocity")
    public String velocity(@RequestParam("template") String template) {
        return templateService.renderVelocity(template);
    }

    @PostMapping(value = "/template/velocity", consumes = "text/plain")
    public String velocityPost(@RequestBody String template) {
        return templateService.renderVelocity(template);
    }

    @GetMapping("/template/freemarker")
    public String freemarker(@RequestParam("template") String template) {
        return templateService.renderFreeMarker(template);
    }

    @PostMapping(value = "/template/freemarker", consumes = "text/plain")
    public String freemarkerPost(@RequestBody String template) {
        return templateService.renderFreeMarker(template);
    }
}
