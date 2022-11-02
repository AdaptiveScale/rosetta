#!/bin/bash

source $(dirname "$0")/version.sh

update_version() {
    echo "Checking versions"
    if [[ $BRANCH_NAME != "release"* ]]; then
        echo "Not release branch - skipping version update"
        return
    fi
    IFS=. read MAJOR_V MINOR_V MICRO_V <<<"${BRANCH_NAME##release-}"
    if [ -z "$MAJOR_V" ]; then
        echo "Missing major version"
        exit 1
    fi
    if [ -z "$MINOR_V" ]; then
        echo "Missing minor version"
        exit 1
    fi
    if [ -z "$MICRO_V" ]; then
        echo "Missing micro version"
        exit 1
    fi
    VERSION_BUMP=$MAJOR_V.$MINOR_V.$MICRO_V
    if [ $APP_VERSION == $VERSION_BUMP ]; then
        echo "Versions match - skipping update"
        return
    fi
    echo "Updating version $APP_VERSION to $VERSION_BUMP"

    printf '%s\n' "%s/version =.*/version = \'$VERSION_BUMP\'/g" 'x' | ex build.gradle
    printf '%s\n' "%s/version =.*/version = \"$VERSION_BUMP\",/g" 'x' | ex cli/src/main/java/com/adaptivescale/rosetta/cli/Cli.java

    # Commit and push version bump
    git config --local user.name "github-actions[bot]"
    git config --local user.email "41898282+github-actions[bot]@users.noreply.github.com"
    git add build.gradle
    git add cli/src/main/java/com/adaptivescale/rosetta/cli/Cli.java
    git commit -m "Version bump: ${BUILD_VERSION} to ${VERSION_BUMP}"
    git push --set-upstream origin $BRANCH_NAME
    APP_VERSION=$VERSION_BUMP
}

# Set default version to develop
APP_VERSION=develop

# Trigger read version
read_version
echo "Version: $APP_VERSION"

# Update version on release if needed
update_version $BRANCH_NAME
echo "Version: $APP_VERSION"
