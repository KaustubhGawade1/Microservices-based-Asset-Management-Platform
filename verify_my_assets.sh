#!/bin/bash

# Port forward must be active on 8085 and 8081

# 1. Login to get token
echo "Logging in..."
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"username":"user1", "password":"password1"}' | jq -r .accessToken)

if [ "$TOKEN" == "null" ] || [ -z "$TOKEN" ]; then
    echo "Login failed. Check auth-service."
    exit 1
fi
echo "Token received."

# 2. Call My Assets
echo "Fetching My Assets..."
curl -v -X GET http://localhost:8085/api/asset/my-assets \
  -H "Authorization: Bearer $TOKEN"
