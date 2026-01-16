#!/bin/bash

AUTH_URL="http://localhost:8081/api/auth"
ADMIN_URL="http://localhost:8089/api/admin"

# Random user to avoid conflicts
RAND_SUFFIX=$(date +%s)
NEW_USER="user_$RAND_SUFFIX"
NEW_EMAIL="user_$RAND_SUFFIX@example.com"
PASSWORD="password123"

echo "=== Verifying User Approval Workflow ==="
echo "Target User: $NEW_USER"

# 1. Signup Request
echo -e "\n1. Submitting Signup Request..."
STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$AUTH_URL/signup-request" \
  -H "Content-Type: application/json" \
  -d "{\"username\": \"$NEW_USER\", \"email\": \"$NEW_EMAIL\", \"password\": \"$PASSWORD\", \"role\": [\"user\"]}")

if [ "$STATUS" -ne 200 ]; then
    echo "FAILED: Signup request failed with status $STATUS"
    exit 1
fi
echo "SUCCESS: Signup request submitted."

# 2. Login as Admin
echo -e "\n2. Logging in as Admin (user1)..."
ADMIN_TOKEN=$(curl -s -X POST "$AUTH_URL/signin" \
  -H "Content-Type: application/json" \
  -d '{"username": "user1", "password": "password1"}' | jq -r .accessToken)

if [ -z "$ADMIN_TOKEN" ] || [ "$ADMIN_TOKEN" == "null" ]; then
    echo "FAILED: Admin login failed."
    exit 1
fi
echo "SUCCESS: Admin logged in."

# 3. List Requests
echo -e "\n3. Checking Pending Requests..."
REQUESTS=$(curl -s -X GET "$ADMIN_URL/users/requests" \
  -H "Authorization: Bearer $ADMIN_TOKEN")

echo "$REQUESTS" | grep -q "$NEW_USER"
if [ $? -ne 0 ]; then
    echo "FAILED: New user request not found in admin list."
    echo "Response: $REQUESTS"
    exit 1
fi
echo "SUCCESS: Found pending request for $NEW_USER."

# 4. Approve User
echo -e "\n4. Approving User..."
STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$ADMIN_URL/users/$NEW_USER/approve" \
  -H "Authorization: Bearer $ADMIN_TOKEN")

if [ "$STATUS" -ne 200 ]; then
    echo "FAILED: Approval failed with status $STATUS"
    exit 1
fi
echo "SUCCESS: User approved."

# 5. Verify Login as New User
echo -e "\n5. Verifying Login as New User..."
NEW_TOKEN=$(curl -s -X POST "$AUTH_URL/signin" \
  -H "Content-Type: application/json" \
  -d "{\"username\": \"$NEW_USER\", \"password\": \"$PASSWORD\"}" | jq -r .accessToken)

if [ -z "$NEW_TOKEN" ] || [ "$NEW_TOKEN" == "null" ]; then
    echo "FAILED: New user login failed."
    exit 1
fi
echo "SUCCESS: New user logged in successfully!"
