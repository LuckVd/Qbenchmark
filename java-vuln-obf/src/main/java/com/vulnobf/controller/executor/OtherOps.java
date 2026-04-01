package com.vulnobf.controller.executor;

import com.vulnobf.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Other Operations - Additional vulnerabilities
 */
@Component
public class OtherOps {

    private static final Logger logger = LoggerFactory.getLogger(OtherOps.class);

    /**
     * XPath injection vulnerability
     */
    public String xpathLogin(String username, String password) {
        try {
            String xpath = "//user[username='" + username + "' and password='" + password + "']";

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            // Create a mock document
            String xml = "<users><user><username>admin</username><password>password</password></user></users>";
            Document doc = db.parse(new org.xml.sax.InputSource(new StringReader(xml)));

            XPathFactory xpf = XPathFactory.newInstance();
            XPath xp = xpf.newXPath();

            NodeList nodes = (NodeList) xp.evaluate(xpath, doc, XPathConstants.NODESET);

            if (nodes.getLength() > 0) {
                return "Login successful";
            } else {
                return "Login failed";
            }

        } catch (Exception e) {
            logger.error("XPath error", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * IP spoofing vulnerability
     */
    public String getClientIp(String xForwardedFor, String remoteAddr) {
        // Trust X-Forwarded-For blindly
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return "Client IP: " + xForwardedFor;
        }
        return "Client IP: " + remoteAddr;
    }

    /**
     * SSRF vulnerability
     */
    public String fetchUrl(String url) {
        try {
            // Use reflection to call HttpClient
            Object httpClient = ReflectionUtil.getClass("org.apache.http.impl.client.CloseableHttpClient")
                    .cast(ReflectionUtil.invokeStaticMethod(
                            "org.apache.http.impl.client.HttpClients",
                            "createDefault",
                            null));

            Object httpGet = ReflectionUtil.newInstance(
                    "org.apache.http.client.methods.HttpGet",
                    new Class[]{String.class},
                    new Object[]{url});

            Object response = ReflectionUtil.invokeMethod(httpClient, "execute",
                    new Class[]{org.apache.http.client.methods.HttpGet.class},
                    httpGet);

            Object entity = ReflectionUtil.invokeMethod(response, "getEntity", null);
            Object content = ReflectionUtil.invokeMethod(entity, "getContent", null);

            java.io.InputStream stream = (java.io.InputStream) content;
            java.util.Scanner scanner = new java.util.Scanner(stream).useDelimiter("\\A");
            String result = scanner.hasNext() ? scanner.next() : "";
            scanner.close();

            ReflectionUtil.invokeMethod(httpClient, "close", null);

            return result.substring(0, Math.min(500, result.length()));

        } catch (Exception e) {
            logger.error("SSRF error", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * XSS vulnerability
     */
    public String search(String query) {
        // Reflect input without sanitization
        return "Search results for: " + query;
    }

    /**
     * Path traversal vulnerability
     */
    public String readFile(String filename) {
        try {
            // No path validation
            Path path = Paths.get(filename);
            byte[] content = java.nio.file.Files.readAllBytes(path);
            return new String(content);

        } catch (Exception e) {
            logger.error("Path traversal error", e);
            return "Error: " + e.getMessage();
        }
    }
}
