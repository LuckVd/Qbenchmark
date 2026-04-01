package com.vulnobf.controller.executor;

import com.vulnobf.util.ReflectionUtil;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Template Operations - SSTI vulnerabilities
 */
@Component
public class TemplateOps {

    private static final Logger logger = LoggerFactory.getLogger(TemplateOps.class);

    /**
     * Evaluate Velocity template
     */
    public String evaluateVelocity(String template, Map<String, Object> params) {
        try {
            VelocityContext context = new VelocityContext();
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                context.put(entry.getKey(), entry.getValue());
            }

            StringWriter writer = new StringWriter();
            Velocity.evaluate(context, writer, "velocity", template);

            return writer.toString();

        } catch (Exception e) {
            logger.error("Velocity error", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Evaluate FreeMarker template
     */
    public String evaluateFreeMarker(String template, Map<String, Object> params) {
        try {
            // Use reflection to avoid direct FreeMarker API dependencies
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);

            // Use a simple string template approach via reflection
            // For demonstration, just return the template
            return "FreeMarker template: " + template;

        } catch (Exception e) {
            logger.error("FreeMarker error", e);
            return "Error: " + e.getMessage();
        }
    }
}
