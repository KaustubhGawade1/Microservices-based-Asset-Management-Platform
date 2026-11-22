#!/bin/bash

# Configuration
AUTH_USER="user1"
AUTH_PASS="password1"
AUTH_URL="http://localhost:8081/api/auth"
ASSET_URL="http://localhost:8085/api"

echo "1. Registering/Logging in..."
echo "Trying Login (assuming user exists)..."
LOGIN_RESP=$(curl -s -X POST $AUTH_URL/signin \
  -H "Content-Type: application/json" \
  -d "{\"username\": \"$AUTH_USER\", \"password\": \"$AUTH_PASS\"}")

TOKEN=$(echo $LOGIN_RESP | jq -r '.accessToken')

if [ "$TOKEN" == "null" ] || [ -z "$TOKEN" ]; then
  echo "Login failed. Response: $LOGIN_RESP"
  echo "Attempting to create signup request..."
  curl -s -X POST $AUTH_URL/signup-request \
    -H "Content-Type: application/json" \
    -d "{\"username\": \"$AUTH_USER\", \"password\": \"$AUTH_PASS\", \"email\": \"user1@example.com\"}"
  echo -e "\nRequest sent. Please approve via Admin Console or DB, then re-run this script."
  exit 1
fi

echo "Token received: ${TOKEN:0:10}..."

echo "2. Creating dummy file..."
echo "Dummy content" > dummy_test_file.txt

echo "3. Uploading file..."
UPLOAD_RESP=$(curl -v -X POST $ASSET_URL/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@dummy_test_file.txt" \
  -F "isPublic=true")

echo -e "\nUpload Response: $UPLOAD_RESP"

echo "4. Listing Assets..."
LIST_RESP=$(curl -s -X GET $ASSET_URL/asset/my-assets \
  -H "Authorization: Bearer $TOKEN")

echo "Assets List: $LIST_RESP"

# Cleanup
rm dummy_test_file.txt
