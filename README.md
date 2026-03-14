# Etworks 
This README is shared by both the frontend and backend projects.


HireNest is the React frontend. JobSphere is the Spring Boot backend. Together they implement a job portal with seeker, employer, and admin flows.

**What’s implemented **
- Authentication: email/password registration, OTP verification, login, refresh, logout, password reset.
- Admin authentication: admin login + OTP verification, refresh, logout.
- Google OAuth: login with role selection and callback handling.
- Role-based access: seeker, employer, admin protected routes.
- Jobs: create, list with filters, update, deactivate, status updates, likes, saved jobs.
- Applications: apply, list by job, list my applications, update status, check already applied.
- Seeker profile: basic info, profile image, address, bio, skills, sector, tags, social links, projects, CV details.
- CV builder: template listing, auto-fill from profile, preview, PDF download.
- Employer: company profile + logo upload, company verification workflow, employer analytics stats.
- Admin: user management, CV template management, company verification review, analytics stats.
- Job alerts and matching: alerts CRUD, auto-sync with profile, matched jobs endpoint, recommended candidates.
- Payments: Chapa integration for job posting payments, verification, history, webhook.
- Notifications: list, unread count, mark read, test notification endpoint.
- AI assistant: `/api/v1/ai/chat` and the Ema chat widget on the frontend.

Some UI routes are placeholders (see `C:\Users\Hp\Desktop\HireNestBackend\HireNest\src\App.jsx` where `PlaceholderPage` is used).

**Tech Stack**
- Frontend: React 19, Vite, Tailwind CSS, React Router, Zustand, Axios.
- Backend: Spring Boot 3.5.8, Java 21, PostgreSQL, Flyway, Spring Security, JWT, OAuth2, OpenAPI (springdoc), OpenPDF.
- Integrations: Brevo (email), Cloudinary (media), Chapa (payments), Google OAuth, Gemini (AI).

**Local Setup**
Backend (JobSphere):
```bash
cd C:\Users\Hp\Desktop\HireNestBackend\Jobsphere\jobSite
mvn spring-boot:run
```

Frontend (HireNest):
```bash
cd C:\Users\Hp\Desktop\HireNestBackend\HireNest
npm install
npm run dev
```

Frontend runs on `http://localhost:5173`. Backend runs on `http://localhost:8080`.

**Environment Variables**
Frontend (`C:\Users\Hp\Desktop\HireNestBackend\HireNest\.env`):

| Variable | Description | Example |
|---|---|---|
| `VITE_API_BASE_URL` | Backend base URL | `http://localhost:8080` |
| `VITE_ENV` | Environment | `development` |

Backend (`C:\Users\Hp\Desktop\HireNestBackend\Jobsphere\jobSite\src\main\resources\application.properties`):

| Variable | Description |
|---|---|
| `DB_URL` | PostgreSQL JDBC URL |
| `DB_USERNAME` | DB username |
| `DB_PASSWORD` | DB password |
| `JWT_SECRET` | JWT signing secret |
| `GOOGLE_CLIENT_ID` | Google OAuth client id |
| `GOOGLE_CLIENT_SECRET` | Google OAuth client secret |
| `BREVO_SMTP_USERNAME` | Brevo SMTP username |
| `BREVO_SMTP_KEY` | Brevo SMTP key |
| `GEMINI_API_KEY` | Gemini API key |



**API Overview (selected)**
- Auth: `/api/v1/auth/*`, `/api/v1/admin/auth/*`
- Jobs: `/api/v1/jobs`, `/api/v1/jobs/{id}`, `/api/v1/jobs/{id}/like`, `/api/v1/jobs/saved`
- Applications: `/api/v1/applications`
- Seeker profile: `/api/v1/seekers/profile` and `/api/v1/seekers/profile/details/*`
- Employer: `/api/v1/company-profile`, `/api/v1/employer/verification`, `/api/v1/employer/analytics/stats`
- Admin: `/api/v1/admin/users`, `/api/v1/admin/cv-templates`, `/api/v1/admin/company-verifications`, `/api/v1/admin/analytics/stats`
- CV builder: `/api/v1/cv-builder/*`
- Payments: `/api/v1/payments/*`
- Notifications: `/api/v1/notifications/*`
- AI: `/api/v1/ai/chat`

**Screenshots**
Home page
<img width="811" height="318" alt="image" src="https://github.com/user-attachments/assets/3a46856b-e23a-40c8-a6b9-12266aff6cad" />

User Login page
<img width="800" height="304" alt="image" src="https://github.com/user-attachments/assets/8824eef6-a944-41c4-9c5c-123f844ec29a" />

Admin Login page
<img width="808" height="297" alt="image" src="https://github.com/user-attachments/assets/71d72f19-1d1f-4438-a1c2-e70e1ed788d1" />

Seeker Dashboard
<img width="803" height="292" alt="image" src="https://github.com/user-attachments/assets/2f6a271d-0263-4aa2-a4c7-4d6371de4d3f" />

Employer Dashboard
<img width="777" height="294" alt="image" src="https://github.com/user-attachments/assets/6c93c338-c51c-435b-8cbc-7fdef9f90201" />

Admin Dashboard
<img width="766" height="301" alt="image" src="https://github.com/user-attachments/assets/2a36fef0-5a7d-42b9-8f9b-0b514d3e0e3f" />

Job management page
<img width="614" height="383" alt="image_2025-12-24_01-37-32" src="https://github.com/user-attachments/assets/8a07ab62-4e10-42c8-bc62-6e07fa3b85fe" />

Payment page
<img width="737" height="427" alt="image_2025-12-24_03-14-30" src="https://github.com/user-attachments/assets/da408ee1-7d91-428b-b3a1-a683ec30675e" />
