package com.webapp.core.controller.executor;

import com.webapp.core.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Advanced Service - Advanced system operations
 */
@Component
public class AdvancedService {

    private static final Logger logger = LoggerFactory.getLogger(AdvancedService.class);

    public String getClientInfo(HttpServletRequest request) {
        return "Client IP: " + request.getRemoteAddr() + "\n" +
               "User-Agent: " + request.getHeader("User-Agent");
    }

    public String fetchUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            java.lang.StringBuilder result = new java.lang.StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();

            return result.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String search(String query) {
        return "Search results for: " + query;
    }

    public String connectRmi(String endpoint) {
        try {
            String rmiClass = "java.rmi.registry.LocateRegistry";
            Object registry = ReflectionUtil.invokeStaticMethod(rmiClass, "getRegistry",
                    new Class[]{int.class}, 1099);
            return "RMI connected to: " + endpoint;
        } catch (Exception e) {
            return "RMI Error: " + e.getMessage();
        }
    }

    public String connectLdap(String endpoint) {
        try {
            String ldapClass = "javax.naming.directory.InitialDirContext";
            Object context = ReflectionUtil.newInstance(ldapClass, null);
            return "LDAP connected to: " + endpoint;
        } catch (Exception e) {
            return "LDAP Error: " + e.getMessage();
        }
    }

    public String handleClte(String data) {
        return "CLTE processed: " + data.length() + " bytes";
    }

    public String handleTecl(String data) {
        return "TECL processed: " + data.length() + " bytes";
    }

    public String getUserProfile(String id) {
        return "User Profile ID: " + id;
    }

    public String getUserProfileByName(String name) {
        return "User Profile: " + name;
    }

    public String getAdminConfig() {
        return "Admin Config:\n- debug: true\n- allowAll: true";
    }
}
