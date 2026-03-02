#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SNAPSHOT_DIR="$SCRIPT_DIR/snapshots/baseline"
EXAMPLE_DIR="$SCRIPT_DIR/example"

# Build and publish plugin
echo "Publishing plugin to mavenLocal..."
"$SCRIPT_DIR/gradlew" -p "$SCRIPT_DIR" publishToMavenLocal -q

# Collect docs in example project
echo "Collecting docs in example project..."
"$EXAMPLE_DIR/gradlew" -p "$EXAMPLE_DIR" collectDocs -q

# Replace snapshot
echo "Updating baseline snapshot..."
rm -rf "$SNAPSHOT_DIR"
mkdir -p "$SNAPSHOT_DIR"
cp -R "$EXAMPLE_DIR/build/ai-docs/"* "$SNAPSHOT_DIR/"

echo "Baseline updated at snapshots/baseline/"
