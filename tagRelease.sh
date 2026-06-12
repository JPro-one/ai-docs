#!/bin/bash
# Tags the current commit as release vX.Y.Z (no publishing happens locally).
# The tag push triggers .github/workflows/release.yml, which builds and publishes.
set -e

VERSION=$1
if [[ ! "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "usage: ./tagRelease.sh X.Y.Z"
    exit 1
fi

if [ -n "$(git status --porcelain)" ]; then
    echo "error: working tree is not clean"
    exit 1
fi

BRANCH=$(git branch --show-current)
if [ "$BRANCH" != "main" ]; then
    echo "error: not on main (on $BRANCH)"
    exit 1
fi

git fetch origin
if [ "$(git rev-parse HEAD)" != "$(git rev-parse origin/main)" ]; then
    echo "error: HEAD is not in sync with origin/main"
    exit 1
fi

if git rev-parse "v$VERSION" >/dev/null 2>&1; then
    echo "error: tag v$VERSION already exists"
    exit 1
fi

git tag "v$VERSION"
git push origin "v$VERSION"
echo "Tagged and pushed v$VERSION — the release workflow takes it from here:"
echo "https://github.com/JPro-one/ai-docs/actions"
