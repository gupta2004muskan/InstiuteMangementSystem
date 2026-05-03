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
            loadAttendance();
            break;
        case 'grades':
            loadGrades();
            break;
        case 'queries':
            loadQueries();
            break;
    }
}

async function loadDashboardData() {
    try {
        // Load courses count
        const coursesResponse = await fetchAPI('/student/courses');
        const coursesData = await coursesResponse.json();
        if (coursesData.success) {
            document.getElementById('enrolled-courses').textContent = coursesData.data.length;
        }
        
        // Load attendance
        const attendanceResponse = await fetchAPI('/student/attendance');
        const attendanceData = await attendanceResponse.json();
        if (attendanceData.success) {
            const attendanceValues = Object.values(attendanceData.data);
            if (attendanceValues.length > 0) {
                const avgAttendance = attendanceValues.reduce((sum, course) => sum + course.percentage, 0) / attendanceValues.length;
                document.getElementById('avg-attendance').textContent = avgAttendance.toFixed(1) + '%';
            }
        }
        
        // Load GPA
        const gradesResponse = await fetchAPI('/student/grades');
        const gradesData = await gradesResponse.json();
        if (gradesData.success && gradesData.data.gpa) {
            document.getElementById('current-gpa').textContent = gradesData.data.gpa.toFixed(2);
        }
        
        // Load queries
        const queriesResponse = await fetchAPI('/student/queries');
        const queriesData = await queriesResponse.json();
        if (queriesData.success) {
            const pendingQueries = queriesData.data.filter(q => q.status === 'PENDING').length;
            document.getElementById('pending-queries').textContent = pendingQueries;
        }
    } catch (error) {
        console.error('Error loading dashboard data:', error);
    }
}

async function loadMyCourses() {
    try {
        const response = await fetchAPI('/student/courses');
        const data = await response.json();
        
        const tbody = document.getElementById('courses-tbody');
        
        if (data.success && data.data.length > 0) {
            tbody.innerHTML = data.data.map(enrollment => `
                <tr>
                    <td>${enrollment.course.courseCode}</td>
                    <td>${enrollment.course.courseName}</td>
                    <td>${enrollment.course.faculty ? enrollment.course.faculty.user.firstName + ' ' + enrollment.course.faculty.user.lastName : 'TBA'}</td>
                    <td>${enrollment.course.schedule || 'TBA'}</td>
                    <td>${enrollment.course.credits}</td>
                    <td><span class="badge badge-success">${enrollment.status}</span></td>
                </tr>
            `).join('');
        } else {
            tbody.innerHTML = '<tr><td colspan="6" class="empty-state">No courses enrolled yet.</td></tr>';
        }
    } catch (error) {
        console.error('Error loading courses:', error);
        showNotification('Failed to load courses', 'error');
    }
}

async function showAvailableCourses() {
    try {
        const response = await fetchAPI('/student/available-courses');
        const data = await response.json();
        
        const tbody = document.getElementById('available-courses-tbody');
        
        if (data.success && data.data.length > 0) {
            tbody.innerHTML = data.data.map(course => `
                <tr>
                    <td>${course.courseCode}</td>
                    <td>${course.courseName}</td>
                    <td>${course.credits}</td>
                    <td>
                        <button class="btn btn-sm btn-primary" onclick="enrollInCourse(${course.id})">
                            Enroll
                        </button>
                    </td>
                </tr>
            `).join('');
        } else {
            tbody.innerHTML = '<tr><td colspan="4" class="empty-state">No available courses.</td></tr>';
        }
        
        document.getElementById('available-courses-modal').classList.add('active');
    } catch (error) {
        console.error('Error loading available courses:', error);
        showNotification('Failed to load available courses', 'error');
    }
}

async function enrollInCourse(courseId) {
    try {
        const response = await fetchAPI(`/student/enroll/${courseId}`, {
            method: 'POST'
        });
        
        const data = await response.json();
        
        if (data.success) {
            showNotification('Successfully enrolled in course!', 'success');
            closeModal('available-courses-modal');
            loadMyCourses();
            loadDashboardData();
        } else {
            showNotification(data.message, 'error');
        }
    } catch (error) {
        console.error('Error enrolling in course:', error);
        showNotification('Failed to enroll in course', 'error');
    }
}

