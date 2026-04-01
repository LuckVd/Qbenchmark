package com.vulnobf.controller.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vulnobf.util.EncodingUtil;
import com.vulnobf.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Base64;

/**
 * Data Operations - Deserialization vulnerabilities
 * Uses reflection and encoded configuration
 */
@Component
public class DataOps {

    private static final Logger logger = LoggerFactory.getLogger(DataOps.class);

    /**
     * Jackson deserialization with enableDefaultTyping
     * Uses reflection to configure the mapper
     */
    public String jacksonParse(String payload) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            // Use reflection to call enableDefaultTyping
            // This hides the dangerous API call
            Class<?>[] paramTypes = {
                Class.forName("com.fasterxml.jackson.annotation.JsonTypeInfo$Id")
            };
            Object[] args = {ReflectionUtil.getClass("com.fasterxml.jackson.annotation.JsonTypeInfo$Id")
                    .getField("CLASS").get(null)};

            ReflectionUtil.invokeMethod(mapper, "enableDefaultTyping", paramTypes, args);

            // Parse the payload
            Object obj = mapper.readValue(payload, Object.class);
            logger.debug("Jackson parsed: {}", obj.getClass().getName());

            return "Parsed object: " + obj.getClass().getName();

        } catch (Exception e) {
            logger.error("Jackson error", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Cookie deserialization vulnerability
     * Direct ObjectInputStream usage
     */
    public String deserializeCookie(String cookieValue) {
        try {
            // Decode the cookie value
            byte[] decoded = Base64.getDecoder().decode(cookieValue);

            // Create input stream
            ByteArrayInputStream bytes = new ByteArrayInputStream(decoded);

            // Use reflection to create ObjectInputStream
            ObjectInputStream in = (ObjectInputStream) ReflectionUtil.newInstance(
                    "java.io.ObjectInputStream",
                    new Class[]{java.io.InputStream.class},
                    new Object[]{bytes}
            );

            // Read object
            Object obj = in.readObject();
            in.close();

            logger.debug("Deserialized: {}", obj.getClass().getName());
            return "Deserialized: " + obj.getClass().getName();

        } catch (Exception e) {
            logger.error("Deserialization error", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * XStream deserialization
     */
    public String xStreamParse(String xml) {
        try {
            // Create XStream via reflection
            String xstreamClass = "com.thoughtworks.xstream.XStream";
            Object xstream = ReflectionUtil.newInstance(xstreamClass, null);

            // Deserialize
            Object obj = ReflectionUtil.invokeMethod(xstream, "fromXML",
                    new Class[]{String.class}, xml);

            return "XStream parsed: " + obj.getClass().getName();

        } catch (Exception e) {
            logger.error("XStream error", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * SnakeYaml deserialization
     */
    public String yamlParse(String yaml) {
        try {
            // Create Yaml via reflection
            String yamlClass = "org.yaml.snakeyaml.Yaml";
            Object yamlObj = ReflectionUtil.newInstance(yamlClass, null);

            // Deserialize
            Object obj = ReflectionUtil.invokeMethod(yamlObj, "load",
                    new Class[]{String.class}, yaml);

            return "YAML parsed: " + (obj != null ? obj.getClass().getName() : "null");

        } catch (Exception e) {
            logger.error("YAML error", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * XMLDecoder deserialization
     */
    public String xmlDecode(String xml) {
        try {
            // Create XMLDecoder via reflection
            String decoderClass = "java.beans.XMLDecoder";
            Object decoder = ReflectionUtil.newInstance(decoderClass,
                    new Class[]{java.io.InputStream.class},
                    new Object[]{new java.io.ByteArrayInputStream(xml.getBytes())});

            // Read object
            Object obj = ReflectionUtil.invokeMethod(decoder, "readObject", null);

            // Close
            ReflectionUtil.invokeMethod(decoder, "close", null);

            return "XMLDecoded: " + obj.getClass().getName();

        } catch (Exception e) {
            logger.error("XMLDecoder error", e);
            return "Error: " + e.getMessage();
        }
    }
}
