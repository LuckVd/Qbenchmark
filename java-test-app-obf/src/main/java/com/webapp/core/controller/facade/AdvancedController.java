package com.webapp.core.controller.facade;

import com.webapp.core.controller.executor.AdvancedService;
import com.webapp.core.controller.executor.OtherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * Advanced Controller - Advanced system APIs
 */
@RestController
@RequestMapping("/api/v1")
public class AdvancedController {

    private static final Logger logger = LoggerFactory.getLogger(AdvancedController.class);

    @Autowired
    private AdvancedService advancedService;

    @Autowired
    private OtherService otherService;

    @GetMapping("/auth/login")
    public String login(@RequestParam("username") String username, @RequestParam("password") String password) {
        return otherService.handleLogin(username, password);
    }

    @GetMapping("/network/client")
    public String clientInfo(HttpServletRequest request) {
        return advancedService.getClientInfo(request);
    }

    @GetMapping("/http/fetch")
    public String fetch(@RequestParam("url") String url) {
        return advancedService.fetchUrl(url);
    }

    @GetMapping("/search")
    public String search(@RequestParam("q") String q) {
        return advancedService.search(q);
    }

    @GetMapping("/remote/rmi")
    public String rmi(@RequestParam("endpoint") String endpoint) {
        return advancedService.connectRmi(endpoint);
    }

    @GetMapping("/remote/ldap")
    public String ldap(@RequestParam("endpoint") String endpoint) {
        return advancedService.connectLdap(endpoint);
    }

    @PostMapping("/http/clte")
    public String clte(@RequestBody String data) {
        return advancedService.handleClte(data);
    }

    @PostMapping("/http/tecl")
    public String tecl(@RequestBody String data) {
        return advancedService.handleTecl(data);
    }
}
