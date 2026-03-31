package com.vulnlab.controller;

import com.vulnlab.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * SSRF (服务端请求伪造) 漏洞演示
 *
 * 漏洞说明：
 * - 应用程序根据用户输入的URL发起请求
 * - 未对URL进行有效验证
 * - 攻击者可利用此漏洞读取内网资源、探测内网端口等
 *
 * 修复方案：
 * 1. 使用URL白名单
 * 2. 禁止访问内网IP（10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16, 127.0.0.0/8）
 * 3. 禁止访问file://、ftp://等非http协议
 *
 * @author VulnLab
 */
@RestController
@RequestMapping("/ssrf")
public class SSRFController {

    private static final Logger logger = LoggerFactory.getLogger(SSRFController.class);

    /**
     * SSRF漏洞 - URLConnection方式
     *
     * 测试用例：
     * - 读取文件: http://localhost:8080/ssrf/urlconnection/vuln?url=file:///etc/passwd
     * - 内网探测: http://localhost:8080/ssrf/urlconnection/vuln?url=http://127.0.0.1:22
     * - 正常请求: http://localhost:8080/ssrf/urlconnection/vuln?url=http://www.baidu.com
     *
     * @param url 目标URL
     * @return 响应内容
     */
    @GetMapping("/urlconnection/vuln")
    public String urlConnectionVuln(@RequestParam("url") String url) {
        logger.info("SSRF URL: {}", url);
        return HttpUtil.fetchByURLConnection(url);
    }

    /**
     * SSRF漏洞 - HttpURLConnection方式
     *
     * 测试用例：
     * - http://localhost:8080/ssrf/httpurl/vuln?url=file:///etc/passwd
     *
     * @param url 目标URL
     * @return 响应内容
     */
    @GetMapping("/httpurl/vuln")
    public String httpUrlVuln(@RequestParam("url") String url) {
        logger.info("SSRF URL: {}", url);
        return HttpUtil.fetchByHttpURLConnection(url);
    }

    /**
     * SSRF漏洞 - HttpClient方式
     *
     * 测试用例：
     * - http://localhost:8080/ssrf/httpclient/vuln?url=http://127.0.0.1:8080
     *
     * @param url 目标URL
     * @return 响应内容
     */
    @GetMapping("/httpclient/vuln")
    public String httpClientVuln(@RequestParam("url") String url) {
        logger.info("SSRF URL: {}", url);
        return HttpUtil.fetchByHttpClient(url);
    }

    /**
     * SSRF漏洞 - 下载远程文件
     *
     * 测试用例：
     * - http://localhost:8080/ssrf/download/vuln?url=file:///etc/passwd
     *
     * @param url 目标URL
     * @return 文件内容(Base64编码)
     */
    @GetMapping("/download/vuln")
    public String downloadVuln(@RequestParam("url") String url) {
        logger.info("SSRF Download URL: {}", url);
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

    /**
     * SSRF安全代码 - URL白名单验证
     *
     * 测试用例：
     * - 正常: http://localhost:8080/ssrf/urlconnection/sec?url=http://www.baidu.com
     * - 被拒绝: http://localhost:8080/ssrf/urlconnection/sec?url=http://192.168.1.1
     *
     * @param url 目标URL
     * @return 响应内容或错误信息
     */
    @GetMapping("/urlconnection/sec")
    public String urlConnectionSecure(@RequestParam("url") String url) {
        logger.info("SSRF URL (secure): {}", url);

        // 安全验证
        if (!HttpUtil.isValidUrl(url)) {
            return "Invalid URL. Only HTTP/HTTPS protocols are allowed.";
        }

        if (!HttpUtil.isNotInternalIp(url)) {
            return "Access to internal IP addresses is not allowed.";
        }

        return HttpUtil.fetchByURLConnection(url);
    }
}
