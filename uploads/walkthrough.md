
### Bug Fixes & Improvements

#### Login 500 Internal Server Error Resolved
- **Issue**: Login requests were failing with a 500 Internal Server Error.
- **Root Cause**: A `DataIntegrityViolationException` occurred in `RefreshTokenService` because the `refreshtoken` table had a unique constraint on `user_id`, and the code blindly attempted to insert a new token for an existing user.
- **Fix**: Refactored `RefreshTokenService.createRefreshToken` to check for an existing token using `findByUser`, delete it if found, and `flush` the repository before creating a new token.
- **Verification**: Confirmed successful login via `curl` with a 200 OK response containing the access token. Validated frontend login flow with an automated browser test.

![Successful Dashboard Login](file:///home/kaustubhgawade/.gemini/antigravity/brain/12bf0d75-e01a-4a7a-be08-2cb9aaa6574f/dashboard_success_1767693682264.png)

#### Contact Admin Feature
- **Implementation**: The "Contact Admin" string on the login page was converted to a functional `mailto:admin@example.com` link, allowing users to easily reach support without a dedicated signup form.
