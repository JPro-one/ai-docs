#!/bin/bash
# Publishes the Gradle plugin to the Gradle Plugin Portal.
set -e
cd "$(dirname "$0")"

VERSION=$(./gradlew -q :ai-docs-core:properties | grep "^version:" | awk '{print $2}')
echo "Publishing version $VERSION to the Gradle Plugin Portal"

if [[ "$VERSION" == *-SNAPSHOT ]]; then
    echo "error: $VERSION is a SNAPSHOT — the Plugin Portal only takes releases (tag with ./tagRelease.sh)"
    exit 1
fi

./gradlew :ai-docs-gradle-plugin:publishPlugins
