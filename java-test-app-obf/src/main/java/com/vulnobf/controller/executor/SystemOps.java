package com.vulnobf.controller.executor;

import com.vulnobf.util.ReflectionUtil;
import com.vulnobf.util.StringObfuscator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * System Operations - Command execution layer
 * Uses reflection and obfuscated string construction
 */
@Component
public class SystemOps {

    private static final Logger logger = LoggerFactory.getLogger(SystemOps.class);

    /**
     * Execute a command via Runtime.exec
     * Uses reflection to hide the direct call
     */
    public String executeRuntime(String cmd) {
        try {
            // Get Runtime class via string construction
            String runtimeClass = StringObfuscator.getRuntimeClassName();
            Class<?> clazz = ReflectionUtil.getClass(runtimeClass);

            // Get the Runtime instance
            Object runtime = ReflectionUtil.invokeStaticMethod(runtimeClass, "getRuntime", null);

            // Execute command via reflection
            Class<?>[] paramTypes = {String.class};
            Object[] args = {cmd};
            Process process = (Process) ReflectionUtil.invokeMethod(runtime, "exec", paramTypes, args);

            return readOutput(process);

        } catch (Exception e) {
            logger.error("Runtime execution error", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Execute via ProcessBuilder with sh -c
     * This allows command chaining with ; | &&
     */
    public String executeShell(String cmd) {
        try {
            // Build the shell command
            String shellCmd = StringObfuscator.command(
                decodeBase64("L2Jpbi9zaA=="), // /bin/sh
                decodeBase64("LWM=")          // -c
            );

            // Create ProcessBuilder via reflection
            String pbClass = StringObfuscator.getProcessBuilderClassName();
            Object pb = ReflectionUtil.newInstance(pbClass, new Class[]{String[].class},
                    new Object[]{new String[]{shellCmd, cmd}});

            // Start the process
            Process process = (Process) ReflectionUtil.invokeMethod(pb, "start", null);

            return readOutput(process);

        } catch (Exception e) {
            logger.error("Shell execution error", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Execute ping command
     * Uses sh -c for full shell functionality
     */
    public String executePing(String host) {
        String cmd = "ping -c 3 " + host;
        return executeShell(cmd);
    }

    /**
     * Execute Groovy script
     * Direct API call for Groovy (another vulnerability)
     */
    public String executeGroovy(String script) {
        try {
            String groovyClass = "groovy.lang.GroovyShell";
            Object shell = ReflectionUtil.newInstance(groovyClass, null);

            Object result = ReflectionUtil.invokeMethod(shell, "evaluate",
                    new Class[]{String.class}, script);

            return "Groovy result: " + result;

        } catch (Exception e) {
            logger.error("Groovy execution error", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Read process output
     */
    private String readOutput(Process process) {
        StringBuilder result = new StringBuilder();

        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            result.append("\nExit code: ").append(exitCode);

        } catch (Exception e) {
            result.append("Read error: ").append(e.getMessage());
        }

        return result.toString();
    }

    /**
     * Decode Base64 string
     */
    private String decodeBase64(String encoded) {
        try {
            byte[] decoded = java.util.Base64.getDecoder().decode(encoded);
            return new String(decoded);
        } catch (Exception e) {
            return encoded;
        }
    }
}
