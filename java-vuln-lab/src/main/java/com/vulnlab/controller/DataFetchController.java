package com.vulnlab.controller;

import com.vulnlab.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/data")
public class DataFetchController {

    private static final Logger logger = LoggerFactory.getLogger(DataFetchController.class);

    @GetMapping("/fetch")
    public String fetch(@RequestParam("url") String url) {
        logger.info("Fetch URL: {}", url);
        return HttpUtil.fetchByURLConnection(url);
    }

    @GetMapping("/fetch/http")
    public String fetchHttp(@RequestParam("url") String url) {
        logger.info("Fetch HTTP URL: {}", url);
        return HttpUtil.fetchByHttpURLConnection(url);
    }

    @GetMapping("/fetch/client")
    public String fetchClient(@RequestParam("url") String url) {
        logger.info("Fetch client URL: {}", url);
        return HttpUtil.fetchByHttpClient(url);
    }

    @GetMapping("/download")
    public String download(@RequestParam("url") String url) {
        logger.info("Download URL: {}", url);
        try {
            byte[] data = HttpUtil.downloadFile(url);
            if (data != null) {
                return java.util.Base64.getEncoder().encodeToString(data);
            }
            return "Download failed";
        } catch (Exception e) {
            logger.error("Download error", e);
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/fetch/sec")
    public String fetchSecure(@RequestParam("url") String url) {
        logger.info("Fetch URL (secure): {}", url);

        if (!HttpUtil.isValidUrl(url)) {
            return "Invalid URL. Only HTTP/HTTPS protocols are allowed.";
        }

        if (!HttpUtil.isNotInternalIp(url)) {
            return "Access to internal IP addresses is not allowed.";
        }

        return HttpUtil.fetchByURLConnection(url);
    }
}
