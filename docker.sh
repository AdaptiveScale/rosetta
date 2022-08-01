#!/bin/bash

set -e
#gradle build cli:jar

docker build -t ailegion/rosetta:latest -t ailegion/rosetta:0.0.1 -f Dockerfile
