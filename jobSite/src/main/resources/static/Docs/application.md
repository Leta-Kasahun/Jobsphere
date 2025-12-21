# Job Applications Flow

## 1. Apply for Job
Seeker:
- Selects a job
- Writes cover letter (required, max 10,000 chars)
- Enters expected salary (optional)
- Submits application

Auto-filled:
- CV from seeker profile
- Skills, sectors, tags (for employer view)

Status: PENDING

## 2. View Applications
Employer can:
- View all applications per job
- See:
  - Seeker basic info
  - CV link
  - Skills, sectors, tags
  - Cover letter
  - Expected salary
- Filter by status:
  - PENDING / APPROVED / REJECTED / HIRED

## 3. Review & Actions
Employer can:
- Add notes
- Change status:
  - PENDING → APPROVED
  - PENDING → REJECTED
- Mark as HIRED

## 4. Status Tracking
Each application stores:
- Status
- Applied date
- Reviewed date
- Hired flag
- Employer notes

## 5. Job Impact
- Each application increases job apply counter
- If marked HIRED:
  - Job can be closed
