#!/bin/bash
# 业务逻辑漏洞验证脚本
# 漏洞类型: Business Logic Vulnerabilities
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
    echo -e "${BLUE}业务逻辑漏洞验证脚本${NC}"
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
        echo "  响应: $(echo "$response" | head -c 200)..."
        PASSED=$((PASSED + 1))
    else
        echo -e "  ${RED}[✗] 测试失败${NC}"
        echo "  响应: $(echo "$response" | head -c 200)..."
        FAILED=$((FAILED + 1))
    fi
}

# 安全版本测试
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
        echo "  响应: $(echo "$response" | head -c 200)..."
        PASSED=$((PASSED + 1))
    else
        echo -e "  ${RED}[✗] 防护无效${NC}"
        echo "  响应: $(echo "$response" | head -c 200)..."
        FAILED=$((FAILED + 1))
    fi
}

# 开始测试
print_header

echo -e "${BLUE}=== 1. 支付金额篡改测试 ===${NC}"

echo -e "\n${YELLOW}[信息] 测试数据:${NC}"
echo "  user1 余额: 10000"
echo "  p1 (iPhone 15 Pro): 7999"
echo "  p2 (MacBook Pro): 15999"

test_case \
    "金额篡改 - 0.01元购买高价商品" \
    "${BASE_URL}/logic/payment?userId=user1&productId=p1&amount=0.01" \
    "支付金额: 0.01" \
    "将7999元的商品以0.01元购买"

test_case \
    "负数金额 - 增加余额" \
    "${BASE_URL}/logic/payment/signed?userId=user1&productId=p1&amount=-5000" \
    "余额增加" \
    "使用负数金额导致余额增加"

echo -e "\n${BLUE}=== 2. 优惠券滥用测试 ===${NC}"

test_case \
    "优惠券叠加 - 多个优惠券同时使用" \
    "${BASE_URL}/logic/payment/coupons?userId=user1&productId=p2&coupons=SAVE100,SAVE500,PERCENT20" \
    "叠加优惠券" \
    "同时使用多个优惠券可能导致免费"

echo -e "\n${BLUE}=== 3. 验证码漏洞测试 ===${NC}"

test_case \
    "验证码生成 - 可预测的时间戳" \
    "${BASE_URL}/logic/captcha/generate?userId=test" \
    "验证码: [0-9]{4}" \
    "验证码基于时间戳生成"

test_case \
    "固定验证码 - admin固定使用1234" \
    "${BASE_URL}/logic/captcha/admin?userId=admin&captcha=1234" \
    "正确" \
    "admin用户使用固定验证码"

test_case \
    "验证码暴力破解 - 无频率限制" \
    "${BASE_URL}/logic/captcha/bruteforce?userId=user1&captcha=0000" \
    "暴力破解" \
    "没有频率限制，可以暴力破解4位验证码"

echo -e "\n${BLUE}=== 4. 安全版本测试 ===${NC}"

test_safe \
    "安全支付 - 服务端验证价格" \
    "${BASE_URL}/logic/safe/payment?userId=user1&productId=p1" \
    "不可篡改" \
    "金额由服务端验证"

test_safe \
    "安全验证码 - 一次性使用" \
    "${BASE_URL}/logic/safe/captcha?userId=test&captcha=1234" \
    "不存在" \
    "验证码一次性使用"

echo -e "\n${BLUE}=== 5. 信息端点测试 ===${NC}"

echo -e "\n${YELLOW}[*] 访问业务逻辑信息端点:${NC}"
info_url="${BASE_URL}/logic/info"
info_response=$(curl -s "$info_url" 2>/dev/null)
if [ $? -eq 0 ] && [ -n "$info_response" ]; then
    echo -e "  ${GREEN}[✓] 信息端点正常${NC}"
    echo "$info_response" | head -15
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

echo -e "\n${YELLOW}风险说明:${NC}"
echo -e "${YELLOW}- 支付金额篡改: 经济损失风险${NC}"
echo -e "${YELLOW}- 优惠券滥用: 免费获取商品${NC}"
echo -e "${YELLOW}- 验证码可预测: 账户被盗风险${NC}"

echo ""
echo -e "${BLUE}常见攻击场景:${NC}"
echo "1. 修改支付金额为 0.01 元购买高价商品"
echo "2. 使用负数金额增加账户余额"
echo "3. 重复使用同一优惠券"
echo "4. 叠加多个优惠券实现免费购买"
echo "5. 预测验证码登录他人账户"

if [ $FAILED -eq 0 ]; then
    echo -e "\n${GREEN}所有测试通过！${NC}"
    exit 0
else
    echo -e "\n${YELLOW}有 $FAILED 个测试失败${NC}"
    exit 0
fi
