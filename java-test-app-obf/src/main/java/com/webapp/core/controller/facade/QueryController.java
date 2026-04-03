package com.webapp.core.controller.facade;

import com.webapp.core.controller.handler.QueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Query Controller - User data lookup APIs
 */
@RestController
@RequestMapping("/api/v1")
public class QueryController {

    private static final Logger logger = LoggerFactory.getLogger(QueryController.class);

    @Autowired
    private QueryService queryService;

    @GetMapping("/query/user")
    public String getUser(@RequestParam("name") String name) {
        logger.info("User lookup request: {}", name);
        return queryService.handleUserQuery(name);
    }

    @GetMapping("/query/search")
    public String search(@RequestParam("q") String q) {
        logger.info("Search request: {}", q);
        return queryService.handleSearchQuery(q);
    }

    @GetMapping("/query/sort")
    public String sort(@RequestParam("by") String by) {
        logger.info("Sort request: {}", by);
        return queryService.handleSortQuery(by);
    }
}
