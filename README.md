# 🎓 Institute Management System

A comprehensive, industry-level Institute Management System built with **Spring Boot** and modern web technologies. This system provides complete management capabilities for educational institutions including student enrollment, course management, attendance tracking, grade management, and an AI-powered query solver.

## 🌟 Features

### 🔐 Authentication & Authorization
- **JWT-based Authentication** - Secure token-based authentication
- **Role-Based Access Control** - Three user roles: Admin, Faculty, and Student
- **Secure Password Encryption** - BCrypt password hashing

### 👨‍🎓 Student Features
- Student registration and profile management
- Course enrollment and viewing
- Real-time attendance tracking with percentage calculation
- Grade viewing and GPA calculation
- **PDF Report Card Generation** - Download comprehensive report cards
- **AI-Powered Query Solver** - Ask questions and get instant AI-generated answers
- Email notifications for grades and attendance alerts
- Document upload functionality

### 👨‍🏫 Faculty Features
- Faculty dashboard with course overview
- Mark attendance for enrolled students
- Enter and manage student grades
- View course-wise student lists
- Answer student queries manually
- Automatic email notifications to students

### 👨‍💼 Admin Features
- Complete system dashboard with statistics
- Student management (Create, Read, Update, Delete)
- Faculty management
- Course management and assignment
- View all enrollments and system metrics

### 📧 Email Notifications
- Welcome emails on registration
- Grade update notifications
- Attendance alert emails (when below 75%)
- Query answer notifications

### 🤖 AI Query Solver
- Automatic AI-powered responses to student questions
- Category-based query organization (Academic, Administrative, Technical, etc.)
- Manual override by faculty for complex queries
- Query history and status tracking

### 📊 Reporting & Analytics
- Attendance percentage calculation
- GPA calculation (course-wise and overall)
- PDF report card generation with iText
- Real-time statistics dashboard

## 🛠️ Tech Stack

### Backend
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Security**: Spring Security + JWT (jjwt 0.12.3)
- **Database**: MySQL 8.0+
- **ORM**: Spring Data JPA / Hibernate
- **PDF Generation**: iText 7
- **Email**: JavaMailSender
- **Build Tool**: Maven

### Frontend
- **HTML5** - Semantic markup
- **CSS3** - Modern responsive design with CSS Grid and Flexbox
- **JavaScript (ES6+)** - Vanilla JS for dynamic interactions
- **Fetch API** - RESTful API communication

### Database
- **MySQL** - Relational database
- **JPA/Hibernate** - ORM with automatic schema generation

## 📁 Project Structure

```
institute-management-system/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/institute/management/
│   │   │   │   ├── config/          # Security & app configuration
│   │   │   │   ├── controller/      # REST API controllers
│   │   │   │   ├── dto/             # Data Transfer Objects
│   │   │   │   ├── entity/          # JPA entities
│   │   │   │   ├── repository/      # Data access layer
│   │   │   │   ├── security/        # JWT & authentication
│   │   │   │   ├── service/         # Business logic
│   │   │   │   └── InstituteManagementApplication.java
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   └── pom.xml
├── frontend/
│   ├── css/
│   │   └── style.css
│   ├── js/
│   │   ├── auth.js
│   │   ├── config.js
│   │   ├── student.js
│   │   ├── faculty.js
│   │   └── admin.js
│   ├── pages/
│   │   ├── student-dashboard.html
│   │   ├── faculty-dashboard.html
│   │   └── admin-dashboard.html
│   └── index.html
└── README.md
```

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- Node.js (optional, for development server)

### Database Setup

1. **Create MySQL Database**:
```sql
CREATE DATABASE institute_db;
```

2. **Update Database Configuration**:
Edit `backend/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/institute_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
```

### Email Configuration

Configure email settings in `application.properties`:
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

**For Gmail**:
1. Enable 2-Factor Authentication
2. Generate an App Password
3. Use the App Password in the configuration

### AI Query Solver Configuration (Optional)

To enable AI-powered query responses, add your OpenAI API key:
```properties
openai.api.key=your-openai-api-key-here
```

If not configured, queries will remain pending for manual faculty response.

### Backend Setup

1. **Navigate to backend directory**:
```bash
cd backend
```

2. **Build the project**:
```bash
mvn clean install
```

3. **Run the application**:
```bash
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

### Frontend Setup

1. **Navigate to frontend directory**:
```bash
cd frontend
```

2. **Update API URL** (if needed):
Edit `frontend/js/config.js`:
```javascript
const API_URL = 'http://localhost:8080/api';
```

3. **Serve the frontend**:

**Option 1: Using Python**:
```bash
python -m http.server 8000
```

**Option 2: Using Node.js**:
```bash
npx http-server -p 8000
```

**Option 3: Using VS Code Live Server**:
- Install Live Server extension
- Right-click on `index.html` and select "Open with Live Server"

Access the application at `http://localhost:8000`

## 👥 Default User Roles

