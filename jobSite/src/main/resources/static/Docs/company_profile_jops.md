# Company Profile Flow
---
## 1. Access
- Only APPROVED employers
- Verification code must be used

## 2. Create Profile
Auto-filled:
- Company Name
- Website

Required:
- Description

Optional:
- Logo (Cloudinary)
- Industry
- Legal Status
- Social Links
- Headquarters

## 3. Rules
- One profile per employer
- Company name cannot be changed

## 4. Update
- Employer can update profile fields anytime

## 5. Result
- Employer can post jobs


---
# Job Posting Flow

## 1. Access
- Employer must have company profile

## 2. Create Job
Required:
- Title
- Description
- Job Type
- Workplace Type
- Category
- Education Level

Optional:
- Gender Requirement
- Vacancy Count
- Experience Level
- Experience Description
- Salary Min/Max
- Deadline
- Address (not required for REMOTE)

## 3. Status
- Default: is_active = true

## 4. Update
- Employer can update own jobs

## 5. Deactivate
- Set is_active = false

## 6. View
- Only active jobs are public
