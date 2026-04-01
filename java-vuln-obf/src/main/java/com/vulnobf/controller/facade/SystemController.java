package com.vulnobf.controller.facade;

import com.vulnobf.controller.handler.SystemHandler;
import com.vulnobf.mapping.PathMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * System Controller - Command injection endpoints
 * REST API facade for system operations
 */
@RestController
@RequestMapping("/api/v1")
public class SystemController {

    private static final Logger logger = LoggerFactory.getLogger(SystemController.class);

    @Autowired
    private SystemHandler systemHandler;

    @Autowired
    private PathMapping pathMapping;

    /**
     * Execute command endpoint - Command injection
     * Path: /api/v1/system/exec
     * Original: /cmd/runtime/vuln
     */
    @GetMapping("/system/exec")
    public String exec(@RequestParam("arg") String arg) {
        logger.info("Exec arg: {}", arg);
        return systemHandler.handleExec(arg);
    }

    /**
     * Run command endpoint - Command injection via ProcessBuilder
     * Path: /api/v1/system/run
     * Original: /cmd/processbuilder/vuln
     */
    @GetMapping("/system/run")
    public String run(@RequestParam("arg") String arg) {
        logger.info("Run arg: {}", arg);
        return systemHandler.handleRun(arg);
    }

    /**
     * Ping endpoint - Command injection
     * Path: /api/v1/network/ping
     * Original: /cmd/ping/vuln
     */
    @GetMapping("/network/ping")
    public String ping(@RequestParam("host") String host) {
        logger.info("Ping host: {}", host);
        return systemHandler.handlePing(host);
    }

    /**
     * Script eval endpoint - Groovy injection
     * Path: /api/v1/script/eval
     * Original: /cmd/groovy
     */
    @GetMapping("/script/eval")
    public String eval(@RequestParam("cmd") String cmd) {
        logger.info("Eval cmd: {}", cmd.substring(0, Math.min(20, cmd.length())));
        return systemHandler.handleEval(cmd);
    }

    /**
     * Network info endpoint
     */
    @GetMapping("/network/info")
    public String networkInfo() {
        return "Network operations:\n" +
               "- exec: Execute system command\n" +
               "- run: Run with shell\n" +
               "- ping: Ping host\n" +
               "- eval: Evaluate script";
    }
}
