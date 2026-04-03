package com.webapp.core.controller.facade;

import com.webapp.core.controller.handler.DataHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Data Controller - Data processing and import APIs
 */
@RestController
@RequestMapping("/api/v1")
public class DataController {

    private static final Logger logger = LoggerFactory.getLogger(DataController.class);

    @Autowired
    private DataHandler dataHandler;

    @PostMapping("/data/parse")
    public String parseData(@RequestBody String data) {
        return dataHandler.handleParse(data);
    }

    @GetMapping("/data/fetch")
    public String fetchData(@RequestParam("url") String url) {
        return dataHandler.handleFetch(url);
    }

    @PostMapping("/data/xml")
    public String processXml(@RequestBody String xml) {
        return dataHandler.handleXml(xml);
    }

    @PostMapping("/data/yml")
    public String processYml(@RequestBody String yml) {
        return dataHandler.handleYml(yml);
    }
}
