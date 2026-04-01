package com.vulnlab.controller;

import com.vulnlab.util.CommandUtil;
import groovy.lang.GroovyShell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/system")
public class SystemExecController {

    private static final Logger logger = LoggerFactory.getLogger(SystemExecController.class);

    @GetMapping("/exec")
    public String exec(@RequestParam("file") String file) {
        String result;

        try {
            String cmd = "ls -la /tmp/" + file;
            logger.info("Command: {}", cmd);

            result = CommandUtil.executeByRuntime(cmd);
        } catch (Exception e) {
            logger.error("Command execution error", e);
            result = "Error: " + e.getMessage();
        }

        return result;
    }

    @GetMapping("/run")
    public String run(@RequestParam("dir") String dir) {
        String result;

        try {
            String[] cmd = {"/bin/sh", "-c", "ls -la " + dir};
            logger.info("Command: /bin/sh -c ls -la {}", dir);

            result = CommandUtil.executeByProcessBuilder(cmd);
        } catch (Exception e) {
            logger.error("Command execution error", e);
            result = "Error: " + e.getMessage();
        }

        return result;
    }

    @GetMapping("/ping")
    public String ping(@RequestParam("host") String host) {
        String result;

        try {
            String[] cmd = {"/bin/sh", "-c", "ping -c 3 " + host};
            logger.info("Command: /bin/sh -c ping -c 3 {}", host);

            result = CommandUtil.executeByProcessBuilder(cmd);
        } catch (Exception e) {
            logger.error("Command execution error", e);
            result = "Error: " + e.getMessage();
        }

        return result;
    }

    @GetMapping("/check")
    public String check(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String xCommand = request.getHeader("X-Command");

        logger.info("User-Agent: {}", userAgent);
        logger.info("X-Command: {}", xCommand);

        String result = "";

        try {
            String cmd = "echo " + (xCommand != null ? xCommand : userAgent);
            result = CommandUtil.executeByProcessBuilder(new String[]{"/bin/sh", "-c", cmd});
        } catch (Exception e) {
            logger.error("Command execution error", e);
            result = "Error: " + e.getMessage();
        }

        return result;
    }

    @GetMapping("/ping/sec")
    public String pingSecure(@RequestParam("host") String host) {
        String result;

        if (!CommandUtil.isValidIp(host)) {
            return "Invalid IP address format";
        }

        try {
            String cmd = "ping -c 3 " + host;
            logger.info("Command: {}", cmd);
            result = CommandUtil.executeByRuntime(cmd);
        } catch (Exception e) {
            logger.error("Command execution error", e);
            result = "Error: " + e.getMessage();
        }

        return result;
    }

    @GetMapping("/script/eval")
    public String scriptEval(@RequestParam("cmd") String cmd) {
        logger.info("Script command: {}", cmd);

        try {
            GroovyShell shell = new GroovyShell();
            Object result = shell.evaluate(cmd);

            logger.info("Script result: {}", result);
            return "Script executed: " + result;
        } catch (Exception e) {
            logger.error("Script execution error", e);
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/script/eval/sec")
    public String scriptEvalSecure(@RequestParam("cmd") String cmd) {
        logger.info("Script secure command: {}", cmd);

        String[] allowedCommands = {"1+1", "2*2", "3-1", "\"test\"", "Math.PI"};

        for (String allowed : allowedCommands) {
            if (allowed.equals(cmd)) {
                try {
                    GroovyShell shell = new GroovyShell();
                    Object result = shell.evaluate(cmd);
                    return "Script secure executed: " + result;
                } catch (Exception e) {
                    return "Error: " + e.getMessage();
                }
            }
        }

        return "Command not allowed. Only safe commands are permitted.";
    }
}
