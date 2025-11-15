#!/bin/bash

# Clean Test Environment Script
# This script cleans Gradle and IntelliJ caches to fix test execution issues

set -e

echo "🧹 Cleaning test environment..."
echo ""

# Navigate to the api-service directory
cd "$(dirname "$0")"

# Clean Gradle build directories
echo "1. Cleaning Gradle build directories..."
rm -rf build/
rm -rf .gradle/
rm -rf out/
echo "   ✓ Gradle directories cleaned"

# Clean IntelliJ IDEA directories (preserving run configurations)
echo ""
echo "2. Cleaning IntelliJ IDEA cache..."
rm -rf .idea/workspace.xml
rm -rf .idea/tasks.xml
rm -rf .idea/usage.statistics.xml
rm -rf .idea/shelf/
echo "   ✓ IntelliJ cache cleaned (run configurations preserved)"

# Run Gradle clean
echo ""
echo "3. Running Gradle clean..."
if [ -f "./gradlew" ]; then
    ./gradlew clean --quiet
    echo "   ✓ Gradle clean completed"
else
    echo "   ⚠ gradlew not found, skipping"
fi

echo ""
echo "✅ Test environment cleaned successfully!"
echo ""
echo "Next steps:"
echo "  1. In IntelliJ: File → Invalidate Caches → Invalidate and Restart"
echo "  2. Reopen the project"
echo "  3. Wait for Gradle sync to complete"
echo "  4. Run tests: Right-click SentimentControllerTest → Run"
echo ""
