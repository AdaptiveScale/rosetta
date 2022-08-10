#!/bin/bash

set -e

source $(dirname "$0")/version.sh

# Trigger read version
read_version
echo "Version: $APP_VERSION"

docker build -t ailegion/rosetta:latest -t ailegion/rosetta:$APP_VERSION -f Dockerfile
docker push ailegion/rosetta:latest
docker push ailegion/rosetta:$APP_VERSION
