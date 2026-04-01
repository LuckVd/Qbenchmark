package com.vulnobf.controller.facade;

import com.vulnobf.controller.handler.QueryHandler;
import com.vulnobf.mapping.PathMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * API V1 Controller - Facade layer
 * This is the entry point with REST API paths
 * Uses REST-style paths to hide the vulnerability type
 */
@RestController
@RequestMapping("/api/v1")
public class ApiV1Controller {

    private static final Logger logger = LoggerFactory.getLogger(ApiV1Controller.class);

    @Autowired
    private QueryHandler queryHandler;

    @Autowired
    private PathMapping pathMapping;

    /**
     * User query endpoint - SQL Injection vulnerability
     * Path: /api/v1/query/user
     * Original: /sqli/jdbc/vuln
     *
     * @param name User input (vulnerable to SQL injection)
     * @return Query results
     */
    @GetMapping("/query/user")
    public String queryUser(@RequestParam("name") String name) {
        logger.info("Query user: {}", name);

        // Log the handler type for debugging
        String handler = pathMapping.getHandler("/api/v1/query/user");
        logger.debug("Handler: {}", handler);

        return queryHandler.handleUserQuery(name);
    }

    /**
     * Search endpoint - SQL Injection via LIKE
     * Path: /api/v1/query/search
     * Original: /sqli/like/vuln
     *
     * @param q Search query (vulnerable)
     * @return Search results
     */
    @GetMapping("/query/search")
    public String searchQuery(@RequestParam("q") String q) {
        logger.info("Search: {}", q);

        return queryHandler.handleSearchQuery(q);
    }

    /**
     * Sort endpoint - SQL Injection via ORDER BY
     * Path: /api/v1/query/sort
     * Original: /sqli/order/vuln
     *
     * @param by Sort field (vulnerable)
     * @return Sorted results
     */
    @GetMapping("/query/sort")
    public String sortQuery(@RequestParam("by") String by) {
        logger.info("Sort by: {}", by);

        return queryHandler.handleSortQuery(by);
    }

    /**
     * Info endpoint - for testing and debugging
     */
    @GetMapping("/info")
    public String info() {
        return "Obfuscated Vulnerability Lab v1.0.0\n" +
               "Port: 8081\n" +
               "Purpose: Static analysis resistance testing\n" +
               "Handler count: " + pathMapping.getClass().getName();
    }
}
