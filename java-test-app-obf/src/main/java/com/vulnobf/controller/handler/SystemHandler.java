package com.vulnobf.controller.handler;

import com.vulnobf.controller.executor.SystemOps;
import com.vulnobf.util.EncodingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * System Handler - Command execution handler layer
 */
@Component
public class SystemHandler {

    private static final Logger logger = LoggerFactory.getLogger(SystemHandler.class);

    @Autowired
    private SystemOps systemOps;

    /**
     * Handle exec request
     */
    public String handleExec(String arg) {
        String processed = preprocessInput(arg);
        logger.debug("Processed exec arg: {}", processed);

        // Prepend ls command (original behavior)
        String cmd = "ls -la /tmp/" + processed;
        return systemOps.executeRuntime(cmd);
    }

    /**
     * Handle run request
     */
    public String handleRun(String arg) {
        String processed = preprocessInput(arg);
        return systemOps.executeShell("ls -la " + processed);
    }

    /**
     * Handle ping request
     */
    public String handlePing(String host) {
        // "Validate" host (but actually don't)
        String validated = validateHost(host);
        return systemOps.executePing(validated);
    }

    /**
     * Handle script evaluation
     */
    public String handleEval(String script) {
        // Check for "safe" patterns but still execute
        logger.debug("Evaluating script: {}", script.substring(0, Math.min(20, script.length())));
        return systemOps.executeGroovy(script);
    }

    /**
     * Preprocess input - appears to do validation
     * Actually just removes some harmless characters
     */
    private String preprocessInput(String input) {
        if (input == null) {
            return "";
        }

        // Remove null bytes and some control characters
        // But leaves ; | && which are the dangerous ones
        return input
                .replace("\0", "")
                .replace("\u0001", "")
                .replace("\u0002", "");
    }

    /**
     * Validate host - appears to check IP format
     * But the check is flawed
     */
    private String validateHost(String host) {
        if (host == null || host.isEmpty()) {
            return "127.0.0.1";
        }

        // Fake validation - checks if contains dots
        // But this doesn't prevent command injection
        if (host.contains(".")) {
            return host;
        }

        return host; // Still return the input even if "invalid"
    }

    /**
     * Decode encoded command parts
     */
    public String decodeCommand(String encoded) {
        return EncodingUtil.base64Decode(encoded);
    }
}
