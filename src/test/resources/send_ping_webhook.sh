#!/bin/bash
OWNER=$1
REPO=$2
HOOK_ID=$3
curl -L \
-X POST \
-H "Accept: application/vnd.github+json" \
-H "Authorization: Bearer $GITHUB_TOKEN" \
-H "X-GitHub-Api-Version: 2022-11-28" \
"https://api.github.com/repos/$OWNER/$REPO/hooks/$HOOK_ID/pings"
