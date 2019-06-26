#!/bin/bash

TOP=$(realpath $(pwd)/$(dirname $BASH_SOURCE)/..)

URIS="
https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.25/slf4j-api-1.7.25.jar
https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.25/slf4j-api-1.7.25-sources.jar
https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/1.7.25/slf4j-simple-1.7.25.jar
https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/1.7.25/slf4j-simple-1.7.25-sources.jar
"

for uri in $URIS; do
wget -N $uri -P $TOP/lib
done
