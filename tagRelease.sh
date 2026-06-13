#!/bin/bash
# Tags the current commit as release vX.Y.Z (no publishing happens locally).
# The tag push triggers .github/workflows/release.yml, which builds and publishes.
set -e

# First arg is the project name, guarding against running this in the wrong repo.
PROJECT="ai-docs"
if [ "$1" != "$PROJECT" ]; then
    echo "usage: ./tagRelease.sh $PROJECT X.Y.Z"
    exit 1
fi

VERSION=$2
if [[ ! "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "usage: ./tagRelease.sh $PROJECT X.Y.Z"
    exit 1
fi

# Only tracked modifications matter: the tag points at HEAD, so uncommitted
# edits would be silently missing from the release. Untracked files are fine.
if [ -n "$(git status --porcelain -uno)" ]; then
    echo "error: uncommitted changes to tracked files"
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
