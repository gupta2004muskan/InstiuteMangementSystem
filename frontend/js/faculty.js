checkAuthAndRedirect();

const user = getUser();
let currentCourseStudents = [];

// Initialize dashboard
document.addEventListener('DOMContentLoaded', () => {
    displayUserInfo();
    loadDashboardData();
    showSection('dashboard');
    
    // Set today's date for attendance
    document.getElementById('attendance-date').valueAsDate = new Date();
});

function displayUserInfo() {
    const userName = `${user.firstName} ${user.lastName}`;
    document.getElementById('user-name').textContent = userName;
    document.getElementById('welcome-name').textContent = user.firstName;
    document.getElementById('user-avatar').textContent = user.firstName.charAt(0) + user.lastName.charAt(0);
}

function showSection(sectionName) {
    const sections = document.querySelectorAll('.section');
    sections.forEach(section => section.classList.remove('active'));
    
    const targetSection = document.getElementById(`${sectionName}-section`);
    if (targetSection) {
        targetSection.classList.add('active');
        targetSection.style.display = 'block';
    }
    
    // Load data for the section
    switch(sectionName) {
        case 'courses':
            loadMyCourses();
            break;
        case 'attendance':
            loadCoursesForAttendance();
            break;
        case 'grades':
            loadCoursesForGrades();
            break;
        case 'queries':
            loadPendingQueries();
            break;
    }
}

async function loadDashboardData() {
    try {
        // Load courses
        const coursesResponse = await fetchAPI('/faculty/courses');
        const coursesData = await coursesResponse.json();
        if (coursesData.success) {
            document.getElementById('total-courses').textContent = coursesData.data.length;
            
            // Calculate total students
            let totalStudents = 0;
            for (const course of coursesData.data) {
                const studentsResponse = await fetchAPI(`/faculty/course/${course.id}/students`);
                const studentsData = await studentsResponse.json();
                if (studentsData.success) {
                    totalStudents += studentsData.data.length;
                }
            }
            document.getElementById('total-students').textContent = totalStudents;
        }
        
        // Load pending queries
        const queriesResponse = await fetchAPI('/faculty/queries/pending');
        const queriesData = await queriesResponse.json();
        if (queriesData.success) {
            document.getElementById('pending-queries').textContent = queriesData.data.length;
        }
    } catch (error) {
        console.error('Error loading dashboard data:', error);
    }
}

async function loadMyCourses() {
    try {
        const response = await fetchAPI('/faculty/courses');
        const data = await response.json();
        
        const tbody = document.getElementById('courses-tbody');
        
        if (data.success && data.data.length > 0) {
            const coursesHTML = await Promise.all(data.data.map(async course => {
                const studentsResponse = await fetchAPI(`/faculty/course/${course.id}/students`);
                const studentsData = await studentsResponse.json();
                const studentCount = studentsData.success ? studentsData.data.length : 0;
                
                return `
                    <tr>
                        <td>${course.courseCode}</td>
                        <td>${course.courseName}</td>
                        <td>${course.department}</td>
                        <td>${course.semester}</td>
                        <td>${studentCount}</td>
                        <td>
                            <button class="btn btn-sm btn-primary" onclick="viewCourseDetails(${course.id})">
                                View Details
                            </button>
                        </td>
                    </tr>
                `;
            }));
            
            tbody.innerHTML = coursesHTML.join('');
        } else {
            tbody.innerHTML = '<tr><td colspan="6" class="empty-state">No courses assigned yet.</td></tr>';
        }
    } catch (error) {
        console.error('Error loading courses:', error);
        showNotification('Failed to load courses', 'error');
    }
}

async function loadCoursesForAttendance() {
    try {
        const response = await fetchAPI('/faculty/courses');
        const data = await response.json();
        
        const select = document.getElementById('attendance-course');
        
        if (data.success && data.data.length > 0) {
            select.innerHTML = '<option value="">Select a course</option>' +
                data.data.map(course => 
                    `<option value="${course.id}">${course.courseCode} - ${course.courseName}</option>`
                ).join('');
        }
    } catch (error) {
        console.error('Error loading courses:', error);
    }
}

