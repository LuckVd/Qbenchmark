#!/bin/bash
# Template render validation script

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
echo -e "${BLUE}Template Render Validation Script${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

test_case() {
    local name="$1"
    local url="$2"
    TOTAL=$((TOTAL + 1))
    echo -e "\n${YELLOW}[Test $TOTAL]${NC} $name"
    echo "URL: $url"

    response=$(curl -s "$url" 2>/dev/null)
    if [ $? -eq 0 ]; then
        echo -e "  ${GREEN}[✓] Endpoint responded${NC}"
        echo "  Response: $(echo "$response" | head -c 100)..."
        PASSED=$((PASSED + 1))
    else
        echo -e "  ${RED}[✗] Request failed${NC}"
        FAILED=$((FAILED + 1))
    fi
}

echo -e "${BLUE}=== Template Injection Tests ===${NC}"

test_case "Velocity template" \
    "${BASE_URL}/api/v1/tpl/velocity?template=test"

test_case "FreeMarker template" \
    "${BASE_URL}/api/v1/tpl/data/render?template=test"

# Summary
echo -e "\n${BLUE}========================================${NC}"
echo "Total tests: ${YELLOW}$TOTAL${NC}"
echo "Passed: ${GREEN}$PASSED${NC}"
echo "Failed: ${RED}$FAILED${NC}"
