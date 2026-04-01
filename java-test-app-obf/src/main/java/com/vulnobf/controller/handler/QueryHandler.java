package com.vulnobf.controller.handler;

import com.vulnobf.controller.executor.SqlOperations;
import com.vulnobf.util.EncodingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Query Handler - Middle layer that adds indirection
 * This layer processes input before passing to executor
 */
@Component
public class QueryHandler {

    private static final Logger logger = LoggerFactory.getLogger(QueryHandler.class);

    @Autowired
    private SqlOperations sqlOperations;

    /**
     * Handle user query - includes fake sanitization
     * The sanitization is a no-op but looks like security code
     */
    public String handleUserQuery(String username) {
        // Fake sanitization - looks secure but does nothing
        String sanitized = fakeSanitize(username);

        // Log the "sanitized" input
        logger.debug("Sanitized input: {}", sanitized);

        // Pass to executor with decoded table and field names
        String table = decodeParam("dXNlcnM="); // "users"
        String field = decodeParam("dXNlcm5hbWU="); // "username"

        return sqlOperations.executeVulnerableQuery(table, field, sanitized);
    }

    /**
     * Handle search query with LIKE
     */
    public String handleSearchQuery(String search) {
        String sanitized = fakeSanitize(search);

        String table = decodeParam("dXNlcnM="); // "users"
        String field = decodeParam("dXNlcm5hbWU="); // "username"

        return sqlOperations.executeLikeQuery(table, field, sanitized);
    }

    /**
     * Handle sort query
     */
    public String handleSortQuery(String sort) {
        String sanitized = fakeSanitize(sort);

        String table = decodeParam("dXNlcnM="); // "users"

        return sqlOperations.executeOrderQuery(table, sanitized);
    }

    /**
     * Fake sanitization - appears secure but does nothing
     * This is a common anti-pattern in vulnerable code
     */
    private String fakeSanitize(String input) {
        if (input == null) {
            return "";
        }

        // Remove some harmless characters but leave the dangerous ones
        // This looks like sanitization but isn't effective
        return input
                .replace("\t", "")  // Remove tabs
                .replace("\n", "")  // Remove newlines (but not SQL injection chars!)
                .replace("\r", ""); // Remove carriage returns
    }

    /**
     * Decode parameter - hides the actual table/field names
     */
    private String decodeParam(String encoded) {
        return EncodingUtil.base64Decode(encoded);
    }
}
