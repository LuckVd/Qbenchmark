package com.vulnobf.controller.facade;

import com.vulnobf.controller.executor.OtherOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Other Vulnerabilities Controller
 */
@RestController
@RequestMapping("/api/v1")
public class OtherVulnController {

    private static final Logger logger = LoggerFactory.getLogger(OtherVulnController.class);

    @Autowired
    private OtherOps otherOps;

    /**
     * XPath login
     * Path: /api/v1/auth/login
     * Original: /xpath/login
     */
    @PostMapping("/auth/login")
    public String login(@RequestParam("username") String username,
                       @RequestParam("password") String password) {
        logger.info("Login attempt: {}", username);
        return otherOps.xpathLogin(username, password);
    }

    /**
     * Get client IP (spoofable)
     * Path: /api/v1/network/client
     * Original: /ip/spoof
     */
    @GetMapping("/network/client")
    public String clientIp(@RequestHeader(value = "X-Forwarded-For", required = false) String xff,
                           @RequestHeader(value = "X-Real-IP", defaultValue = "127.0.0.1") String remote) {
        return otherOps.getClientIp(xff, remote);
    }

    /**
     * SSRF fetch
     * Path: /api/v1/http/fetch
     * Original: /ssrf/vuln
     */
    @GetMapping("/http/fetch")
    public String fetch(@RequestParam("url") String url) {
        logger.info("Fetch URL: {}", url);
        return otherOps.fetchUrl(url);
    }

    /**
     * XSS search
     * Path: /api/v1/search
     * Original: /xss/reflected
     */
    @GetMapping("/search")
    public String search(@RequestParam("q") String query) {
        logger.info("Search: {}", query);
        return otherOps.search(query);
    }

    /**
     * Path traversal
     * Path: /api/v1/file/read
     * Original: /traversal/vuln
     */
    @GetMapping("/file/read")
    public String readFile(@RequestParam("file") String filename) {
        logger.info("Read file: {}", filename);
        return otherOps.readFile(filename);
    }
}
