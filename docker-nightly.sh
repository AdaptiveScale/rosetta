#!/bin/bash

set -e

docker build -t ailegion/rosetta:nightly -f Dockerfile
docker push ailegion/rosetta:nightly
