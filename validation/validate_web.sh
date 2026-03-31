#!/bin/bash
# Web 安全漏洞验证脚本

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
echo -e "${BLUE}Web 安全漏洞验证脚本${NC}"
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

echo -e "${BLUE}=== Web 安全测试 ===${NC}"

echo -e "\n${BLUE}--- URL 重定向 ---${NC}"
test_case "开放重定向" \
    "${BASE_URL}/urlRedirect/redirect?url=http://evil.com" \
    ""
test_case "Header 重定向" \
    "${BASE_URL}/urlRedirect/header?url=http://evil.com" \
    ""

echo -e "\n${BLUE}--- 文件上传 ---${NC}"
test_case "WebShell 上传" \
    "${BASE_URL}/file/upload" \
    ""

echo -e "\n${BLUE}--- JWT 漏洞 ---${NC}"
test_case "None 算法" \
    "${BASE_URL}/jwt/none?token=eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJhZG1pbiJ9" \
    ""

echo -e "\n${BLUE}--- CORS/CSRF ---${NC}"
test_case "CORS 漏洞" \
    "${BASE_URL}/cors/vuln" \
    ""

# 信息端点
echo -e "\n${YELLOW}[*] Web 安全信息端点:${NC}"
info=$(curl -s "${BASE_URL}/urlRedirect/info" 2>/dev/null)
echo "$info" | head -10

# 总结
echo -e "\n${BLUE}========================================${NC}"
echo -e "总测试数: ${YELLOW}$TOTAL${NC}"
echo -e "通过: ${GREEN}$PASSED${NC}"
echo -e "失败: ${RED}$FAILED${NC}"
