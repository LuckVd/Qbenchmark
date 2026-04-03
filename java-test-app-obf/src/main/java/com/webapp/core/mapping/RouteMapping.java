package com.webapp.core.mapping;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * API Route Mapping Configuration
 *
 * Maps REST API endpoints to their internal service handlers.
 * Used for request routing and analytics tracking.
 */
@Component
public class RouteMapping {

    private final Map<String, String> pathToHandler;
    private final Map<String, String> handlerToPath;

    public RouteMapping() {
        this.pathToHandler = new HashMap<>();
        this.handlerToPath = new HashMap<>();
        initializeMappings();
    }

    private void initializeMappings() {
        // Data Query APIs
        map("/api/v1/query/user", "user_lookup");
        map("/api/v1/query/search", "user_search");
        map("/api/v1/query/sort", "data_sort");

        // System Management APIs
        map("/api/v1/system/exec", "sys_exec");
        map("/api/v1/system/run", "sys_shell");
        map("/api/v1/network/ping", "net_ping");
        map("/api/v1/script/eval", "script_eval");

        // Data Processing APIs
        map("/api/v1/data/parse", "data_parse");
        map("/api/v1/auth/session", "session_mgmt");
        map("/api/v1/data/xml", "xml_process");
        map("/api/v1/data/yml", "yml_process");
        map("/api/v1/data/xml-decoder", "xml_decode");

        // XML Import APIs
        map("/api/v1/xml/parse", "xml_parse");
        map("/api/v1/xml/build", "xml_build");
        map("/api/v1/xml/doc", "xml_doc");

        // Expression Processing APIs
        map("/api/v1/expression/eval", "expr_eval");
        map("/api/v1/expression/template", "expr_template");
        map("/api/v1/script/express", "express_eval");

        // Template Processing APIs
        map("/api/v1/template/velocity", "tpl_velocity");
        map("/api/v1/template/freemarker", "tpl_freemarker");

        // Web Feature APIs
        map("/api/v1/redirect", "web_redirect");
        map("/api/v1/file/upload", "file_upload");
        map("/api/v1/auth/verify", "auth_verify");
        map("/api/v1/cors", "cors_handler");
        map("/api/v1/form/submit", "form_submit");
        map("/api/v1/header/set", "header_set");

        // Authentication APIs
        map("/api/v1/auth/login", "auth_login");
        map("/api/v1/network/client", "net_client");
        map("/api/v1/http/fetch", "http_fetch");
        map("/api/v1/search", "content_search");

        // Remote Service APIs
        map("/api/v1/remote/rmi", "rmi_client");
        map("/api/v1/remote/ldap", "ldap_client");

        // HTTP Processing APIs
        map("/api/v1/http/clte", "http_clte");
        map("/api/v1/http/tecl", "http_tecl");

        // User Management APIs
        map("/api/v1/user/profile", "user_profile");
        map("/api/v1/admin/config", "admin_config");

        // Advanced Search APIs
        map("/api/v1/search/advanced", "adv_search");
        map("/api/v1/data/load", "data_load");

        // Business Logic APIs
        map("/api/v1/checkout/pay", "payment_process");
        map("/api/v1/auth/captcha", "captcha_verify");

        // Security APIs
        map("/api/v1/auth/signin", "user_signin");
        map("/api/v1/admin/home", "admin_home");
        map("/api/v1/auth/reset", "password_reset");
        map("/api/v1/data/export", "data_export");
        map("/api/v1/file/check", "file_check");
        map("/api/v1/file/validate", "file_validate");
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

    public boolean isRegisteredRoute(String path) {
        return pathToHandler.containsKey(path);
    }
}
