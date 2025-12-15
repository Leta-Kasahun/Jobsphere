# JobSphere Authentication API

**Base URL:** `http://localhost:8080`

## USER AUTH ENDPOINTS

POST /api/v1/auth/register                 → Create new user account
POST /api/v1/auth/verify-otp               → Confirm email with OTP  
POST /api/v1/auth/login                    → Login with email/password
POST /api/v1/auth/forgot-password          → Request password reset OTP
POST /api/v1/auth/verify-reset-otp         → Verify OTP for password reset
POST /api/v1/auth/reset-password           → Set new password with reset token
POST /api/v1/auth/logout                   → Clear authentication
POST /api/v1/auth/refresh                  → Get new access token
GET  /api/v1/auth/oauth-success            → Handle Google login callback
POST /api/v1/auth/select-role              → Set role for new Google user

## ADMIN AUTH ENDPOINTS

POST /api/v1/admin/auth/login              → Admin login (requires OTP)
POST /api/v1/admin/auth/verify-otp         → Complete admin login with OTP
POST /api/v1/admin/auth/forgot-password    → Request admin password reset
POST /api/v1/admin/auth/verify-reset-otp   → Verify admin reset OTP
POST /api/v1/admin/auth/reset-password     → Set new admin password
POST /api/v1/admin/auth/logout             → Clear admin authentication
POST /api/v1/admin/auth/refresh            → Renew admin access token

## GOOGLE OAUTH

GET /oauth2/authorization/google           → Start Google login

## TEST EXAMPLES

### User Registration
POST http://localhost:8080/api/v1/auth/register
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123",
  "userType": "SEEKER"
}

### User Login  
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123"
}

### Admin Login
POST http://localhost:8080/api/v1/admin/auth/login
Content-Type: application/json

{
  "email": "admin@jobsphere.com",
  "password": "admin123"
}

POST http://localhost:8080/api/v1/admin/auth/verify-otp
Content-Type: application/json

{
  "email": "admin@jobsphere.com",
  "otp": "123456"
}

### Password Reset
POST http://localhost:8080/api/v1/auth/forgot-password
Content-Type: application/json

{"email": "test@example.com"}

POST http://localhost:8080/api/v1/auth/verify-reset-otp
Content-Type: application/json

{
  "email": "test@example.com",
  "otp": "123456"
}

POST http://localhost:8080/api/v1/auth/reset-password
Content-Type: application/json

{
  "resetToken": "jwt-from-step2",
  "newPassword": "newpass123",
  "confirmPassword": "newpass123"
}