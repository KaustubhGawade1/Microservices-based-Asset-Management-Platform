#!/bin/bash

# Configuration
AUTH_SERVICE="http://localhost:8081/api/auth"
ASSET_SERVICE="http://localhost:8085/api"
USERNAME="user1"
PASSWORD="password1"

# 1. Login
echo "1. Logging in..."
TOKEN=$(curl -s -X POST $AUTH_SERVICE/signin \
  -H "Content-Type: application/json" \
  -d "{\"username\": \"$USERNAME\", \"password\": \"$PASSWORD\"}" | jq -r .accessToken)

if [ -z "$TOKEN" ] || [ "$TOKEN" == "null" ]; then
    echo "Login failed."
    exit 1
fi

# 1.5 Upload a File
echo "1.5 Uploading file..."
echo "Download Test Content" > download_test.txt
curl -s -X POST $ASSET_SERVICE/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@download_test.txt"

# 2. Get Asset ID (using My Assets)
echo "2. Fetching My Assets..."
ASSETS_JSON=$(curl -s -X GET $ASSET_SERVICE/asset/my-assets \
  -H "Authorization: Bearer $TOKEN")

echo "DEBUG: Assets JSON: $ASSETS_JSON"

ASSET_ID=$(echo $ASSETS_JSON | jq '.[-1].asset_id')
FILENAME=$(echo $ASSETS_JSON | jq -r '.[-1].filename')
echo "Target Asset ID: $ASSET_ID ($FILENAME)"

if [ -z "$ASSET_ID" ] || [ "$ASSET_ID" == "null" ]; then
    echo "No assets found."
    exit 1
fi

# 3. Download
echo "3. Downloading asset..."
curl -v -X GET "$ASSET_SERVICE/asset/download/$ASSET_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -o "downloaded_$FILENAME"

if [ -f "downloaded_$FILENAME" ]; then
    echo "SUCCESS: File downloaded as downloaded_$FILENAME"
else
    echo "FAILURE: File not downloaded."
fi
