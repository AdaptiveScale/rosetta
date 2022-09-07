#!/bin/bash

set -e

source $(dirname "$0")/version.sh

# Trigger read version
read_version
echo "Build version: $APP_VERSION"

git config --local user.name "github-actions[bot]"
git config --local user.email "41898282+github-actions[bot]@users.noreply.github.com"
git tag "v$APP_VERSION" $TAG_BASE
git push origin "v$APP_VERSION"
