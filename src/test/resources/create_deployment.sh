#!/bin/bash
OWNER=$1
REPO=$2
curl -L \
-X POST \
-H "Accept: application/vnd.github+json" \
-H "Authorization: Bearer $GITHUB_TOKEN" \
-H "X-GitHub-Api-Version: 2022-11-28" \
-d '{"ref":"master","environment":"staging","payload":"{ \"deploy\": \"migrate\" }","description":"Deploy request from hubot"}' \
"https://api.github.com/repos/$OWNER/$REPO/deployments"
