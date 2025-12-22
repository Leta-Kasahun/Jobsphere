CV Builder & Templates (Implemented)
1. Admin — Create & Delete CV Templates (Private)
Create Template

Admin creates a CV template that defines structure only.

Sample Template Skeleton (Admin Input)

{
  "name": "Senior Software Engineer",
  "category": "TECH",
  "description": "Professional CV template for senior technical roles",
  "sections": {
    "header": {
      "title": { "required": true },
      "professional_summary": { "required": true }
    },
    "experience": {
      "job_title": { "required": true },
      "company_name": { "required": true },
      "start_date": { "required": true },
      "end_date": { "required": false },
      "description": { "required": true },
      "technologies": { "required": false }
    },
    "projects": {
      "project_name": { "required": true },
      "description": { "required": true },
      "technologies": { "required": true },
      "project_url": { "required": false }
    },
    "education": {
      "institution": { "required": true },
      "degree": { "required": true },
      "field_of_study": { "required": false },
      "graduation_year": { "required": false }
    },
    "skills": {
      "technical_skills": { "required": true },
      "professional_skills": { "required": false }
    },
    "languages": {
      "language_name": { "required": true },
      "proficiency_level": { "required": true }
    }
  }
}

Delete Template

Admin deletes template

Template becomes unavailable to job seekers

Existing seeker CV data remains unchanged

2. Job Seeker — View, Build & Download CV (Public)
View CV Templates

Job seeker opens CV Builder and sees available templates.

Sample Template List (View Only)

[
  {
    "id": "tpl-1",
    "name": "Senior Software Engineer",
    "category": "TECH",
    "description": "Professional technical CV"
  }
]

Preview Template

Seeker previews layout

No data is changed

Select Template & Auto-Fill

When seeker selects a template:

System reads existing seeker_cv.details

Matching fields are auto-filled

Missing fields stay empty

Sample Auto-Filled CV Data

{
  "title": "Senior Backend Developer",
  "professional_summary": "Experienced Java developer",
  "experience": [
    {
      "job_title": "Backend Engineer",
      "company_name": "Tech Corp",
      "start_date": "2021-01",
      "description": "Built REST APIs",
      "technologies": ["Java", "Spring Boot"]
    }
  ]
}

Edit & Add Additional Data

Seeker can:

Edit auto-filled fields

Add missing information

Add new entries

Example Added Data by Seeker

{
  "projects": [
    {
      "project_name": "Job Portal",
      "description": "Online recruitment platform",
      "technologies": ["Spring Boot", "PostgreSQL"],
      "project_url": "https://github.com/example"
    }
  ],
  "certifications": [
    {
      "certificate_name": "AWS Developer",
      "issuer": "Amazon",
      "year": 2024
    }
  ]
}

Save CV

Data is saved into existing seeker_cv.details

Required fields must be filled

Optional fields may be empty

Download CV

CV is rendered using selected template

Seeker downloads CV as PDF

Data Rules (Strict)

Templates define structure only

CV data belongs to seeker

One CV per seeker

Uploaded CV files are not used

Templates never store seeker data