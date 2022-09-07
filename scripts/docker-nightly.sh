#!/bin/bash

set -e

docker build -t adaptivescale/rosetta:nightly -f Dockerfile .
docker push adaptivescale/rosetta:nightly
