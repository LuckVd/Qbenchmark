package com.vulnlab.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.sql.*;

/**
 * SQL注入漏洞演示
 *
 * 漏洞说明：
 * - 直接拼接用户输入到SQL语句中
 * - 未使用预编译语句(PreparedStatement)
 *
 * 修复方案：使用PreparedStatement参数化查询
 *
 * @author VulnLab
 */
@RestController
@RequestMapping("/sqli")
public class SQLInjectionController {

    private static final Logger logger = LoggerFactory.getLogger(SQLInjectionController.class);

    @Value("${spring.datasource.url:jdbc:mysql://localhost:3306/test}")
    private String dbUrl;

    @Value("${spring.datasource.username:root}")
    private String dbUser;

    @Value("${spring.datasource.password:password}")
    private String dbPassword;

    /**
     * SQL注入漏洞 - JDBC方式
     *
     * 测试URL: http://localhost:8080/sqli/jdbc/vuln?username=admin' OR '1'='1
     * 正常URL: http://localhost:8080/sqli/jdbc/vuln?username=admin
     *
     * @param username 用户名
     * @return 查询结果
     */
    @GetMapping("/jdbc/vuln")
    public String jdbcVuln(@RequestParam("username") String username) {
        StringBuilder result = new StringBuilder();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

            // 漏洞代码：直接拼接SQL
            String sql = "SELECT * FROM users WHERE username = '" + username + "'";
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
            logger.error("SQL Injection error", e);
            return "Database error: " + e.getMessage();
        }

        return result.length() > 0 ? result.toString() : "No results found";
    }

    /**
     * SQL注入安全代码 - 使用PreparedStatement
     *
     * 测试URL: http://localhost:8080/sqli/jdbc/sec?username=admin
     *
     * @param username 用户名
     * @return 查询结果
     */
    @GetMapping("/jdbc/sec")
    public String jdbcSecure(@RequestParam("username") String username) {
        StringBuilder result = new StringBuilder();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

            // 安全代码：使用预编译语句
            String sql = "SELECT * FROM users WHERE username = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);

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

    /**
     * SQL注入漏洞 - like子句
     *
     * 测试URL: http://localhost:8080/sqli/like/vuln?username=admin%' OR '1'='1
     *
     * @param username 用户名
     * @return 查询结果
     */
    @GetMapping("/like/vuln")
    public String likeVuln(@RequestParam("username") String username) {
        StringBuilder result = new StringBuilder();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

            // 漏洞代码：like子句直接拼接
            String sql = "SELECT * FROM users WHERE username LIKE '%" + username + "%'";
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
            logger.error("SQL Injection error", e);
            return "Database error: " + e.getMessage();
        }

        return result.length() > 0 ? result.toString() : "No results found";
    }

    /**
     * SQL注入漏洞 - order by子句
     *
     * 测试URL: http://localhost:8080/sqli/order/vuln?sort=id ASC; DROP TABLE users--
     *
     * @param sort 排序字段
     * @return 查询结果
     */
    @GetMapping("/order/vuln")
    public String orderVuln(@RequestParam("sort") String sort) {
        StringBuilder result = new StringBuilder();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

            // 漏洞代码：order by直接拼接
            String sql = "SELECT * FROM users ORDER BY " + sort;
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
            logger.error("SQL Injection error", e);
            return "Database error: " + e.getMessage();
        }

        return result.length() > 0 ? result.toString() : "No results found";
    }
}
