package com.vulnobf.controller.executor;

import com.vulnobf.util.EncodingUtil;
import com.vulnobf.util.StringObfuscator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.*;

/**
 * SQL Operations - The actual vulnerable code layer
 * This is where the real SQL injection happens
 */
@Component
public class SqlOperations {

    private static final Logger logger = LoggerFactory.getLogger(SqlOperations.class);

    @Value("${spring.datasource.url:jdbc:h2:mem:testdb}")
    private String dbUrl;

    @Value("${spring.datasource.username:sa}")
    private String dbUser;

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    /**
     * Execute a vulnerable SQL query with string concatenation
     * Uses multiple obfuscation layers
     */
    public String executeVulnerableQuery(String table, String field, String value) {
        StringBuilder result = new StringBuilder();

        try {
            // Use StringObfuscator to build SQL - hides the direct string literal
            String sql = StringObfuscator.selectSql(table, field, value);

            logger.debug("Query: {}", sql);

            // Get connection via reflection to hide direct API call
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
            logger.error("Query error", e);
            return "Error: " + e.getMessage();
        }

        return result.length() > 0 ? result.toString() : "No results";
    }

    /**
     * Execute a LIKE query vulnerability
     */
    public String executeLikeQuery(String table, String field, String value) {
        StringBuilder result = new StringBuilder();

        try {
            String sql = StringObfuscator.likeSql(table, field, value);
            logger.debug("LIKE query: {}", sql);

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
            logger.error("LIKE query error", e);
            return "Error: " + e.getMessage();
        }

        return result.length() > 0 ? result.toString() : "No matches";
    }

    /**
     * Execute ORDER BY vulnerability
     */
    public String executeOrderQuery(String table, String sortField) {
        StringBuilder result = new StringBuilder();

        try {
            // Build ORDER BY with concatenation
            String baseSql = EncodingUtil.base64Decode("U0VMRUNUICogRlJPTSA=");
            String sql = baseSql + table + " " + EncodingUtil.base64Decode("T1JERVIgQlkg") + sortField;

            logger.debug("ORDER BY query: {}", sql);

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
            logger.error("ORDER BY error", e);
            return "Error: " + e.getMessage();
        }

        return result.length() > 0 ? result.toString() : "No results";
    }

    /**
     * Get database connection
     * Uses H2 database (configured in application.yml)
     */
    private Connection getConnection() throws ClassNotFoundException, SQLException {
        // Use H2 driver - no class loading needed for H2 in Spring Boot
        // The encoded name below is just for obfuscation demo
        // "org.h2.Driver" encoded
        String driverClass = EncodingUtil.base64Decode("b3JnLmgyLkRyaXZlcg==");

        Class.forName(driverClass);
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }
}
