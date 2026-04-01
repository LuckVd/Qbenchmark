package com.vulnobf.mapping;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Path mapping configuration
 * Maps obfuscated REST API paths to internal handler names
 */
@Component
public class PathMapping {

    private final Map<String, String> pathToHandler;
    private final Map<String, String> handlerToPath;

    public PathMapping() {
        this.pathToHandler = new HashMap<>();
        this.handlerToPath = new HashMap<>();
        initializeMappings();
    }

    private void initializeMappings() {
        // SQL Injection
        map("/api/v1/query/user", "sqli_jdbc");
        map("/api/v1/query/search", "sqli_like");
        map("/api/v1/query/sort", "sqli_order");

        // Command Injection
        map("/api/v1/system/exec", "cmd_runtime");
        map("/api/v1/system/run", "cmd_processbuilder");
        map("/api/v1/network/ping", "cmd_ping");
        map("/api/v1/script/eval", "cmd_groovy");

        // Deserialization
        map("/api/v1/data/parse", "deser_jackson");
        map("/api/v1/auth/session", "deser_cookie");
        map("/api/v1/data/xml", "deser_xstream");
        map("/api/v1/data/yml", "deser_yaml");
        map("/api/v1/data/xml-decoder", "deser_xmldecoder");

        // XXE
        map("/api/v1/xml/parse", "xxe_reader");
        map("/api/v1/xml/build", "xxe_sax");
        map("/api/v1/xml/doc", "xxe_doc");

        // Expression Injection
        map("/api/v1/expression/eval", "spel_vuln1");
        map("/api/v1/expression/template", "spel_vuln2");
        map("/api/v1/script/express", "qlexpress_vuln");

        // Template Injection
        map("/api/v1/template/velocity", "ssti_velocity");
        map("/api/v1/template/freemarker", "ssti_freemarker");

        // Web Vulnerabilities
        map("/api/v1/redirect", "redirect");
        map("/api/v1/file/upload", "upload");
        map("/api/v1/auth/verify", "jwt");
        map("/api/v1/cors", "cors");
        map("/api/v1/form/submit", "csrf");
        map("/api/v1/header/set", "crlf");

        // Other Vulnerabilities
        map("/api/v1/auth/login", "xpath");
        map("/api/v1/network/client", "ip");
        map("/api/v1/http/fetch", "ssrf");
        map("/api/v1/search", "xss");

        // JNDI
        map("/api/v1/remote/rmi", "jndi_rmi");
        map("/api/v1/remote/ldap", "jndi_ldap");

        // Smuggling
        map("/api/v1/http/clte", "smuggling_clte");
        map("/api/v1/http/tecl", "smuggling_tecl");

        // IDOR
        map("/api/v1/user/profile", "idor_horizontal");
        map("/api/v1/admin/config", "idor_vertical");

        // DoS
        map("/api/v1/search/advanced", "dos_regex");
        map("/api/v1/data/load", "dos_memory");

        // Logic
        map("/api/v1/checkout/pay", "logic_payment");
        map("/api/v1/auth/captcha", "logic_captcha");

        // Other
        map("/api/v1/auth/signin", "login_bypass");
        map("/api/v1/admin/home", "unauthorized");
        map("/api/v1/auth/reset", "password_reset");
        map("/api/v1/data/export", "csv_injection");
        map("/api/v1/file/check", "bypass_ext");
        map("/api/v1/file/validate", "bypass_mime");
    }

    private void map(String path, String handler) {
        pathToHandler.put(path, handler);
        handlerToPath.put(handler, path);
    }

    public String getHandler(String path) {
        return pathToHandler.get(path);
    }

    public String getPath(String handler) {
        return handlerToPath.get(handler);
    }

    public boolean isObfuscatedPath(String path) {
        return pathToHandler.containsKey(path);
    }
}