After starting the application, you can register users with different roles:

### Admin
- Full system access
- Can create students, faculty, and courses
- View all system statistics

### Faculty
- Manage assigned courses
- Mark attendance
- Enter grades
- Answer student queries

### Student
- View enrolled courses
- Check attendance and grades
- Download report cards
- Ask questions via AI query solver

## 📚 API Documentation

### Authentication Endpoints

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "1234567890",
  "role": "STUDENT"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 1,
  "email": "user@example.com",
  "role": "STUDENT",
  "firstName": "John",
  "lastName": "Doe"
}
```

### Student Endpoints

All student endpoints require `Authorization: Bearer <token>` header.

#### Get Student Profile
```http
GET /api/student/profile
```

#### Get Enrolled Courses
```http
GET /api/student/courses
```

#### Enroll in Course
```http
POST /api/student/enroll/{courseId}
```

#### Get Attendance
```http
GET /api/student/attendance
```

#### Get Grades
```http
GET /api/student/grades
```

#### Download Report Card
```http
GET /api/student/report-card
```

#### Submit Query
```http
POST /api/student/query
Content-Type: application/json

{
  "category": "ACADEMIC",
  "subject": "Question about assignment",
  "question": "When is the assignment due?"
}
```

#### Get My Queries
```http
GET /api/student/queries
```

### Faculty Endpoints

#### Get Faculty Profile
```http
GET /api/faculty/profile
```

#### Get My Courses
```http
GET /api/faculty/courses
```

#### Get Course Students
```http
GET /api/faculty/course/{courseId}/students
```

#### Mark Attendance
```http
POST /api/faculty/attendance/mark
Content-Type: application/json

{
  "courseId": 1,
  "date": "2024-01-15",
  "attendance": [
    {
      "studentId": 1,
      "status": "PRESENT"
    },
    {
      "studentId": 2,
      "status": "ABSENT"
    }
  ]
}
```

#### Add Grade
```http
POST /api/faculty/grade/add
Content-Type: application/json

{
  "studentId": 1,
  "courseId": 1,
  "examType": "MIDTERM",
  "marksObtained": 85,
  "totalMarks": 100
}
```

#### Get Pending Queries
```http
GET /api/faculty/queries/pending
```

#### Answer Query
```http
POST /api/faculty/query/{queryId}/answer
Content-Type: application/json

{
  "answer": "The assignment is due on January 20th."
}
```

### Admin Endpoints

#### Get Dashboard Statistics
```http
GET /api/admin/dashboard
```

#### Create Student
```http
POST /api/admin/student/create
Content-Type: application/json

{
  "email": "student@example.com",
  "password": "password123",
  "firstName": "Jane",
  "lastName": "Smith",
  "phoneNumber": "1234567890",
  "rollNumber": "CS2024001",
  "dateOfBirth": "2000-01-01",
  "address": "123 Main St",
  "guardianName": "John Smith",
  "guardianPhone": "0987654321",
  "department": "Computer Science",
  "semester": "1",
  "bloodGroup": "A+"
}
```

#### Create Faculty
```http
POST /api/admin/faculty/create
Content-Type: application/json

{
  "email": "faculty@example.com",
  "password": "password123",
  "firstName": "Dr. Robert",
  "lastName": "Johnson",
  "phoneNumber": "1234567890",
  "employeeId": "FAC001",
  "department": "Computer Science",
  "designation": "Professor",
  "qualification": "Ph.D.",
  "specialization": "Machine Learning",
  "joiningDate": "2020-01-01",
  "officeRoom": "A-101"
}
```

#### Create Course
```http
POST /api/admin/course/create
Content-Type: application/json

{
  "courseCode": "CS101",
  "courseName": "Introduction to Programming",
  "description": "Basic programming concepts",
  "credits": 4,
  "department": "Computer Science",
  "semester": "1",
  "facultyId": 1,
  "schedule": "Mon-Wed-Fri 10:00-11:00",
  "room": "Lab-1",
  "maxStudents": 60
}
```

## 🎨 Features Explanation

### JWT Authentication Flow
1. User logs in with email and password
2. Server validates credentials and generates JWT token
3. Token is stored in localStorage on client side
4. All subsequent requests include token in Authorization header
5. Server validates token and extracts user information

### Grade Calculation System
- Supports multiple exam types: Midterm, Final, Assignment, Quiz, Project, Practical
- Automatic letter grade calculation based on percentage:
  - 90%+ : A+ (4.0)
  - 85-89%: A (3.7)
  - 80-84%: A- (3.3)
  - 75-79%: B+ (3.0)
  - 70-74%: B (2.7)
  - 65-69%: B- (2.3)
  - 60-64%: C+ (2.0)
  - 55-59%: C (1.7)
  - 50-54%: C- (1.3)
  - <50%  : F (0.0)
- Overall GPA calculation across all courses

### Attendance System
- Daily attendance marking by faculty
- Status options: Present, Absent, Late, Excused
- Automatic percentage calculation
- Email alerts when attendance falls below 75%
- Course-wise and overall attendance tracking

### AI Query Solver
- Automatic categorization of queries
- AI-generated responses using OpenAI API
- Fallback to manual faculty response
- Email notifications when queries are answered
- Query history and status tracking

### PDF Report Card
- Professional report card generation using iText
- Includes student information, grades, and GPA
- Downloadable in PDF format
- Timestamped for record-keeping

## 🚀 Deployment

### AWS EC2 Deployment

#### 1. Launch EC2 Instance
- Choose Ubuntu Server 22.04 LTS
- Instance type: t2.medium or higher
- Configure security group:
  - Port 22 (SSH)
  - Port 8080 (Backend)
  - Port 80 (Frontend)
  - Port 3306 (MySQL - only if using EC2 MySQL)

#### 2. Install Java
```bash
sudo apt update
sudo apt install openjdk-17-jdk -y
java -version
```

#### 3. Install Maven
```bash
sudo apt install maven -y
mvn -version
```

#### 4. Install MySQL
```bash
sudo apt install mysql-server -y
sudo mysql_secure_installation
```

#### 5. Configure MySQL
```bash
sudo mysql -u root -p

