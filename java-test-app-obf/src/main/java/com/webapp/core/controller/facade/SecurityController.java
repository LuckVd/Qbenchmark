package com.webapp.core.controller.facade;

import com.webapp.core.controller.executor.OtherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Security Controller - Authentication and security APIs
 */
@RestController
@RequestMapping("/api/v1")
public class SecurityController {

    private static final Logger logger = LoggerFactory.getLogger(SecurityController.class);

    @Autowired
    private OtherService otherService;

    @PostMapping("/auth/signin")
    public String signin(@RequestBody String credentials) {
        return otherService.handleSignin(credentials);
    }

    @GetMapping("/admin/home")
    public String adminHome() {
        return otherService.getAdminHome();
    }

    @PostMapping("/auth/reset")
    public String reset(@RequestParam("token") String token, @RequestParam("password") String password) {
        return otherService.resetPassword(token, password);
    }

    @GetMapping("/data/export")
    public String export(@RequestParam("format") String format) {
        return otherService.exportData(format);
    }

    @PostMapping("/file/check")
    public String check(@RequestParam("file") String file) {
        return otherService.checkFile(file);
    }

    @PostMapping("/file/validate")
    public String validate(@RequestParam("file") String file) {
        return otherService.validateFile(file);
    }
}
