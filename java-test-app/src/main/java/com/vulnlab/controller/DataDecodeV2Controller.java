package com.vulnlab.controller;

import com.thoughtworks.xstream.XStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;
import org.yaml.snakeyaml.Yaml;

@RestController
@RequestMapping("/api/v1/data/decode/v2")
public class DataDecodeV2Controller {

    private static final Logger logger = LoggerFactory.getLogger(DataDecodeV2Controller.class);

    @PostMapping("/xml")
    public String decodeXml(@RequestBody String content) {
        logger.info("XML decode attempt, content length: {}", content.length());

        try {
            XStream xs = new XStream();
            Object obj = xs.fromXML(content);

            logger.info("XML decoded object: {}", obj.getClass().getName());
            return "XML decode completed. Object type: " + obj.getClass().getName();
        } catch (Exception e) {
            logger.error("XML decode error", e);
            return "XML decode error: " + e.getMessage();
        }
    }

    @PostMapping("/xml/sec")
    public String decodeXmlSecure(@RequestBody String content) {
        logger.info("XML secure decode attempt");

        try {
            XStream xs = new XStream();
            XStream.setupDefaultSecurity(xs);

            Object obj = xs.fromXML(content);
            return "XML secure decode completed. Object: " + obj;
        } catch (Exception e) {
            logger.error("XML secure decode error", e);
            return "XML secure decode error: " + e.getMessage();
        }
    }

    @PostMapping("/yml")
    public String decodeYml(@RequestBody String content) {
        logger.info("YML decode attempt");

        try {
            Yaml yaml = new Yaml();
            Object obj = yaml.load(content);

            return "YML decode completed. Object: " + obj;
        } catch (Exception e) {
            logger.error("YML decode error", e);
            return "YML decode error: " + e.getMessage();
        }
    }

    @PostMapping("/yml/sec")
    public String decodeYmlSecure(@RequestBody String content) {
        logger.info("YML secure decode attempt");

        try {
            Yaml yaml = new Yaml(new org.yaml.snakeyaml.constructor.SafeConstructor());
            Object obj = yaml.load(content);

            return "YML secure decode completed. Object: " + obj;
        } catch (Exception e) {
            logger.error("YML secure decode error", e);
            return "YML secure decode error: " + e.getMessage();
        }
    }

    @PostMapping("/xml-decoder")
    public String decodeXmlDecoder(@RequestBody String content) {
        logger.info("XMLDecoder attempt");

        try {
            XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(content.getBytes()));
            Object obj = decoder.readObject();
            decoder.close();

            return "XMLDecoder completed. Object: " + obj.getClass().getName();
        } catch (Exception e) {
            logger.error("XMLDecoder error", e);
            return "XMLDecoder error: " + e.getMessage();
        }
    }

    @GetMapping("/info")
    public String info() {
        return "Data Decode V2\n" +
               "Endpoints:\n" +
               "- POST /api/v1/data/decode/v2/xml\n" +
               "- POST /api/v1/data/decode/v2/yml\n" +
               "- POST /api/v1/data/decode/v2/xml-decoder\n";
    }
}
