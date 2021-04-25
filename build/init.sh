#!/bin/bash

TOP=$(realpath $(pwd)/$(dirname $BASH_SOURCE)/..)

URIS="
"

for uri in $URIS; do
wget -N $uri -P $TOP/lib
done
