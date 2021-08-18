#!/bin/bash

TOP=$(realpath $(pwd)/$(dirname $BASH_SOURCE)/..)

URIS="
https://repo1.maven.org/maven2/org/junit/jupiter/junit-jupiter-api/5.7.2/junit-jupiter-api-5.7.2.jar
"

for uri in $URIS; do
wget -N $uri -P $TOP/lib
done
