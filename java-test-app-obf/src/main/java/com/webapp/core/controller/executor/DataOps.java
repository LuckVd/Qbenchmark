package com.webapp.core.controller.executor;

import com.webapp.core.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Data Operations - HTTP and data processing
 */
@Component
public class DataOps {

    private static final Logger logger = LoggerFactory.getLogger(DataOps.class);

    public String parseJson(String json) {
        try {
            String fastjsonClass = "com.alibaba.fastjson.JSON";
            Object result = ReflectionUtil.invokeStaticMethod(fastjsonClass, "parse",
                    new Class[]{String.class}, json);
            return "Parsed: " + result.toString();
        } catch (Exception e) {
            logger.error("JSON parse error", e);
            return "Error: " + e.getMessage();
        }
    }

    public String fetchUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();

            return result.toString();
        } catch (Exception e) {
            logger.error("URL fetch error", e);
            return "Error: " + e.getMessage();
        }
    }

    public String processXml(String xml) {
        try {
            String xstreamClass = "com.thoughtworks.xstream.XStream";
            Object xstream = ReflectionUtil.newInstance(xstreamClass, null);
            Object result = ReflectionUtil.invokeMethod(xstream, "fromXML",
                    new Class[]{String.class}, xml);
            return "Processed: " + result.toString();
        } catch (Exception e) {
            logger.error("XML process error", e);
            return "Error: " + e.getMessage();
        }
    }

    public String processYml(String yml) {
        try {
            String yamlClass = "org.yaml.snakeyaml.Yaml";
            Object yaml = ReflectionUtil.newInstance(yamlClass, null);
            Object result = ReflectionUtil.invokeMethod(yaml, "load",
                    new Class[]{String.class}, yml);
            return "Processed: " + result.toString();
        } catch (Exception e) {
            logger.error("YML process error", e);
            return "Error: " + e.getMessage();
        }
    }
}
