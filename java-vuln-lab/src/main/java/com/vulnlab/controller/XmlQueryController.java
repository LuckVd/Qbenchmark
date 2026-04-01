package com.vulnlab.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathConstants;
import org.xml.sax.InputSource;

import java.io.StringReader;

@RestController
@RequestMapping("/api/v1/xml/query")
public class XmlQueryController {

    private static final Logger logger = LoggerFactory.getLogger(XmlQueryController.class);

    @PostMapping("/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password) {
        logger.info("Login attempt - username: {}", username);

        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new StringReader("<users>"
                            + "<user>"
                            + "<username>admin</username>"
                            + "<password>secret123</password>"
                            + "<role>administrator</role>"
                            + "</user>"
                            + "<user>"
                            + "<username>user</username>"
                            + "<password>userpass</password>"
                            + "<role>normal</role>"
                            + "</user>"
                            + "</users>")));

            XPath xpath = XPathFactory.newInstance().newXPath();
            String query = "/users/user[username='" + username + "' and password='" + password + "']";
            logger.info("Query: {}", query);

            NodeList nodes = (NodeList) xpath.evaluate(query, doc, XPathConstants.NODESET);

            if (nodes.getLength() > 0) {
                logger.warn("Login successful for: {}", username);
                return "Login successful! Welcome back, " + username;
            } else {
                return "Login failed: invalid credentials";
            }
        } catch (Exception e) {
            logger.error("Login error", e);
            return "Error: " + e.getMessage();
        }
    }

    @PostMapping("/login/sec")
    public String loginSecure(@RequestParam("username") String username,
                              @RequestParam("password") String password) {
        logger.info("Secure login attempt - username: {}", username);

        if (username.contains("'") || username.contains("\"") ||
            username.contains("or ") || username.contains("and ") ||
            password.contains("'") || password.contains("\"")) {
            return "Login failed: invalid characters detected";
        }

        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new StringReader("<users>"
                            + "<user>"
                            + "<username>admin</username>"
                            + "<password>secret123</password>"
                            + "<role>administrator</role>"
                            + "</user>"
                            + "<user>"
                            + "<username>user</username>"
                            + "<password>userpass</password>"
                            + "<role>normal</role>"
                            + "</user>"
                            + "</users>")));

            XPath xpath = XPathFactory.newInstance().newXPath();
            String query = "/users/user[username='" + username + "' and password='" + password + "']";
            NodeList nodes = (NodeList) xpath.evaluate(query, doc, XPathConstants.NODESET);

            if (nodes.getLength() > 0) {
                return "Login successful! Welcome back, " + username;
            } else {
                return "Login failed: invalid credentials";
            }
        } catch (Exception e) {
            logger.error("Secure login error", e);
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/info")
    public String info() {
        return "XML Query Login\n" +
               "POST /api/v1/xml/query/login\n" +
               "Payload: username=admin&password=secret123\n";
    }
}
