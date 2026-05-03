checkAuthAndRedirect();

const user = getUser();

// Initialize dashboard
document.addEventListener('DOMContentLoaded', () => {
    displayUserInfo();
    loadDashboardData();
    showSection('dashboard');
});

function displayUserInfo() {
    const userName = `${user.firstName} ${user.lastName}`;
    document.getElementById('user-name').textContent = userName;
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
        case 'students':
            loadStudents();
            break;
        case 'faculty':
            loadFaculty();
            break;
        case 'courses':
            loadCourses();
            loadFacultyForDropdown();
            break;
    }
}

async function loadDashboardData() {
    try {
        const response = await fetchAPI('/admin/dashboard');
        const data = await response.json();
        
        if (data.success) {
            document.getElementById('total-students').textContent = data.data.totalStudents;
            document.getElementById('total-faculty').textContent = data.data.totalFaculty;
            document.getElementById('total-courses').textContent = data.data.totalCourses;
            document.getElementById('total-enrollments').textContent = data.data.totalEnrollments;
        }
    } catch (error) {
        console.error('Error loading dashboard data:', error);
    }
}

async function loadStudents() {
    try {
        const response = await fetchAPI('/admin/students');
        const data = await response.json();
        
        const tbody = document.getElementById('students-tbody');
        
        if (data.success && data.data.length > 0) {
            tbody.innerHTML = data.data.map(student => `
                <tr>
                    <td>${student.rollNumber}</td>
                    <td>${student.user.firstName} ${student.user.lastName}</td>
                    <td>${student.user.email}</td>
                    <td>${student.department}</td>
                    <td>${student.semester}</td>
                    <td>
                        <button class="btn btn-sm btn-secondary" onclick="editStudent(${student.id})">
                            Edit
                        </button>
                        <button class="btn btn-sm btn-danger" onclick="deleteStudent(${student.id})">
                            Delete
                        </button>
                    </td>
                </tr>
            `).join('');
        } else {
            tbody.innerHTML = '<tr><td colspan="6" class="empty-state">No students found.</td></tr>';
        }
    } catch (error) {
        console.error('Error loading students:', error);
        showNotification('Failed to load students', 'error');
    }
}

async function addStudent(event) {
    event.preventDefault();
    
    const studentData = {
        email: document.getElementById('student-email').value,
        password: document.getElementById('student-password').value,
        firstName: document.getElementById('student-firstname').value,
        lastName: document.getElementById('student-lastname').value,
        phoneNumber: document.getElementById('student-phone').value,
        rollNumber: document.getElementById('student-rollnumber').value,
        dateOfBirth: document.getElementById('student-dob').value,
        address: document.getElementById('student-address').value,
        guardianName: document.getElementById('student-guardian-name').value,
        guardianPhone: document.getElementById('student-guardian-phone').value,
        department: document.getElementById('student-department').value,
        semester: document.getElementById('student-semester').value,
        bloodGroup: document.getElementById('student-bloodgroup').value
    };
    
    try {
        const response = await fetchAPI('/admin/student/create', {
            method: 'POST',
            body: JSON.stringify(studentData)
        });
        
        const data = await response.json();
        
        if (data.success) {
            showNotification('Student added successfully!', 'success');
            closeModal('add-student-modal');
            loadStudents();
            loadDashboardData();
            event.target.reset();
        } else {
            showNotification(data.message, 'error');
        }
    } catch (error) {
        console.error('Error adding student:', error);
        showNotification('Failed to add student', 'error');
    }
}

async function loadFaculty() {
    try {
        const response = await fetchAPI('/admin/faculty');
        const data = await response.json();
        
        const tbody = document.getElementById('faculty-tbody');
        
        if (data.success && data.data.length > 0) {
            tbody.innerHTML = data.data.map(faculty => `
                <tr>
                    <td>${faculty.employeeId}</td>
                    <td>${faculty.user.firstName} ${faculty.user.lastName}</td>
                    <td>${faculty.user.email}</td>
                    <td>${faculty.department}</td>
                    <td>${faculty.designation}</td>
                    <td>
                        <button class="btn btn-sm btn-secondary" onclick="editFaculty(${faculty.id})">
                            Edit
                        </button>
                    </td>
                </tr>
            `).join('');
        } else {
            tbody.innerHTML = '<tr><td colspan="6" class="empty-state">No faculty found.</td></tr>';
        }
    } catch (error) {
        console.error('Error loading faculty:', error);
        showNotification('Failed to load faculty', 'error');
    }
}

