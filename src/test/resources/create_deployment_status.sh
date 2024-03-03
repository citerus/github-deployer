#!/bin/bash
OWNER=$1
REPO=$2
DEPLOYMENT_ID=$3
curl -L \
-X POST \
-H "Accept: application/vnd.github+json" \
-H "Authorization: Bearer $GITHUB_TOKEN" \
-H "X-GitHub-Api-Version: 2022-11-28" \
-d '{"environment":"production","state":"success","log_url":"https://example.com/deployment/42/output","description":"Deployment finished successfully."}' \
"https://api.github.com/repos/$OWNER/$REPO/deployments/$DEPLOYMENT_ID/statuses"