CREATE DATABASE institute_db;
CREATE USER 'institute_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON institute_db.* TO 'institute_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

#### 6. Deploy Backend
```bash
# Clone or upload your project
cd backend

# Update application.properties with production settings
nano src/main/resources/application.properties

# Build the application
mvn clean package -DskipTests

# Run the application
nohup java -jar target/management-system-1.0.0.jar > app.log 2>&1 &
```

#### 7. Deploy Frontend
```bash
# Install Nginx
sudo apt install nginx -y

# Copy frontend files
sudo cp -r frontend/* /var/www/html/

# Update API URL in config.js
sudo nano /var/www/html/js/config.js
# Change API_URL to your EC2 public IP or domain

# Restart Nginx
sudo systemctl restart nginx
```

### Cloud Database Options

#### Railway MySQL
1. Sign up at [railway.app](https://railway.app)
2. Create new MySQL database
3. Copy connection details
4. Update `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://containers-us-west-xxx.railway.app:xxxx/railway
spring.datasource.username=root
spring.datasource.password=your_password
```

#### PlanetScale MySQL
1. Sign up at [planetscale.com](https://planetscale.com)
2. Create new database
3. Get connection string
4. Update `application.properties` with connection details

### Render Deployment (Alternative)

1. Create account on [render.com](https://render.com)
2. Create new Web Service
3. Connect your GitHub repository
4. Configure:
   - Build Command: `cd backend && mvn clean package -DskipTests`
   - Start Command: `java -jar backend/target/management-system-1.0.0.jar`
5. Add environment variables for database and email

## 🔧 Configuration

### Environment Variables (Production)

Create `application-prod.properties`:
```properties
# Server
server.port=${PORT:8080}

# Database
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# JWT
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000

# Email
spring.mail.username=${EMAIL_USERNAME}
spring.mail.password=${EMAIL_PASSWORD}

# OpenAI
openai.api.key=${OPENAI_API_KEY}
```

Run with production profile:
```bash
java -jar -Dspring.profiles.active=prod target/management-system-1.0.0.jar
```

## 🧪 Testing

### Manual Testing Checklist

#### Authentication
- [ ] Register new user (Student, Faculty, Admin)
- [ ] Login with valid credentials
- [ ] Login with invalid credentials
- [ ] JWT token generation and validation
- [ ] Role-based access control

#### Student Features
- [ ] View profile
- [ ] Enroll in courses
- [ ] View enrolled courses
- [ ] Check attendance
- [ ] View grades and GPA
- [ ] Download report card
- [ ] Submit query
- [ ] View query responses

#### Faculty Features
- [ ] View assigned courses
- [ ] View course students
- [ ] Mark attendance
- [ ] Enter grades
- [ ] View pending queries
- [ ] Answer queries

#### Admin Features
- [ ] View dashboard statistics
- [ ] Create student
- [ ] Create faculty
- [ ] Create course
- [ ] Assign faculty to course
- [ ] View all students/faculty/courses

## 📝 Additional Features to Implement

### Phase 2 Enhancements
- [ ] Timetable management
- [ ] Exam scheduling
- [ ] Library management
- [ ] Fee management
- [ ] Hostel management
- [ ] Transport management
- [ ] Event management
- [ ] Notice board
- [ ] Chat system between students and faculty
- [ ] Mobile app (React Native)
- [ ] Advanced analytics and reports
- [ ] Bulk operations (CSV import/export)
- [ ] Multi-language support
- [ ] Dark mode

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License.

## 👨‍💻 Author

Created with ❤️ for educational institutions

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- iText for PDF generation
- OpenAI for AI capabilities
- All contributors and testers

## 📞 Support

For support, email support@institute.edu or create an issue in the repository.

---

**Happy Learning! 🎓**