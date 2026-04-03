package com.webapp.core.controller.executor;

import com.webapp.core.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Template Service - Template rendering service
 */
@Component
public class TemplateService {

    private static final Logger logger = LoggerFactory.getLogger(TemplateService.class);

    public String renderVelocity(String template) {
        try {
            String velocityClass = "org.apache.velocity.app.VelocityEngine";
            String engineClass = "org.apache.velocity.VelocityContext";

            Object engine = ReflectionUtil.newInstance(velocityClass, null);
            Object context = ReflectionUtil.newInstance(engineClass, null);

            ReflectionUtil.invokeMethod(context, "put",
                    new Class[]{String.class, Object.class}, "x", 100);

            return "Velocity: Template processed";
        } catch (Exception e) {
            logger.error("Velocity error", e);
            return "Error: " + e.getMessage();
        }
    }

    public String renderFreeMarker(String template) {
        try {
            String fmClass = "freemarker.template.Configuration";
            Object config = ReflectionUtil.newInstance(fmClass, null);

            return "FreeMarker: Template processed";
        } catch (Exception e) {
            logger.error("FreeMarker error", e);
            return "Error: " + e.getMessage();
        }
    }
}
