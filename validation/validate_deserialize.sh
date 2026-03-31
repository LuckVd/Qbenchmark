#!/bin/bash
# 反序列化漏洞验证脚本

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
echo -e "${BLUE}反序列化漏洞验证脚本${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

test_case() {
    local name="$1"
    local url="$2"
    TOTAL=$((TOTAL + 1))
    echo -e "\n${YELLOW}[测试 $TOTAL]${NC} $name"
    echo "URL: $url"

    response=$(curl -s "$url" -X POST 2>/dev/null)
    if [ $? -eq 0 ]; then
        echo -e "  ${GREEN}[✓] 端点响应${NC}"
        echo "  响应: $(echo "$response" | head -c 100)..."
        PASSED=$((PASSED + 1))
    else
        echo -e "  ${RED}[✗] 请求失败${NC}"
        FAILED=$((FAILED + 1))
    fi
}

echo -e "${BLUE}=== 反序列化测试 ===${NC}"

test_case "Jackson 反序列化" \
    "${BASE_URL}/deserialize/jackson" \
    ""

test_case "Fastjson 反序列化" \
    "${BASE_URL}/fastjson/deserialize" \
    ""

test_case "Shiro RememberMe" \
    "${BASE_URL}/shiro/deserialize" \
    ""

test_case "XStream 反序列化" \
    "${BASE_URL}/deserialize/xstream" \
    ""

test_case "SnakeYaml 反序列化" \
    "${BASE_URL}/deserialize/yaml" \
    ""

test_case "XMLDecoder 反序列化" \
    "${BASE_URL}/deserialize/xmldecoder" \
    ""

# 信息端点
echo -e "\n${YELLOW}[*] 反序列化信息端点:${NC}"
info=$(curl -s "${BASE_URL}/deserialize/info" 2>/dev/null)
echo "$info" | head -10

# 总结
echo -e "\n${BLUE}========================================${NC}"
echo -e "总测试数: ${YELLOW}$TOTAL${NC}"
echo -e "通过: ${GREEN}$PASSED${NC}"
echo -e "失败: ${RED}$FAILED${NC}"
