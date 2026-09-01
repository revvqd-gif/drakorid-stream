#!/usr/bin/env bash
# usage: set-version.sh v0.1.0
# Updates versionCode & versionName in app/build.gradle.kts to match the git tag.
set -euo pipefail

TAG="${1:-}"
if [ -z "$TAG" ]; then
  echo "usage: $0 <tag>" >&2
  exit 1
fi

# strip leading 'v'
VERSION="${TAG#v}"
# versionName: strip pre-release suffix (e.g. 0.1.0-rc1 → 0.1.0)
CLEAN_VERSION="${VERSION%%-*}"
# versionCode: take the major+minor+patch digits as a single integer (0.1.0 → 10)
MAJOR=$(echo "$CLEAN_VERSION" | cut -d. -f1)
MINOR=$(echo "$CLEAN_VERSION" | cut -d. -f2)
PATCH=$(echo "$CLEAN_VERSION" | cut -d. -f3)
CODE=$((MAJOR * 10000 + MINOR * 100 + PATCH))

FILE="app/build.gradle.kts"
sed -i "s/versionName = \".*\"/versionName = \"$CLEAN_VERSION\"/" "$FILE"
sed -i "s/versionCode = .*/versionCode = $CODE/" "$FILE"

echo "Set versionName=$CLEAN_VERSION versionCode=$CODE"