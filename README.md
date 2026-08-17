# Doctor Queue 🏥

A full-stack doctor queue management system that allows patients to join a doctor's queue and enables doctors to manage patients in real time.

## 🚀 Features

### Patient

* Patient registration and login
* View available doctors
* Select a doctor and clinic
* Join a doctor's queue
* Receive a queue token number
* View current queue status
* See patients ahead in the queue
* See estimated waiting time
* Cancel a queue
* Live queue updates

### Doctor

* Doctor registration and login
* Doctor dashboard
* View current patient
* Start the next patient
* Complete a patient
* Skip a patient
* Mark skipped patients as completed
* Cancel waiting patients
* View waiting queue
* View skipped patients
* View completed patients
* View today's queue statistics
* Live queue updates

## 🛠️ Tech Stack

### Backend

* Java
* Spring Boot
* Spring Security
* JWT Authentication
* Spring Data JPA
* Hibernate
* PostgreSQL
* Gradle
* Server-Sent Events (SSE)

### Frontend

* React
* Vite
* JavaScript
* Axios
* CSS

## 📁 Project Structure

```text
doctorqueue/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/doctorqueue/doctorqueue/
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── dto/
│   │   │       ├── entity/
│   │   │       ├── exception/
│   │   │       ├── repository/
│   │   │       ├── security/
│   │   │       └── service/
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│
├── doctorqueue-frontend/
│   ├── src/
│   │   ├── api/
│   │   ├── components/
│   │   ├── App.jsx
│   │   ├── App.css
│   │   └── main.jsx
│   ├── package.json
│   └── vite.config.js
│
├── build.gradle
├── settings.gradle
├── gradlew
└── README.md
```

## 🔐 Authentication

The application uses JWT-based authentication.

The authentication flow is:

```text
Register
   ↓
Login
   ↓
Backend generates JWT
   ↓
Frontend stores authentication information
   ↓
Axios attaches Bearer token
   ↓
Spring Security validates JWT
   ↓
Protected API access
```

## 🔄 Queue Flow

### Patient

```text
Login
  ↓
Select Doctor
  ↓
Enter Patient Name
  ↓
Join Queue
  ↓
Receive Token
  ↓
Wait
  ↓
Doctor Starts Patient
  ↓
SERVING
  ↓
Doctor Completes / Skips Patient
  ↓
Queue Updated
```

### Doctor

```text
Login
  ↓
Doctor Dashboard
  ↓
View Waiting Queue
  ↓
NEXT PATIENT
  ↓
SERVING
  ↓
COMPLETE / SKIP
  ↓
Next Patient
```

## 📡 API Endpoints

### Authentication

```text
POST /api/auth/register
POST /api/auth/login
```

### Doctors

```text
GET /api/doctors
```

### Queue

```text
POST /api/queue/join

GET /api/queue/{queueId}

GET /api/queue/patient/{queueId}/dashboard

DELETE /api/queue/{queueId}
```

### Doctor Queue

```text
GET /api/queue/doctor/{doctorId}

GET /api/queue/doctor/{doctorId}/dashboard

GET /api/queue/doctor/{doctorId}/completed-today

POST /api/queue/doctor/{doctorId}/next

POST /api/queue/doctor/{doctorId}/complete

POST /api/queue/doctor/{doctorId}/skip

POST /api/queue/doctor/{doctorId}/skipped/{queueId}/complete
```

### Real-Time Events

```text
GET /api/queue/events
```

The application uses Server-Sent Events to update patient and doctor dashboards when the queue changes.

## 🗄️ Database

The application uses PostgreSQL.

Main entities include:

```text
User
Doctor
Clinic
QueueEntry
QueueStatus
```

A queue entry contains information such as:

```text
id
doctor_id
patient_id
patient_name
token_number
status
joined_at
```

Possible queue statuses include:

```text
WAITING
SERVING
COMPLETED
SKIPPED
CANCELLED
```

## ⚙️ Backend Setup

Clone the repository:

```bash
git clone git@github.com:Piyushs09/doctorqueue.git
cd doctorqueue
```

Configure PostgreSQL and update the backend configuration.

Then start the Spring Boot application:

```bash
./gradlew bootRun
```

The backend runs on:

```text
http://localhost:8080
```

## 🎨 Frontend Setup

Go to the frontend:

```bash
cd doctorqueue-frontend
```

Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

The frontend runs on:

```text
http://localhost:5173
```

## 🧪 Build

### Backend

```bash
./gradlew clean build
```

### Frontend

```bash
cd doctorqueue-frontend
npm run build
```

## 🔒 Environment & Secrets

Do not commit production passwords, JWT secrets, API keys, or other sensitive credentials to GitHub.

Production configuration should use environment variables.

Example:

```text
DATABASE_URL
DATABASE_USERNAME
DATABASE_PASSWORD
JWT_SECRET
```

## 📌 Current Status

The core Doctor Queue workflow is working:

* Authentication
* Patient queue joining
* Doctor dashboard
* Queue token generation
* Waiting queue
* Serving patient
* Complete patient
* Skip patient
* Complete skipped patient
* Patient dashboard
* Real-time queue updates
* Doctor statistics

## 🔮 Future Improvements

Planned improvements include:

* Online appointment booking
* Doctor statistics and analytics
* Average consultation time analytics
* Daily / weekly / monthly reports
* Multiple clinic support
* Doctor availability
* Patient notifications
* SMS / WhatsApp notifications
* Production deployment
* Admin dashboard
* Better mobile UI
* Queue history
* Appointment scheduling

## 👨‍💻 Author

**Piyush Singh**

GitHub: https://github.com/Piyushs09

LinkedIn: https://www.linkedin.com/in/piyush-singh-s/

## 📄 License

This project is currently for learning and development purposes.

