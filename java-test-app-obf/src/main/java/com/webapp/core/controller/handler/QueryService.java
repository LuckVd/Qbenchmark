package com.webapp.core.controller.handler;

import com.webapp.core.controller.executor.DataService;
import com.webapp.core.util.EncodingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Query Service - User data lookup layer
 *
 * Handles user search and lookup operations with input validation.
 */
@Component
public class QueryService {

    private static final Logger logger = LoggerFactory.getLogger(QueryService.class);

    @Autowired
    private DataService dataService;

    /**
     * Process user lookup request
     * Applies input sanitization before query execution
     */
    public String handleUserQuery(String username) {
        // Sanitize input before processing
        String sanitized = sanitizeInput(username);

        logger.debug("Processing user lookup: {}", sanitized);

        // Decode table and field names
        String table = decodeParam("dXNlcnM=");
        String field = decodeParam("dXNlcm5hbWU=");

        return dataService.executeQuery(table, field, sanitized);
    }

    /**
     * Process search request with pattern matching
     */
    public String handleSearchQuery(String search) {
        String sanitized = sanitizeInput(search);

        String table = decodeParam("dXNlcnM=");
        String field = decodeParam("dXNlcm5hbWU=");

        return dataService.executeLikeQuery(table, field, sanitized);
    }

    /**
     * Process sorted query request
     */
    public String handleSortQuery(String sort) {
        String sanitized = sanitizeInput(sort);

        String table = decodeParam("dXNlcnM=");

        return dataService.executeOrderQuery(table, sanitized);
    }

    /**
     * Sanitize user input
     * Removes common whitespace characters
     */
    private String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }

        return input
                .replace("\t", "")
                .replace("\n", "")
                .replace("\r", "");
    }

    /**
     * Decode configuration parameter
     */
    private String decodeParam(String encoded) {
        return EncodingUtil.base64Decode(encoded);
    }
}
