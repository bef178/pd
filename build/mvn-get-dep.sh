#!/bin/bash

if [[ "$BASH_SOURCE" == /* ]]; then
    TOP=$(realpath $(dirname $BASH_SOURCE)/..)
else
    TOP=$(realpath $(pwd)/$(dirname $BASH_SOURCE)/..)
fi

mvn dependency:get \
    -DgroupId=org.junit.jupiter \
    -DartifactId=junit-jupiter-api \
    -Dversion=5.7.2 \
    -Dtransitive=false
