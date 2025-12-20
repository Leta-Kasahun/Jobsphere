# Frontend Developer Guide - Seeker Profile

## 🔄 User Flow After Registration/Login

1. User registers → gets email OTP → verifies email → slect role


## 📋 Profile Setup Endpoints (In Order)

### 1. Create Profile
POST /api/v1/seekers/profile/basic-info

### 2. Upload Profile Image  
POST /api/v1/seekers/profile/image

### 3. Add Address
POST /api/v1/seekers/profile/address

## 📝 Profile Management Endpoints

### Get Profile
GET /api/v1/seekers/profile/basic-info

### Update Profile  
PUT /api/v1/seekers/profile/basic-info

### Delete Image
DELETE /api/v1/seekers/profile/image

### Remove Address
DELETE /api/v1/seekers/profile/address

### Delete Profile
DELETE /api/v1/seekers/profile/basic-info

## 💡 Examples

### Example 1: First-time Profile Setup

// 1. Create basic profile
POST /api/v1/seekers/profile/basic-info
{
  "firstName": "Alice",
  "middleName": "Marie",
  "lastName": "Johnson",
  "phone": "+251911223344",
  "gender": "FEMALE",
  "dateOfBirth": "1992-05-20"
}

// 2. Upload profile photo
POST /api/v1/seekers/profile/image
form-data: file = [5mb]

// 3. Add address
POST /api/v1/seekers/profile/address
{
  "country": "Ethiopia",
  "city": "Addis Ababa",
  "subCity": "Bole",
  "street": "Airport Road"
}

//4  Update profile info
PUT /api/v1/seekers/profile/basic-info
{
  "firstName": "Alice",
  "middleName": "Marie",
  "lastName": "Johnson-Smith",
  "phone": "+251922334455",
  "gender": "FEMALE",
  "dateOfBirth": "1992-05-20"
}

// Change profile photo
POST /api/v1/seekers/profile/image
form-data: file = [new-photo.jpg]

// Remove old photo first
DELETE /api/v1/seekers/profile/image


// Get full profile data
GET /api/v1/seekers/profile/basic-info

Response:
{
  "id": "uuid-123",
  "firstName": "Alice",
  "middleName": "Marie",
  "lastName": "Johnson",
  "phone": "+251911223344",
  "gender": "FEMALE",
  "dateOfBirth": "1992-05-20",
  "email": "alice@email.com",
  "profileCompletion": "85%",
  "profileImageUrl": "https://cloudinary.com/image.jpg",
  "address": {
    "country": "Ethiopia",
    "city": "Addis Ababa",
    "street": "Airport Road"
  }
}