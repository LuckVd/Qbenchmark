#!/bin/bash
# Validation script for Obfuscated Vulnerability Lab
# Tests that obfuscated vulnerabilities still work

BASE_URL="http://localhost:8081"
PASS=0
FAIL=0

echo "========================================="
echo "Obfuscated Vulnerability Lab Validation"
echo "========================================="
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

test_endpoint() {
    local name="$1"
    local url="$2"
    local expected="$3"

    echo -n "Testing $name... "

    response=$(curl -s "$url" 2>/dev/null)

    if echo "$response" | grep -q "$expected"; then
        echo -e "${GREEN}PASS${NC}"
        ((PASS++))
        return 0
    else
        echo -e "${RED}FAIL${NC} (expected: $expected, got: $response)"
        ((FAIL++))
        return 1
    fi
}

# 1. SQL Injection - Path obfuscation
echo "=== SQL Injection ==="
test_endpoint "SQLi Path Obfuscation" \
    "$BASE_URL/api/v1/query/user?name=test" \
    "SELECT"

# 2. Command Injection - Reflection
echo ""
echo "=== Command Injection ==="
test_endpoint "Command Exec Reflection" \
    "$BASE_URL/api/v1/system/exec?arg=/etc/passwd" \
    "Error\|ls:"

# 3. Expression Injection - SpEL
echo ""
echo "=== Expression Injection ==="
test_endpoint "SpEL Expression" \
    "$BASE_URL/api/v1/expression/eval?expr=2%2B2" \
    "Result: 4"

test_endpoint "SpEL RCE (System.getProperty)" \
    "$BASE_URL/api/v1/expression/eval?expr=T(java.lang.System).getProperty('os.name')" \
    "Result:"

# 4. Deserialization - Jackson (POST)
echo ""
echo "=== Deserialization ==="
response=$(curl -s -X POST "$BASE_URL/api/v1/data/parse" -H "Content-Type: application/json" -d '{"test":"value"}' 2>/dev/null)
echo -n "Testing Jackson Parse... "
if echo "$response" | grep -q "Error\|Parsed\|object"; then
    echo -e "${GREEN}PASS${NC}"
    ((PASS++))
else
    echo -e "${RED}FAIL${NC}"
    ((FAIL++))
fi

# 5. XXE - XML Parser (POST)
echo ""
echo "=== XXE ==="
response=$(curl -s -X POST "$BASE_URL/api/v1/xml/parse" -H "Content-Type: application/xml" -d '<?xml version="1.0"?><test>value</test>' 2>/dev/null)
echo -n "Testing XML Parse... "
if echo "$response" | grep -q "Error\|parsed\|success"; then
    echo -e "${GREEN}PASS${NC}"
    ((PASS++))
else
    echo -e "${RED}FAIL${NC}"
    ((FAIL++))
fi

# 6. Info Endpoint
echo ""
echo "=== Info ==="
test_endpoint "Lab Info" \
    "$BASE_URL/api/v1/info" \
    "Obfuscated"

# Summary
echo ""
echo "========================================="
echo "Summary:"
echo -e "  ${GREEN}Passed: $PASS${NC}"
echo -e "  ${RED}Failed: $FAIL${NC}"
echo "========================================="

if [ $FAIL -eq 0 ]; then
    echo -e "${GREEN}All tests passed!${NC}"
    exit 0
else
    echo -e "${RED}Some tests failed!${NC}"
    exit 1
fi
