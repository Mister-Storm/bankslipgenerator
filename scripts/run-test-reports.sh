#!/bin/bash
# Test Coverage and Mutation Testing Report Generator
# This script runs all tests and generates comprehensive reports

set -e

echo "🧪 Starting Test Coverage and Mutation Analysis..."
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Clean previous reports
echo -e "${BLUE}🧹 Cleaning previous reports...${NC}"
./gradlew clean

# Run unit tests with coverage
echo ""
echo -e "${BLUE}🔬 Running Unit Tests with Coverage...${NC}"
./gradlew test koverHtmlReport koverXmlReport

# Check if tests passed
if [ $? -ne 0 ]; then
    echo -e "${YELLOW}⚠️  Some unit tests failed. Check the reports.${NC}"
    exit 1
fi

# Run integration tests
echo ""
echo -e "${BLUE}🔗 Running Integration Tests...${NC}"
./gradlew integrationTest

# Run mutation tests (this can take a while)
echo ""
echo -e "${BLUE}🧬 Running Mutation Tests (this may take a few minutes)...${NC}"
./gradlew pitest

echo ""
echo -e "${GREEN}✅ All tests completed!${NC}"
echo ""
echo "📊 Reports Generated:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📈 Code Coverage Report:"
echo "   HTML: build/reports/kover/html/index.html"
echo "   XML:  build/reports/kover/report.xml"
echo ""
echo "🧬 Mutation Testing Report:"
echo "   HTML: build/reports/pitest/index.html"
echo "   XML:  build/reports/pitest/mutations.xml"
echo ""
echo "🧪 Unit Test Results:"
echo "   HTML: build/reports/tests/test/index.html"
echo ""
echo "🔗 Integration Test Results:"
echo "   HTML: build/reports/tests/integrationTest/index.html"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Parse and display coverage summary if available
if [ -f "build/reports/kover/report.xml" ]; then
    echo -e "${BLUE}📊 Coverage Summary:${NC}"
    # Simple parsing of XML (requires xmllint or similar)
    if command -v xmllint &> /dev/null; then
        COVERAGE=$(xmllint --xpath "string(//counter[@type='INSTRUCTION']/@covered)" build/reports/kover/report.xml 2>/dev/null || echo "N/A")
        TOTAL=$(xmllint --xpath "string(//counter[@type='INSTRUCTION']/@missed)" build/reports/kover/report.xml 2>/dev/null || echo "N/A")

        if [ "$COVERAGE" != "N/A" ] && [ "$TOTAL" != "N/A" ]; then
            PERCENTAGE=$(awk "BEGIN {printf \"%.2f\", ($COVERAGE / ($COVERAGE + $TOTAL)) * 100}")
            echo "   Instruction Coverage: ${PERCENTAGE}%"
        fi
    else
        echo "   Install xmllint for detailed coverage summary"
    fi
    echo ""
fi

# Display mutation score if available
if [ -f "build/reports/pitest/index.html" ]; then
    echo -e "${BLUE}🧬 Mutation Testing Summary:${NC}"
    echo "   Check the HTML report for detailed mutation coverage"
    echo ""
fi

echo -e "${GREEN}✨ Test analysis complete!${NC}"
echo ""
echo "To view the reports, open them in your browser:"
echo "  - Coverage: xdg-open build/reports/kover/html/index.html"
echo "  - Mutations: xdg-open build/reports/pitest/index.html"

