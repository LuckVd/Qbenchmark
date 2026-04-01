package com.vulnlab.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.sql.*;

@RestController
@RequestMapping("/api/v1/query")
public class QueryController {

    private static final Logger logger = LoggerFactory.getLogger(QueryController.class);

    @Value("${spring.datasource.url:jdbc:mysql://localhost:3306/test}")
    private String dbUrl;

    @Value("${spring.datasource.username:root}")
    private String dbUser;

    @Value("${spring.datasource.password:password}")
    private String dbPassword;

    @GetMapping("/user")
    public String getUser(@RequestParam("name") String name) {
        StringBuilder result = new StringBuilder();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

            String sql = "SELECT * FROM users WHERE username = '" + name + "'";
            logger.info("SQL: {}", sql);

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                result.append("Username: ").append(rs.getString("username"))
                      .append(", Password: ").append(rs.getString("password")).append("\n");
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (ClassNotFoundException | SQLException e) {
            logger.error("Query error", e);
            return "Database error: " + e.getMessage();
        }

        return result.length() > 0 ? result.toString() : "No results found";
    }

    @GetMapping("/user/sec")
    public String getUserSecure(@RequestParam("name") String name) {
        StringBuilder result = new StringBuilder();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

            String sql = "SELECT * FROM users WHERE username = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);

            logger.info("Secure SQL with PreparedStatement");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                result.append("Username: ").append(rs.getString("username"))
                      .append(", Password: ").append(rs.getString("password")).append("\n");
            }

            rs.close();
            pstmt.close();
            conn.close();

        } catch (ClassNotFoundException | SQLException e) {
            logger.error("SQL error", e);
            return "Database error: " + e.getMessage();
        }

        return result.length() > 0 ? result.toString() : "No results found";
    }

    @GetMapping("/search")
    public String search(@RequestParam("q") String q) {
        StringBuilder result = new StringBuilder();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

            String sql = "SELECT * FROM users WHERE username LIKE '%" + q + "%'";
            logger.info("SQL: {}", sql);

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                result.append("Username: ").append(rs.getString("username")).append("\n");
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (ClassNotFoundException | SQLException e) {
            logger.error("Query error", e);
            return "Database error: " + e.getMessage();
        }

        return result.length() > 0 ? result.toString() : "No results found";
    }

    @GetMapping("/sort")
    public String sort(@RequestParam("by") String by) {
        StringBuilder result = new StringBuilder();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

            String sql = "SELECT * FROM users ORDER BY " + by;
            logger.info("SQL: {}", sql);

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                result.append("Username: ").append(rs.getString("username")).append("\n");
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (ClassNotFoundException | SQLException e) {
            logger.error("Query error", e);
            return "Database error: " + e.getMessage();
        }

        return result.length() > 0 ? result.toString() : "No results found";
    }
}
