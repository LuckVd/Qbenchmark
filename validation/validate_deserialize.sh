#!/bin/bash
# Data decode validation script

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
echo -e "${BLUE}Data Decode Validation Script${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

test_case() {
    local name="$1"
    local url="$2"
    TOTAL=$((TOTAL + 1))
    echo -e "\n${YELLOW}[Test $TOTAL]${NC} $name"
    echo "URL: $url"

    response=$(curl -s "$url" -X POST 2>/dev/null)
    if [ $? -eq 0 ]; then
        echo -e "  ${GREEN}[✓] Endpoint responded${NC}"
        echo "  Response: $(echo "$response" | head -c 100)..."
        PASSED=$((PASSED + 1))
    else
        echo -e "  ${RED}[✗] Request failed${NC}"
        FAILED=$((FAILED + 1))
    fi
}

echo -e "${BLUE}=== Deserialization Tests ===${NC}"

test_case "Cookie decode" \
    "${BASE_URL}/api/v1/data/decode/session"

test_case "JSON decode" \
    "${BASE_URL}/api/v1/data/decode/json" \
    "-H \"Content-Type: application/json\""

test_case "XML decode" \
    "${BASE_URL}/api/v1/data/decode/v2/xml" \
    "-H \"Content-Type: application/xml\""

test_case "YML decode" \
    "${BASE_URL}/api/v1/data/decode/v2/yml" \
    "-H \"Content-Type: application/x-yaml\""

# Summary
echo -e "\n${BLUE}========================================${NC}"
echo "Total tests: ${YELLOW}$TOTAL${NC}"
echo "Passed: ${GREEN}$PASSED${NC}"
echo "Failed: ${RED}$FAILED${NC}"
