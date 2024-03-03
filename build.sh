##!/bin/bash

set -e
set -o pipefail
set -x

docker network inspect dind_network >/dev/null 2>&1 || \
    docker network create --driver bridge dind_network

docker start dind || docker run --privileged -d --name dind -d -p 2375:2375 \
        --network dind_network --network-alias dind \
        -e DOCKER_TLS_CERTDIR="" \
        docker:dind

docker build --no-cache --tag app:latest --network=dind_network .
