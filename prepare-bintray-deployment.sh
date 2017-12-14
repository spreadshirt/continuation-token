#!/bin/bash

set -euo pipefail

PROJECT_ROOT="${1}"
export POM_VERSION=$(grep --max-count=1 '<version>' "${PROJECT_ROOT}/pom.xml" | sed -E 's/<version>(.*)<\/version>/\1/' | tr -d '[:space:]')
export GIT_VERSION=$(git describe --always --tags)
export RELEASE_DATE=$(date --utc --iso-8601)
COMMIT_MESSAGE=${TRAVIS_COMMIT_MESSAGE:-"Automated CI release"}
export COMMIT_MESSAGE=$(echo $COMMIT_MESSAGE | tr [:space:] \ )

rm -rf deploy && mkdir deploy
cp "${PROJECT_ROOT}/pom.xml" deploy
cp "${PROJECT_ROOT}"/target/*.jar deploy
for f in $(ls deploy); do
    md5sum < "deploy/$f" > "deploy/${f}.md5"
    sha1sum < "deploy/$f" > "deploy/${f}.sha1"
done

cat bintray.json.tmpl | envsubst > bintray.json
