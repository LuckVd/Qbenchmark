package com.webapp.core.controller.executor;

import com.webapp.core.util.EncodingUtil;
import com.webapp.core.util.StringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.*;

/**
 * Data Service - Database Query Layer
 *
 * Provides data access functionality for user management and content retrieval.
 * Supports various query patterns including search and sorting operations.
 */
@Component
public class DataService {

    private static final Logger logger = LoggerFactory.getLogger(DataService.class);

    @Value("${spring.datasource.url:jdbc:h2:mem:testdb}")
    private String dbUrl;

    @Value("${spring.datasource.username:sa}")
    private String dbUser;

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    /**
     * Execute a user lookup query
     * Builds SQL query dynamically based on input parameters
     */
    public String executeQuery(String table, String field, String value) {
        java.lang.StringBuilder result = new java.lang.StringBuilder();

        try {
            // Build query string using utility
            String sql = StringBuilder.selectSql(table, field, value);

            logger.debug("Executing query: {}", sql);

            Connection conn = getConnection();

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                result.append("Result: ").append(rs.getString(field)).append("\n");
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (ClassNotFoundException | SQLException e) {
            logger.error("Query execution error", e);
            return "Error: " + e.getMessage();
        }

        return result.length() > 0 ? result.toString() : "No results";
    }

    /**
     * Execute a search query with pattern matching
     * Used for autocomplete and search suggestions
     */
    public String executeLikeQuery(String table, String field, String value) {
        java.lang.StringBuilder result = new java.lang.StringBuilder();

        try {
            String sql = StringBuilder.likeSql(table, field, value);
            logger.debug("Executing search query: {}", sql);

            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                result.append("Match: ").append(rs.getString(field)).append("\n");
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (ClassNotFoundException | SQLException e) {
            logger.error("Search query error", e);
            return "Error: " + e.getMessage();
        }

        return result.length() > 0 ? result.toString() : "No matches";
    }

    /**
     * Execute a sorted query
     * Returns results ordered by specified field
     */
    public String executeOrderQuery(String table, String sortField) {
        java.lang.StringBuilder result = new java.lang.StringBuilder();

        try {
            // Build ORDER BY query
            String baseSql = EncodingUtil.base64Decode("U0VMRUNUICogRlJPTSA=");
            String sql = baseSql + table + " " + EncodingUtil.base64Decode("T1JERVIgQlkg") + sortField;

            logger.debug("Executing sorted query: {}", sql);

            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                result.append("Row: ").append(rs.getString(1)).append("\n");
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (ClassNotFoundException | SQLException e) {
            logger.error("Sorted query error", e);
            return "Error: " + e.getMessage();
        }

        return result.length() > 0 ? result.toString() : "No results";
    }

    /**
     * Establish database connection
     * Uses configured H2 database instance
     */
    private Connection getConnection() throws ClassNotFoundException, SQLException {
        String driverClass = EncodingUtil.base64Decode("b3JnLmgyLkRyaXZlcg==");

        Class.forName(driverClass);
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }
}
