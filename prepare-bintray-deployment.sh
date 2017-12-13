#!/bin/bash

set -euo pipefail

POM_PATH="${1}"
export POM_VERSION=$(grep --max-count=1 '<version>' "$POM_PATH" | sed -E 's/<version>(.*)<\/version>/\1/' | tr -d '[:space:]')
export GIT_VERSION=$(git describe --always --tags)
export RELEASE_DATE=$(date --utc --iso-8601)
COMMIT_MESSAGE=${TRAVIS_COMMIT_MESSAGE:-"Automated CI release"}
export COMMIT_MESSAGE=$(echo $COMMIT_MESSAGE | tr [:space:] \ )

cat bintray.json.tmpl | envsubst > bintray.json
