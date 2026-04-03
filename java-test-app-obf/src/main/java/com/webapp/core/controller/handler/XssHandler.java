package com.webapp.core.controller.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Content Handler - Search and content rendering
 */
@Component
public class XssHandler {

    private static final Logger logger = LoggerFactory.getLogger(XssHandler.class);
    private static final Map<String, String> storage = new HashMap<>();
    private static int counter = 0;

    public String handleSearch(String query) {
        // 直接反射用户输入，不进行HTML转义
        return "<html><body>" +
               "<h1>Search Results</h1>" +
               "<p>Your search: " + query + "</p>" +
               "<p>No results found for: " + query + "</p>" +
               "</body></html>";
    }

    public String handleRender(String html) {
        // 直接渲染用户输入的HTML
        return "<html><body>" +
               "<div id=\"content\">" + html + "</div>" +
               "</body></html>";
    }

    public String handleStore(String data) {
        counter++;
        String id = "store_" + counter;
        storage.put(id, data);
        return "Stored with ID: " + id;
    }

    public String handleShow() {
        java.lang.StringBuilder result = new java.lang.StringBuilder();
        result.append("<html><body><h1>Stored Content</h1>");
        for (Map.Entry<String, String> entry : storage.entrySet()) {
            result.append("<div id=\"").append(entry.getKey()).append("\">")
                  .append(entry.getValue())
                  .append("</div>");
        }
        result.append("</body></html>");
        return result.toString();
    }
}
