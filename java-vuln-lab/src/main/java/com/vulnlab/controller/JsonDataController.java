package com.vulnlab.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/json/data")
public class JsonDataController {

    private static final Logger logger = LoggerFactory.getLogger(JsonDataController.class);

    @PostMapping("/decode")
    public String decode(@RequestBody String params) {
        logger.info("JSON decode request: {}", params);

        try {
            JSONObject obj = JSON.parseObject(params, Feature.SupportNonPublicField);
            logger.info("JSON decode result: {}", obj);

            if (obj != null && obj.containsKey("name")) {
                return "JSON decode completed. Name: " + obj.get("name");
            }
            return "JSON decode completed. Object: " + obj.getClass().getName();
        } catch (Exception e) {
            logger.error("JSON decode error", e);
            return "JSON decode error: " + e.getMessage();
        }
    }

    @PostMapping("/parse")
    public String parse(@RequestBody String params) {
        logger.info("JSON parse request: {}", params);

        try {
            Object obj = JSON.parse(params);
            logger.info("JSON parse result: {}", obj);
            return "Parsed object: " + obj.getClass().getName() + " -> " + obj.toString();
        } catch (Exception e) {
            logger.error("JSON parse error", e);
            return "Parse error: " + e.getMessage();
        }
    }

    @PostMapping("/return")
    public String decodeWithReturn(@RequestBody String params) {
        logger.info("JSON decode request: {}", params);

        try {
            JSONObject ob = JSON.parseObject(params);
            if (ob != null && ob.containsKey("name")) {
                return ob.get("name").toString();
            }
            return "JSON decode completed";
        } catch (Exception e) {
            logger.error("JSON decode error", e);
            return e.toString();
        }
    }
}