async function addFaculty(event) {
    event.preventDefault();
    
    const facultyData = {
        email: document.getElementById('faculty-email').value,
        password: document.getElementById('faculty-password').value,
        firstName: document.getElementById('faculty-firstname').value,
        lastName: document.getElementById('faculty-lastname').value,
        phoneNumber: document.getElementById('faculty-phone').value,
        employeeId: document.getElementById('faculty-employeeid').value,
        department: document.getElementById('faculty-department').value,
        designation: document.getElementById('faculty-designation').value,
        qualification: document.getElementById('faculty-qualification').value,
        specialization: document.getElementById('faculty-specialization').value,
        joiningDate: document.getElementById('faculty-joiningdate').value,
        officeRoom: document.getElementById('faculty-officeroom').value
    };
    
    try {
        const response = await fetchAPI('/admin/faculty/create', {
            method: 'POST',
            body: JSON.stringify(facultyData)
        });
        
        const data = await response.json();
        
        if (data.success) {
            showNotification('Faculty added successfully!', 'success');
            closeModal('add-faculty-modal');
            loadFaculty();
            loadDashboardData();
            event.target.reset();
        } else {
            showNotification(data.message, 'error');
        }
    } catch (error) {
        console.error('Error adding faculty:', error);
        showNotification('Failed to add faculty', 'error');
    }
}

async function loadCourses() {
    try {
        const response = await fetchAPI('/admin/courses');
        const data = await response.json();
        
        const tbody = document.getElementById('courses-tbody');
        
        if (data.success && data.data.length > 0) {
            tbody.innerHTML = data.data.map(course => `
                <tr>
                    <td>${course.courseCode}</td>
                    <td>${course.courseName}</td>
                    <td>${course.department}</td>
                    <td>${course.semester}</td>
                    <td>${course.credits}</td>
                    <td>${course.faculty ? course.faculty.user.firstName + ' ' + course.faculty.user.lastName : 'Not Assigned'}</td>
                    <td>
                        <button class="btn btn-sm btn-secondary" onclick="editCourse(${course.id})">
                            Edit
                        </button>
                    </td>
                </tr>
            `).join('');
        } else {
            tbody.innerHTML = '<tr><td colspan="7" class="empty-state">No courses found.</td></tr>';
        }
    } catch (error) {
        console.error('Error loading courses:', error);
        showNotification('Failed to load courses', 'error');
    }
}

async function loadFacultyForDropdown() {
    try {
        const response = await fetchAPI('/admin/faculty');
        const data = await response.json();
        
        const select = document.getElementById('course-faculty');
        
        if (data.success && data.data.length > 0) {
            select.innerHTML = '<option value="">Select Faculty (Optional)</option>' +
                data.data.map(faculty => 
                    `<option value="${faculty.id}">${faculty.user.firstName} ${faculty.user.lastName} - ${faculty.department}</option>`
                ).join('');
        }
    } catch (error) {
        console.error('Error loading faculty:', error);
    }
}

async function addCourse(event) {
    event.preventDefault();
    
    const courseData = {
        courseCode: document.getElementById('course-code').value,
        courseName: document.getElementById('course-name').value,
        description: document.getElementById('course-description').value,
        credits: document.getElementById('course-credits').value,
        department: document.getElementById('course-department').value,
        semester: document.getElementById('course-semester').value,
        schedule: document.getElementById('course-schedule').value,
        room: document.getElementById('course-room').value,
        maxStudents: document.getElementById('course-maxstudents').value
    };
    
    const facultyId = document.getElementById('course-faculty').value;
    if (facultyId) {
        courseData.facultyId = facultyId;
    }
    
    try {
        const response = await fetchAPI('/admin/course/create', {
            method: 'POST',
            body: JSON.stringify(courseData)
        });
        
        const data = await response.json();
        
        if (data.success) {
            showNotification('Course added successfully!', 'success');
            closeModal('add-course-modal');
            loadCourses();
            loadDashboardData();
            event.target.reset();
        } else {
            showNotification(data.message, 'error');
        }
    } catch (error) {
        console.error('Error adding course:', error);
        showNotification('Failed to add course', 'error');
    }
}

async function deleteStudent(studentId) {
    if (!confirm('Are you sure you want to delete this student?')) {
        return;
    }
    
    try {
        const response = await fetchAPI(`/admin/student/${studentId}`, {
            method: 'DELETE'
        });
        
        const data = await response.json();
        
        if (data.success) {
            showNotification('Student deleted successfully!', 'success');
            loadStudents();
            loadDashboardData();
        } else {
            showNotification(data.message, 'error');
        }
    } catch (error) {
        console.error('Error deleting student:', error);
        showNotification('Failed to delete student', 'error');
    }
}

function editStudent(studentId) {
    showNotification('Edit feature coming soon!', 'success');
}

function editFaculty(facultyId) {
    showNotification('Edit feature coming soon!', 'success');
}

function editCourse(courseId) {
    showNotification('Edit feature coming soon!', 'success');
}

function openModal(modalId) {
    document.getElementById(modalId).classList.add('active');
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