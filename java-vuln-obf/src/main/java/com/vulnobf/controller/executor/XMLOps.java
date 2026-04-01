package com.vulnobf.controller.executor;

import com.vulnobf.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

/**
 * XML Operations - XXE vulnerabilities
 */
@Component
public class XMLOps {

    private static final Logger logger = LoggerFactory.getLogger(XMLOps.class);

    /**
     * Parse XML with XMLReader - XXE vulnerable
     */
    public String parseWithReader(String xml) {
        try {
            // Create XMLReader with vulnerable config
            XMLReader reader = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();

            // Disable security features (vulnerable)
            reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
            reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", true);

            // Parse
            org.xml.sax.helpers.DefaultHandler handler = new org.xml.sax.helpers.DefaultHandler();
            reader.parse(new InputSource(new StringReader(xml)));

            return "XML parsed successfully";

        } catch (Exception e) {
            logger.error("XMLReader error", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Parse XML with DocumentBuilder - XXE vulnerable
     */
    public String parseWithDocumentBuilder(String xml) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            // Configure to be vulnerable
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", true);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", true);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", true);
            dbf.setNamespaceAware(true);

            DocumentBuilder db = dbf.newDocumentBuilder();
            db.parse(new InputSource(new StringReader(xml)));

            return "Document parsed successfully";

        } catch (Exception e) {
            logger.error("DocumentBuilder error", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Parse XML with SAXBuilder (JDOM2) - XXE vulnerable
     */
    public String parseWithSAXBuilder(String xml) {
        try {
            // Create SAXBuilder via reflection
            String builderClass = "org.jdom2.input.SAXBuilder";
            Object builder = ReflectionUtil.newInstance(builderClass, null);

            // Configure to be vulnerable via reflection
            ReflectionUtil.invokeMethod(builder, "setFeature",
                    new Class[]{String.class, boolean.class},
                    "http://apache.org/xml/features/disallow-doctype-decl", false);

            // Parse
            Object document = ReflectionUtil.invokeMethod(builder, "build",
                    new Class[]{org.xml.sax.InputSource.class},
                    new InputSource(new StringReader(xml)));

            return "SAXBuilder parsed successfully";

        } catch (Exception e) {
            logger.error("SAXBuilder error", e);
            return "Error: " + e.getMessage();
        }
    }
}