async function loadCourseStudents() {
    const courseId = document.getElementById('attendance-course').value;
    const container = document.getElementById('students-attendance-container');
    
    if (!courseId) {
        container.innerHTML = '<p class="empty-state">Select a course to mark attendance</p>';
        return;
    }
    
    try {
        const response = await fetchAPI(`/faculty/course/${courseId}/students`);
        const data = await response.json();
        
        if (data.success && data.data.length > 0) {
            currentCourseStudents = data.data;
            container.innerHTML = `
                <table>
                    <thead>
                        <tr>
                            <th>Roll Number</th>
                            <th>Student Name</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${data.data.map(enrollment => `
                            <tr>
                                <td>${enrollment.student.rollNumber}</td>
                                <td>${enrollment.student.user.firstName} ${enrollment.student.user.lastName}</td>
                                <td>
                                    <select class="attendance-status" data-student-id="${enrollment.student.id}">
                                        <option value="PRESENT">Present</option>
                                        <option value="ABSENT">Absent</option>
                                        <option value="LATE">Late</option>
                                        <option value="EXCUSED">Excused</option>
                                    </select>
                                </td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            `;
        } else {
            container.innerHTML = '<p class="empty-state">No students enrolled in this course.</p>';
        }
    } catch (error) {
        console.error('Error loading students:', error);
        showNotification('Failed to load students', 'error');
    }
}

async function submitAttendance() {
    const courseId = document.getElementById('attendance-course').value;
    const date = document.getElementById('attendance-date').value;
    
    if (!courseId || !date) {
        showNotification('Please select course and date', 'error');
        return;
    }
    
    const statusSelects = document.querySelectorAll('.attendance-status');
    const attendance = Array.from(statusSelects).map(select => ({
        studentId: select.dataset.studentId,
        status: select.value
    }));
    
    try {
        const response = await fetchAPI('/faculty/attendance/mark', {
            method: 'POST',
            body: JSON.stringify({
                courseId,
                date,
                attendance
            })
        });
        
        const data = await response.json();
        
        if (data.success) {
            showNotification('Attendance marked successfully!', 'success');
            document.getElementById('students-attendance-container').innerHTML = 
                '<p class="empty-state">Select a course to mark attendance</p>';
            document.getElementById('attendance-course').value = '';
        } else {
            showNotification(data.message, 'error');
        }
    } catch (error) {
        console.error('Error submitting attendance:', error);
        showNotification('Failed to submit attendance', 'error');
    }
}

async function loadCoursesForGrades() {
    try {
        const response = await fetchAPI('/faculty/courses');
        const data = await response.json();
        
        const select = document.getElementById('grades-course');
        
        if (data.success && data.data.length > 0) {
            select.innerHTML = '<option value="">Select a course</option>' +
                data.data.map(course => 
                    `<option value="${course.id}">${course.courseCode} - ${course.courseName}</option>`
                ).join('');
        }
    } catch (error) {
        console.error('Error loading courses:', error);
    }
}

async function loadCourseStudentsForGrades() {
    const courseId = document.getElementById('grades-course').value;
    const container = document.getElementById('students-grades-container');
    
    if (!courseId) {
        container.innerHTML = '<p class="empty-state">Select a course to enter grades</p>';
        return;
    }
    
    try {
        const response = await fetchAPI(`/faculty/course/${courseId}/students`);
        const data = await response.json();
        
        if (data.success && data.data.length > 0) {
            container.innerHTML = `
                <table>
                    <thead>
                        <tr>
                            <th>Roll Number</th>
                            <th>Student Name</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${data.data.map(enrollment => `
                            <tr>
                                <td>${enrollment.student.rollNumber}</td>
                                <td>${enrollment.student.user.firstName} ${enrollment.student.user.lastName}</td>
                                <td>
                                    <button class="btn btn-sm btn-primary" 
                                            onclick="openGradeModal(${enrollment.student.id}, '${enrollment.student.user.firstName} ${enrollment.student.user.lastName}', ${courseId})">
                                        Enter Grade
                                    </button>
                                </td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            `;
        } else {
            container.innerHTML = '<p class="empty-state">No students enrolled in this course.</p>';
        }
    } catch (error) {
        console.error('Error loading students:', error);
        showNotification('Failed to load students', 'error');
    }
}

function openGradeModal(studentId, studentName, courseId) {
    document.getElementById('grade-student-id').value = studentId;
    document.getElementById('grade-student-name').value = studentName;
    document.getElementById('grade-course-id').value = courseId;
    document.getElementById('grade-modal').classList.add('active');
}

async function submitGrade(event) {
    event.preventDefault();
    
    const studentId = document.getElementById('grade-student-id').value;
    const courseId = document.getElementById('grade-course-id').value;
    const examType = document.getElementById('grade-exam-type').value;
    const marksObtained = document.getElementById('grade-marks-obtained').value;
    const totalMarks = document.getElementById('grade-total-marks').value;
    
    try {
        const response = await fetchAPI('/faculty/grade/add', {
            method: 'POST',
            body: JSON.stringify({
                studentId,
                courseId,
                examType,
                marksObtained,
                totalMarks
            })
        });
        
        const data = await response.json();
        
        if (data.success) {
            showNotification('Grade added successfully!', 'success');
            closeModal('grade-modal');
            document.getElementById('grade-exam-type').value = 'MIDTERM';
            document.getElementById('grade-marks-obtained').value = '';
            document.getElementById('grade-total-marks').value = '';
        } else {
            showNotification(data.message, 'error');
        }
    } catch (error) {
        console.error('Error submitting grade:', error);
        showNotification('Failed to submit grade', 'error');
    }
}

async function loadPendingQueries() {
    try {
        const response = await fetchAPI('/faculty/queries/pending');
        const data = await response.json();
        
        const container = document.getElementById('queries-container');
        
        if (data.success && data.data.length > 0) {
            container.innerHTML = data.data.map(query => `
                <div class="card" style="margin-bottom: 20px;">
                    <div style="display: flex; justify-content: space-between; align-items: start; margin-bottom: 15px;">
                        <div>
                            <h3>${query.subject}</h3>
                            <p style="color: var(--text-light); margin: 5px 0;">
                                Student: ${query.student.user.firstName} ${query.student.user.lastName} 
                                (${query.student.rollNumber})
                            </p>
                            <span class="badge badge-info">${query.category}</span>
                        </div>
                        <small style="color: var(--text-light);">${formatDateTime(query.createdAt)}</small>
                    </div>
                    <div style="background: var(--light-bg); padding: 15px; border-radius: 8px; margin-bottom: 15px;">
                        <strong>Question:</strong>
                        <p>${query.question}</p>
                    </div>
                    <button class="btn btn-primary" onclick="openAnswerModal(${query.id}, '${query.question.replace(/'/g, "\\'")}')">
                        Answer Query
                    </button>
                </div>
            `).join('');
        } else {
            container.innerHTML = '<p class="empty-state">No pending queries.</p>';
        }
    } catch (error) {
        console.error('Error loading queries:', error);
        showNotification('Failed to load queries', 'error');
    }
}

function openAnswerModal(queryId, question) {
    document.getElementById('answer-query-id').value = queryId;
    document.getElementById('answer-question').value = question;
    document.getElementById('answer-query-modal').classList.add('active');
}

async function submitAnswer(event) {
    event.preventDefault();
    
    const queryId = document.getElementById('answer-query-id').value;
    const answer = document.getElementById('answer-text').value;
    
    try {
        const response = await fetchAPI(`/faculty/query/${queryId}/answer`, {
            method: 'POST',
            body: JSON.stringify({ answer })
        });
        
        const data = await response.json();
        
        if (data.success) {
            showNotification('Answer submitted successfully!', 'success');
            closeModal('answer-query-modal');
            document.getElementById('answer-text').value = '';
            loadPendingQueries();
            loadDashboardData();
        } else {
            showNotification(data.message, 'error');
        }
    } catch (error) {
        console.error('Error submitting answer:', error);
        showNotification('Failed to submit answer', 'error');
    }
}

function viewCourseDetails(courseId) {
    showNotification('Course details feature coming soon!', 'success');
}

function closeModal(modalId) {
    document.getElementById(modalId).classList.remove('active');
}

// Hide all sections except dashboard initially
document.querySelectorAll('.section').forEach((section, index) => {
    if (index !== 0) {
        section.style.display = 'none';
    }
});