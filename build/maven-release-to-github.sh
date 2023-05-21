#!/bin/bash

pom="pom.xml"
repoId="github"
repoUrl="https://maven.pkg.github.com/bef178/pd"

mvn clean deploy -f "$pom" \
    -DaltDeploymentRepository=$repoId::default::$repoUrl \
    -DdeployAtEnd=true
