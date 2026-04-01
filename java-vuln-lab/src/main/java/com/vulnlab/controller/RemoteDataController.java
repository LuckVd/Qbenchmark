package com.vulnlab.controller;

import org.springframework.web.bind.annotation.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

@RestController
@RequestMapping("/api/v1/remote")
public class RemoteDataController {

    @GetMapping("/rmi")
    public String rmi(@RequestParam("url") String url) {
        try {
            Context ctx = new InitialContext();
            Object lookup = ctx.lookup(url);
            return "RMI lookup result: " + lookup;
        } catch (NamingException e) {
            return "RMI lookup failed: " + e.getMessage();
        }
    }

    @GetMapping("/config")
    public String config(@RequestParam("config") String config) {
        try {
            Context ctx = new InitialContext();
            Object remoteConfig = ctx.lookup(config);
            return "Config loaded from: " + config + ", value: " + remoteConfig;
        } catch (NamingException e) {
            return "Failed to load config: " + e.getMessage();
        }
    }

    @GetMapping("/datasource")
    public String datasource(@RequestParam("name") String name) {
        try {
            Context ctx = new InitialContext();
            Object ds = ctx.lookup(name);
            return "DataSource found: " + ds;
        } catch (NamingException e) {
            return "DataSource not found: " + e.getMessage();
        }
    }

    @GetMapping("/ldap")
    public String ldap(@RequestParam("url") String url) {
        try {
            Context ctx = new InitialContext();
            Object lookup = ctx.lookup(url);
            return "LDAP lookup result: " + lookup;
        } catch (NamingException e) {
            return "LDAP lookup failed: " + e.getMessage();
        }
    }

    @GetMapping("/auth")
    public String auth(@RequestParam("url") String url) {
        try {
            Context ctx = new InitialContext();
            Object lookup = ctx.lookup(url);
            return "Auth lookup result: " + lookup;
        } catch (NamingException e) {
            return "Auth lookup failed: " + e.getMessage();
        }
    }

    @GetMapping("/info")
    public String info() {
        return "Remote Data Access\n" +
               "Endpoints:\n" +
               "- GET /api/v1/remote/rmi?url=<rmi_url>\n" +
               "- GET /api/v1/remote/ldap?url=<ldap_url>\n" +
               "- GET /api/v1/remote/config?config=<jndi_url>\n";
    }
}
