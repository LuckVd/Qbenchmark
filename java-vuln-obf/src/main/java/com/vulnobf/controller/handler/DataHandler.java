package com.vulnobf.controller.handler;

import com.vulnobf.controller.executor.DataOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Data Handler - Data processing layer
 */
@Component
public class DataHandler {

    private static final Logger logger = LoggerFactory.getLogger(DataHandler.class);

    @Autowired
    private DataOps dataOps;

    /**
     * Handle data parsing request
     */
    public String handleParse(String data) {
        logger.debug("Parse request, length: {}", data != null ? data.length() : 0);

        // "Validate" the data
        if (data == null || data.isEmpty()) {
            return "Empty data";
        }

        // Pass to executor
        return dataOps.jacksonParse(data);
    }

    /**
     * Handle session cookie
     */
    public String handleSession(String cookie) {
        logger.debug("Session cookie, length: {}", cookie != null ? cookie.length() : 0);

        if (cookie == null || cookie.isEmpty()) {
            return "No session";
        }

        return dataOps.deserializeCookie(cookie);
    }

    /**
     * Handle XML data
     */
    public String handleXml(String xml) {
        logger.debug("XML request, length: {}", xml != null ? xml.length() : 0);

        if (xml == null || xml.isEmpty()) {
            return "Empty XML";
        }

        return dataOps.xStreamParse(xml);
    }

    /**
     * Handle YAML data
     */
    public String handleYaml(String yaml) {
        logger.debug("YAML request, length: {}", yaml != null ? yaml.length() : 0);

        if (yaml == null || yaml.isEmpty()) {
            return "Empty YAML";
        }

        return dataOps.yamlParse(yaml);
    }

    /**
     * Handle XML decoder request
     */
    public String handleDecode(String xml) {
        logger.debug("Decode request, length: {}", xml != null ? xml.length() : 0);

        if (xml == null || xml.isEmpty()) {
            return "Empty data";
        }

        return dataOps.xmlDecode(xml);
    }
}
