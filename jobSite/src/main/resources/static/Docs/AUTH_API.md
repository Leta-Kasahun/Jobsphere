# Authentication Flow Documentation

## 1. User Registration Flow
Step 1: User registers with email and password, selects user type (Seeker/Employer)  
Step 2: System sends OTP to email for verification  
Step 3: User verifies email with OTP OR registers via Google OAuth  
Step 4: After verification, user is redirected to role selection (Seeker/Employer)  
Step 5: User continues to complete their respective profile  

## 2. User Login Flow
Step 1: User logs in with email and password OR via Google login  
Step 2: After successful authentication, user is redirected to their respective dashboard  
Step 3: Seeker → Job Seeker Dashboard | Employer → Employer Dashboard  

## 3. Password Reset Flow
Step 1: User clicks "Forgot Password" and enters email  
Step 2: System sends password reset OTP to email  
Step 3: User is redirected to password reset page  
Step 4: User enters OTP from email for verification  
Step 5: User enters new password and confirms new password  
Step 6: Backend updates password, user can now login with new password  

## 4. Admin Authentication Flow
Step 1: Admin is registered manually in the database (not via registration form)  
Step 2: Admin logs in with email and password  
Step 3: System sends OTP to admin email (2-factor authentication)  
Step 4: Admin verifies with OTP from email  
Step 5: After verification, admin is redirected to admin dashboard  

## Endpoints Summary:

### User Registration & Login:
- `POST /api/v1/auth/register` - Register with email/password
- `POST /api/v1/auth/verify-otp` - Verify email OTP
- `GET /oauth2/authorization/google` - Google OAuth login
- `POST /api/v1/auth/select-role` - Select role after Google login
- `POST /api/v1/auth/login` - Login with email/password

### Password Reset:
- `POST /api/v1/auth/forgot-password` - Request password reset OTP
- `POST /api/v1/auth/verify-reset-otp` - Verify reset OTP, get reset token
- `POST /api/v1/auth/reset-password` - Reset password with token

### Admin:
- `POST /api/v1/admin/auth/login` - Admin login (returns OTP)
- `POST /api/v1/admin/auth/verify-otp` - Admin verify OTP, get access token

# JobSphere Authentication API

**Base URL:** `http://localhost:8080`

## USER AUTH ENDPOINTS
```
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
```
## ADMIN AUTH ENDPOINTS
```
POST /api/v1/admin/auth/login              → Admin login (requires OTP)
POST /api/v1/admin/auth/verify-otp         → Complete admin login with OTP
POST /api/v1/admin/auth/forgot-password    → Request admin password reset
POST /api/v1/admin/auth/verify-reset-otp   → Verify admin reset OTP
POST /api/v1/admin/auth/reset-password     → Set new admin password
POST /api/v1/admin/auth/logout             → Clear admin authentication
POST /api/v1/admin/auth/refresh            → Renew admin access token
```
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
