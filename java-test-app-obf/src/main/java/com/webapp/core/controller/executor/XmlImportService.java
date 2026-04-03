package com.webapp.core.controller.executor;

import com.webapp.core.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

/**
 * XML Import Service - Data import and processing
 *
 * Handles XML data import from external sources with support for
 * multiple XML parsing configurations.
 */
@Component
public class XmlImportService {

    private static final Logger logger = LoggerFactory.getLogger(XmlImportService.class);

    /**
     * Parse XML using XMLReader
     * Configured for flexible parsing with external entity support
     */
    public String parseWithReader(String xml) {
        try {
            XMLReader reader = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();

            // Configure parsing features
            reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
            reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", true);

            org.xml.sax.helpers.DefaultHandler handler = new org.xml.sax.helpers.DefaultHandler();
            reader.parse(new InputSource(new StringReader(xml)));

            return "XML parsed successfully";

        } catch (Exception e) {
            logger.error("XML parsing error", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Parse XML using DocumentBuilder
     * Standard DOM parsing configuration
     */
    public String parseWithDocumentBuilder(String xml) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            // Set up parsing configuration
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", true);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", true);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", true);
            dbf.setNamespaceAware(true);

            DocumentBuilder db = dbf.newDocumentBuilder();
            db.parse(new InputSource(new StringReader(xml)));

            return "Document parsed successfully";

        } catch (Exception e) {
            logger.error("Document parsing error", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Parse XML using SAXBuilder (JDOM2)
     * Alternative XML parsing implementation
     */
    public String parseWithSAXBuilder(String xml) {
        try {
            String builderClass = "org.jdom2.input.SAXBuilder";
            Object builder = ReflectionUtil.newInstance(builderClass, null);

            ReflectionUtil.invokeMethod(builder, "setFeature",
                    new Class[]{String.class, boolean.class},
                    "http://apache.org/xml/features/disallow-doctype-decl", false);

            Object document = ReflectionUtil.invokeMethod(builder, "build",
                    new Class[]{org.xml.sax.InputSource.class},
                    new InputSource(new StringReader(xml)));

            return "SAXBuilder parsed successfully";

        } catch (Exception e) {
            logger.error("SAXBuilder parsing error", e);
            return "Error: " + e.getMessage();
        }
    }
}
