package com.webapp.core.controller.handler;

import com.webapp.core.controller.executor.DataOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Data Handler - Data processing operations
 */
@Component
public class DataHandler {

    private static final Logger logger = LoggerFactory.getLogger(DataHandler.class);

    @Autowired
    private DataOps dataOps;

    public String handleParse(String data) {
        return dataOps.parseJson(data);
    }

    public String handleFetch(String url) {
        return dataOps.fetchUrl(url);
    }

    public String handleXml(String xml) {
        return dataOps.processXml(xml);
    }

    public String handleYml(String yml) {
        return dataOps.processYml(yml);
    }
}
