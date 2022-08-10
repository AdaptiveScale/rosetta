#!/bin/bash

set -e

source $(dirname "$0")/version.sh

# Trigger read version
read_version
echo "Version: $APP_VERSION"

docker build -t adaptivescale/rosetta:latest -t adaptivescale/rosetta:$APP_VERSION -f Dockerfile
docker push adaptivescale/rosetta:latest
docker push adaptivescale/rosetta:$APP_VERSION
