package com.vulnobf.controller.facade;

import com.vulnobf.controller.handler.DataHandler;
import com.vulnobf.mapping.PathMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Data Controller - Deserialization endpoints
 * REST API facade for data operations
 */
@RestController
@RequestMapping("/api/v1")
public class DataController {

    private static final Logger logger = LoggerFactory.getLogger(DataController.class);

    @Autowired
    private DataHandler dataHandler;

    @Autowired
    private PathMapping pathMapping;

    /**
     * Parse data endpoint - Jackson deserialization
     * Path: /api/v1/data/parse
     * Original: /deserialize/jackson
     */
    @PostMapping("/data/parse")
    public String parseData(@RequestBody String data) {
        logger.info("Parse data request");
        return dataHandler.handleParse(data);
    }

    /**
     * Session endpoint - Cookie deserialization
     * Path: /api/v1/auth/session
     * Original: /deserialize/rememberMe
     */
    @GetMapping("/auth/session")
    public String session(@CookieValue(value = "session", required = false) String session) {
        logger.info("Session check");
        return dataHandler.handleSession(session);
    }

    /**
     * XML parse endpoint - XStream deserialization
     * Path: /api/v1/data/xml
     * Original: /deserialize/xstream
     */
    @PostMapping("/data/xml")
    public String parseXml(@RequestBody String xml) {
        logger.info("XML parse request");
        return dataHandler.handleXml(xml);
    }

    /**
     * YAML parse endpoint - SnakeYaml deserialization
     * Path: /api/v1/data/yml
     * Original: /deserialize/yaml
     */
    @PostMapping("/data/yml")
    public String parseYaml(@RequestBody String yaml) {
        logger.info("YAML parse request");
        return dataHandler.handleYaml(yaml);
    }

    /**
     * XML decode endpoint - XMLDecoder deserialization
     * Path: /api/v1/data/xml-decoder
     * Original: /deserialize/xmldecoder
     */
    @PostMapping("/data/xml-decoder")
    public String decodeXml(@RequestBody String xml) {
        logger.info("XML decode request");
        return dataHandler.handleDecode(xml);
    }

    /**
     * Data operations info
     */
    @GetMapping("/data/info")
    public String dataInfo() {
        return "Data operations:\n" +
               "- parse: Parse JSON (Jackson)\n" +
               "- session: Session management\n" +
               "- xml: Parse XML (XStream)\n" +
               "- yml: Parse YAML (SnakeYaml)\n" +
               "- xml-decoder: Decode XML (XMLDecoder)";
    }
}
