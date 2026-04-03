package com.webapp.core.controller.facade;

import com.webapp.core.controller.handler.XssHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Content Controller - Search and content rendering APIs
 */
@RestController
@RequestMapping("/api/v1")
public class XssController {

    private static final Logger logger = LoggerFactory.getLogger(XssController.class);

    @Autowired
    private XssHandler xssHandler;

    @GetMapping("/content/search")
    public String search(@RequestParam("q") String q) {
        logger.info("Search query: {}", q);
        return xssHandler.handleSearch(q);
    }

    @PostMapping("/content/search")
    public String searchPost(@RequestParam("q") String q) {
        return xssHandler.handleSearch(q);
    }

    @GetMapping("/content/render")
    public String render(@RequestParam("html") String html) {
        return xssHandler.handleRender(html);
    }

    @PostMapping("/content/render")
    public String renderPost(@RequestBody String html) {
        return xssHandler.handleRender(html);
    }

    @GetMapping("/content/store")
    public String store(@RequestParam("data") String data) {
        return xssHandler.handleStore(data);
    }

    @GetMapping("/content/show")
    public String show() {
        return xssHandler.handleShow();
    }
}
