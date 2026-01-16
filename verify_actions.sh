#!/bin/bash

# Configuration
AUTH_SERVICE="http://localhost:8081/api/auth"
ASSET_SERVICE="http://localhost:8085/api"
USERNAME="user1"
PASSWORD="password1"

echo "1. Logging in..."
TOKEN=$(curl -s -X POST $AUTH_SERVICE/signin \
  -H "Content-Type: application/json" \
  -d "{\"username\": \"$USERNAME\", \"password\": \"$PASSWORD\"}" | jq -r .accessToken)

if [ -z "$TOKEN" ] || [ "$TOKEN" == "null" ]; then
    echo "Login failed. Check credentials or service status."
    exit 1
fi
echo "Login successful. Token received."

# 2. Upload a File
echo "2. Uploading a test file..."
echo "This is a test file for visibility" > test_vis.txt
curl -s -X POST $ASSET_SERVICE/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@test_vis.txt" \
  -F "isPublic=false"

# 3. Get My Assets to find the ID
echo "3. Fetching My Assets to get ID..."
ASSETS_JSON=$(curl -s -X GET $ASSET_SERVICE/asset/my-assets \
  -H "Authorization: Bearer $TOKEN")

# Extract the last asset ID (assuming it's the one we just uploaded)
ASSET_ID=$(echo $ASSETS_JSON | jq '.[-1].asset_id')
echo "Target Asset ID: $ASSET_ID"

if [ -z "$ASSET_ID" ] || [ "$ASSET_ID" == "null" ]; then
    echo "Failed to retrieve asset ID."
    exit 1
fi

# 4. Toggle Visibility
echo "4. Toggling Visibility to TRUE..."
curl -v -X POST "$ASSET_SERVICE/asset/$ASSET_ID/visibility" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"makePublic": true}'

echo "5. Verifying Visibility..."
# Fetch again and check isPublic
UPDATED_ASSET=$(curl -s -X GET $ASSET_SERVICE/asset/my-assets \
  -H "Authorization: Bearer $TOKEN" | jq ".[] | select(.asset_id == $ASSET_ID)")

IS_PUBLIC=$(echo $UPDATED_ASSET | jq '.public')
echo "Asset public status: $IS_PUBLIC"

if [ "$IS_PUBLIC" == "true" ]; then
    echo "SUCCESS: Visibility updated to TRUE."
else
    echo "FAILURE: Visibility did not update."
fi
