package com.vulnlab.util;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;

/**
 * HTTP工具类 - 用于SSRF漏洞演示
 *
 * @author VulnLab
 */
public class HttpUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    /**
     * 使用URLConnection发起请求 (SSRF漏洞)
     */
    public static String fetchByURLConnection(String url) {
        try {
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String inputLine;
            StringBuilder result = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                result.append(inputLine);
            }
            in.close();
            return result.toString();

        } catch (Exception e) {
            logger.error("URLConnection error", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * 使用HttpURLConnection发起请求 (SSRF漏洞)
     */
    public static String fetchByHttpURLConnection(String url) {
        try {
            URL u = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();

            InputStream is = conn.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(is));

            String inputLine;
            StringBuilder result = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                result.append(inputLine);
            }
            in.close();
            return result.toString();

        } catch (Exception e) {
            logger.error("HttpURLConnection error", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * 使用HttpClient发起请求 (SSRF漏洞)
     */
    public static String fetchByHttpClient(String url) {
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = client.execute(httpGet);

            return EntityUtils.toString(response.getEntity());

        } catch (Exception e) {
            logger.error("HttpClient error", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * 下载文件 (SSRF漏洞)
     */
    public static byte[] downloadFile(String url) {
        try {
            URL u = new URL(url);
            InputStream is = u.openStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            return buffer.toByteArray();

        } catch (Exception e) {
            logger.error("Download error", e);
            return null;
        }
    }

    /**
     * 验证URL是否有效（安全检查）
     */
    public static boolean isValidUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        return url.startsWith("http://") || url.startsWith("https://");
    }

    /**
     * 检查URL是否为内网地址（安全检查）
     */
    public static boolean isNotInternalIp(String url) {
        try {
            URL u = new URL(url);
            String host = u.getHost();

            // 检查是否为内网IP
            if (host == null || host.isEmpty()) {
                return false;
            }

            // 检查127.0.0.1
            if (host.equals("127.0.0.1") || host.equals("localhost")) {
                return false;
            }

            // 检查0.0.0.0
            if (host.equals("0.0.0.0")) {
                return false;
            }

            // 解析IP地址
            byte[] addr = InetAddress.getByName(host).getAddress();

            // 10.0.0.0 - 10.255.255.255
            if (addr[0] == 10) {
                return false;
            }

            // 172.16.0.0 - 172.31.255.255
            if (addr[0] == (byte) 172 && addr[1] >= 16 && addr[1] <= 31) {
                return false;
            }

            // 192.168.0.0 - 192.168.255.255
            if (addr[0] == (byte) 192 && addr[1] == (byte) 168) {
                return false;
            }

            return true;

        } catch (Exception e) {
            logger.error("IP check error", e);
            return false;
        }
    }
}
