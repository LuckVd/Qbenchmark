#!/usr/bin/env python3
"""
恶意 LDAP Server - 用于 JNDI 注入漏洞验证

警告：此代码仅用于安全测试和教育目的

依赖安装：
pip install ldap3

使用方法：
python3 evil_ldap_server.py

测试：
http://localhost:8080/jndi/ldap/vuln?url=ldap://localhost:1389/Exploit

参考：https://github.com/sportradar/python-ldap-server
"""

from ldap3 import Server, Connection, ALL, MODIFY_ADD
from ldap3.server import Server
from ldap3.protocol.ldap import LDAPBindRequest
import socket
import threading
import time

class EvilLDAPServer:
    """简化的恶意 LDAP 服务器"""

    def __init__(self, host='0.0.0.0', port=1389):
        self.host = host
        self.port = port
        self.running = False

    def start(self):
        """启动 LDAP 服务器"""
        self.running = True

        print("=" * 40)
        print("[*] 恶意 LDAP Server 已启动")
        print(f"[*] 监听端口: {self.port}")
        print(f"[*] 监听地址: {self.host}")
        print(f"[*] 测试 URL: ldap://localhost:{self.port}/Exploit")
        print("=" * 40)
        print("[!] 等待 JNDI 连接...")
        print()

        # 创建监听 socket
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            s.bind((self.host, self.port))
            s.listen(5)

            while self.running:
                try:
                    s.settimeout(1)
                    conn, addr = s.accept()
                    print(f"[*] 收到连接: {addr[0]}:{addr[1]}")
                    print("[!] JNDI 注入尝试检测！")

                    # 读取请求数据
                    data = conn.recv(1024)
                    if data:
                        print(f"[*] 请求数据: {data.hex()}")

                    conn.close()
                except socket.timeout:
                    continue
                except Exception as e:
                    print(f"[!] 错误: {e}")
                    continue

    def stop(self):
        """停止服务器"""
        self.running = False


if __name__ == '__main__':
    server = EvilLDAPServer()
    try:
        server.start()
    except KeyboardInterrupt:
        print("\n[!] 服务器已停止")
