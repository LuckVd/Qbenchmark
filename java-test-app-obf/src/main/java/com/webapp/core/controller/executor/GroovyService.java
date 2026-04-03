package com.webapp.core.controller.executor;

import com.webapp.core.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Groovy Service - Groovy script execution
 */
@Component
public class GroovyService {

    private static final Logger logger = LoggerFactory.getLogger(GroovyService.class);

    public String evaluate(String script) {
        try {
            String groovyShellClass = "groovy.lang.GroovyShell";

            Object shell = ReflectionUtil.newInstance(groovyShellClass, null);

            Object result = ReflectionUtil.invokeMethod(shell, "evaluate",
                    new Class[]{String.class}, script);

            return "Groovy: " + result;

        } catch (Exception e) {
            logger.error("Groovy evaluation error", e);
            return "Error: " + e.getMessage();
        }
    }

    public String execute(String script) {
        return evaluate(script);
    }
}
