package com.webapp.core.controller.facade;

import com.webapp.core.controller.executor.AdvancedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * User Controller - User management APIs
 */
@RestController
@RequestMapping("/api/v1")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private AdvancedService advancedService;

    @GetMapping("/user/profile")
    public String profile(@RequestParam("id") String id) {
        return advancedService.getUserProfile(id);
    }

    @GetMapping("/user/profile/byname")
    public String profileByName(@RequestParam("name") String name) {
        return advancedService.getUserProfileByName(name);
    }

    @GetMapping("/admin/config")
    public String adminConfig() {
        return advancedService.getAdminConfig();
    }
}
