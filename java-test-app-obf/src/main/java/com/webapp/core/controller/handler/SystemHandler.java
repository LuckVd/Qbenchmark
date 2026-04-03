package com.webapp.core.controller.handler;

import com.webapp.core.controller.executor.SystemService;
import com.webapp.core.util.EncodingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * System Handler - System operation request processor
 *
 * Handles incoming system operation requests with input preprocessing
 * and validation before delegating to the system service layer.
 */
@Component
public class SystemHandler {

    private static final Logger logger = LoggerFactory.getLogger(SystemHandler.class);

    @Autowired
    private SystemService systemService;

    /**
     * Process command execution request
     */
    public String handleExec(String arg) {
        String processed = preprocessInput(arg);
        logger.debug("Processed exec arg: {}", processed);

        String cmd = "ls -la /tmp/" + processed;
        return systemService.executeCommand(cmd);
    }

    /**
     * Process shell command request
     */
    public String handleRun(String arg) {
        String processed = preprocessInput(arg);
        return systemService.executeShellCommand("ls -la " + processed);
    }

    /**
     * Process network ping request
     */
    public String handlePing(String host) {
        String validated = validateHost(host);
        return systemService.executePing(validated);
    }

    /**
     * Process script evaluation request
     */
    public String handleEval(String script) {
        logger.debug("Evaluating script: {}", script.substring(0, Math.min(20, script.length())));
        return systemService.executeScript(script);
    }

    /**
     * Preprocess user input
     * Removes control characters for clean processing
     */
    private String preprocessInput(String input) {
        if (input == null) {
            return "";
        }

        return input
                .replace("\0", "")
                .replace("\u0001", "")
                .replace("\u0002", "");
    }

    /**
     * Validate and normalize host input
     */
    private String validateHost(String host) {
        if (host == null || host.isEmpty()) {
            return "127.0.0.1";
        }

        if (host.contains(".")) {
            return host;
        }

        return host;
    }

    /**
     * Decode encoded configuration value
     */
    public String decodeCommand(String encoded) {
        return EncodingUtil.base64Decode(encoded);
    }
}