async function loadAttendance() {
    try {
        const response = await fetchAPI('/student/attendance');
        const data = await response.json();
        
        const container = document.getElementById('attendance-container');
        
        if (data.success && Object.keys(data.data).length > 0) {
            container.innerHTML = Object.entries(data.data).map(([courseCode, attendance]) => {
                const percentage = attendance.percentage;
                let progressClass = 'success';
                if (percentage < 75) progressClass = 'danger';
                else if (percentage < 85) progressClass = 'warning';
                
                return `
                    <div class="card" style="margin-bottom: 20px;">
                        <h3>${attendance.courseName}</h3>
                        <p>Present: ${attendance.present} / ${attendance.total} classes</p>
                        <p>Attendance: <strong>${percentage.toFixed(1)}%</strong></p>
                        <div class="progress-bar">
                            <div class="progress-fill ${progressClass}" style="width: ${percentage}%"></div>
                        </div>
                    </div>
                `;
            }).join('');
        } else {
            container.innerHTML = '<p class="empty-state">No attendance data available.</p>';
        }
    } catch (error) {
        console.error('Error loading attendance:', error);
        showNotification('Failed to load attendance', 'error');
    }
}

async function loadGrades() {
    try {
        const response = await fetchAPI('/student/grades');
        const data = await response.json();
        
        const tbody = document.getElementById('grades-tbody');
        
        if (data.success && data.data.grades.length > 0) {
            tbody.innerHTML = data.data.grades.map(grade => `
                <tr>
                    <td>${grade.course.courseName}</td>
                    <td>${grade.examType}</td>
                    <td>${grade.marksObtained}/${grade.totalMarks}</td>
                    <td><span class="badge badge-info">${grade.letterGrade}</span></td>
                    <td>${grade.gradePoint ? grade.gradePoint.toFixed(2) : 'N/A'}</td>
                </tr>
            `).join('');
            
            if (data.data.gpa) {
                document.getElementById('overall-gpa').textContent = data.data.gpa.toFixed(2);
            }
        } else {
            tbody.innerHTML = '<tr><td colspan="5" class="empty-state">No grades available yet.</td></tr>';
        }
    } catch (error) {
        console.error('Error loading grades:', error);
        showNotification('Failed to load grades', 'error');
    }
}

async function submitQuery(event) {
    event.preventDefault();
    
    const category = document.getElementById('query-category').value;
    const subject = document.getElementById('query-subject').value;
    const question = document.getElementById('query-question').value;
    
    try {
        const response = await fetchAPI('/student/query', {
            method: 'POST',
            body: JSON.stringify({ category, subject, question })
        });
        
        const data = await response.json();
        
        if (data.success) {
            showNotification('Question submitted successfully!', 'success');
            document.getElementById('query-subject').value = '';
            document.getElementById('query-question').value = '';
            loadQueries();
            loadDashboardData();
        } else {
            showNotification(data.message, 'error');
        }
    } catch (error) {
        console.error('Error submitting query:', error);
        showNotification('Failed to submit question', 'error');
    }
}

async function loadQueries() {
    try {
        const response = await fetchAPI('/student/queries');
        const data = await response.json();
        
        const container = document.getElementById('queries-list');
        
        if (data.success && data.data.length > 0) {
            container.innerHTML = data.data.map(query => {
                const statusClass = query.status === 'ANSWERED' ? 'success' : 'warning';
                return `
                    <div class="card" style="margin-bottom: 20px;">
                        <div style="display: flex; justify-content: space-between; align-items: start; margin-bottom: 15px;">
                            <div>
                                <h3>${query.subject}</h3>
                                <span class="badge badge-${statusClass}">${query.status}</span>
                                <span class="badge badge-info">${query.category}</span>
                            </div>
                            <small style="color: var(--text-light);">${formatDateTime(query.createdAt)}</small>
                        </div>
                        <div style="background: var(--light-bg); padding: 15px; border-radius: 8px; margin-bottom: 15px;">
                            <strong>Question:</strong>
                            <p>${query.question}</p>
                        </div>
                        ${query.answer ? `
                            <div style="background: #e3f2fd; padding: 15px; border-radius: 8px;">
                                <strong>Answer:</strong>
                                <p>${query.answer}</p>
                                <small style="color: var(--text-light);">
                                    Answered by: ${query.answeredBy} on ${formatDateTime(query.answeredAt)}
                                </small>
                            </div>
                        ` : '<p style="color: var(--text-light); font-style: italic;">Waiting for answer...</p>'}
                    </div>
                `;
            }).join('');
        } else {
            container.innerHTML = '<p class="empty-state">No questions submitted yet.</p>';
        }
    } catch (error) {
        console.error('Error loading queries:', error);
        showNotification('Failed to load questions', 'error');
    }
}

async function downloadReportCard() {
    try {
        const response = await fetch(`${API_URL}/student/report-card`, {
            headers: getAuthHeaders()
        });
        
        if (response.ok) {
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `report_card_${user.firstName}_${user.lastName}.pdf`;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
            showNotification('Report card downloaded successfully!', 'success');
        } else {
            showNotification('Failed to download report card', 'error');
        }
    } catch (error) {
        console.error('Error downloading report card:', error);
        showNotification('Failed to download report card', 'error');
    }
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