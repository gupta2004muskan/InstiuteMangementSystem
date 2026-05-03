package com.institute.management.controller;

import com.institute.management.dto.ApiResponse;
import com.institute.management.entity.*;
import com.institute.management.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private FacultyRepository facultyRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalStudents", studentRepository.count());
            stats.put("totalFaculty", facultyRepository.count());
            stats.put("totalCourses", courseRepository.count());
            stats.put("totalEnrollments", enrollmentRepository.count());
            
            return ResponseEntity.ok(new ApiResponse(true, "Dashboard stats retrieved", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @PostMapping("/student/create")
    public ResponseEntity<?> createStudent(@RequestBody Map<String, Object> studentData) {
        try {
            // Create user account
            User user = new User();
            user.setEmail(studentData.get("email").toString());
            user.setPassword(passwordEncoder.encode(studentData.get("password").toString()));
            user.setFirstName(studentData.get("firstName").toString());
            user.setLastName(studentData.get("lastName").toString());
            user.setPhoneNumber(studentData.get("phoneNumber").toString());
            user.setRole(User.Role.STUDENT);
            user.setActive(true);
            
            User savedUser = userRepository.save(user);
            
            // Create student profile
            Student student = new Student();
            student.setUser(savedUser);
            student.setRollNumber(studentData.get("rollNumber").toString());
            student.setDateOfBirth(LocalDate.parse(studentData.get("dateOfBirth").toString()));
            student.setAddress(studentData.get("address").toString());
            student.setGuardianName(studentData.get("guardianName").toString());
            student.setGuardianPhone(studentData.get("guardianPhone").toString());
            student.setDepartment(studentData.get("department").toString());
            student.setSemester(studentData.get("semester").toString());
            student.setBloodGroup(studentData.get("bloodGroup").toString());
            
            Student savedStudent = studentRepository.save(student);
            
            return ResponseEntity.ok(new ApiResponse(true, "Student created successfully", savedStudent));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @PostMapping("/faculty/create")
    public ResponseEntity<?> createFaculty(@RequestBody Map<String, Object> facultyData) {
        try {
            // Create user account
            User user = new User();
            user.setEmail(facultyData.get("email").toString());
            user.setPassword(passwordEncoder.encode(facultyData.get("password").toString()));
            user.setFirstName(facultyData.get("firstName").toString());
            user.setLastName(facultyData.get("lastName").toString());
            user.setPhoneNumber(facultyData.get("phoneNumber").toString());
            user.setRole(User.Role.FACULTY);
            user.setActive(true);
            
            User savedUser = userRepository.save(user);
            
            // Create faculty profile
            Faculty faculty = new Faculty();
            faculty.setUser(savedUser);
            faculty.setEmployeeId(facultyData.get("employeeId").toString());
            faculty.setDepartment(facultyData.get("department").toString());
            faculty.setDesignation(facultyData.get("designation").toString());
            faculty.setQualification(facultyData.get("qualification").toString());
            faculty.setSpecialization(facultyData.get("specialization").toString());
            faculty.setJoiningDate(LocalDate.parse(facultyData.get("joiningDate").toString()));
            faculty.setOfficeRoom(facultyData.get("officeRoom").toString());
            
            Faculty savedFaculty = facultyRepository.save(faculty);
            
            return ResponseEntity.ok(new ApiResponse(true, "Faculty created successfully", savedFaculty));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @PostMapping("/course/create")
    public ResponseEntity<?> createCourse(@RequestBody Map<String, Object> courseData) {
        try {
            Course course = new Course();
            course.setCourseCode(courseData.get("courseCode").toString());
            course.setCourseName(courseData.get("courseName").toString());
            course.setDescription(courseData.get("description").toString());
            course.setCredits(Integer.parseInt(courseData.get("credits").toString()));
            course.setDepartment(courseData.get("department").toString());
            course.setSemester(courseData.get("semester").toString());
            course.setSchedule(courseData.get("schedule").toString());
            course.setRoom(courseData.get("room").toString());
            course.setMaxStudents(Integer.parseInt(courseData.get("maxStudents").toString()));
            course.setActive(true);
            
            if (courseData.containsKey("facultyId")) {
                Long facultyId = Long.parseLong(courseData.get("facultyId").toString());
                Faculty faculty = facultyRepository.findById(facultyId)
                        .orElseThrow(() -> new RuntimeException("Faculty not found"));
                course.setFaculty(faculty);
            }
            
            Course savedCourse = courseRepository.save(course);
            
            return ResponseEntity.ok(new ApiResponse(true, "Course created successfully", savedCourse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @GetMapping("/students")
    public ResponseEntity<?> getAllStudents() {
        try {
            List<Student> students = studentRepository.findAll();
            return ResponseEntity.ok(new ApiResponse(true, "Students retrieved", students));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @GetMapping("/faculty")
    public ResponseEntity<?> getAllFaculty() {
        try {
            List<Faculty> faculty = facultyRepository.findAll();
            return ResponseEntity.ok(new ApiResponse(true, "Faculty retrieved", faculty));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @GetMapping("/courses")
    public ResponseEntity<?> getAllCourses() {
        try {
            List<Course> courses = courseRepository.findAll();
            return ResponseEntity.ok(new ApiResponse(true, "Courses retrieved", courses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @PutMapping("/student/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable Long id, 
                                          @RequestBody Map<String, Object> studentData) {
        try {
            Student student = studentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Student not found"));
            
            if (studentData.containsKey("department")) {
                student.setDepartment(studentData.get("department").toString());
            }
            if (studentData.containsKey("semester")) {
                student.setSemester(studentData.get("semester").toString());
            }
            if (studentData.containsKey("address")) {
                student.setAddress(studentData.get("address").toString());
            }
            
            Student updatedStudent = studentRepository.save(student);
            
            return ResponseEntity.ok(new ApiResponse(true, "Student updated successfully", updatedStudent));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @DeleteMapping("/student/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id) {
        try {
            Student student = studentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Student not found"));
            
            User user = student.getUser();
            user.setActive(false);
            userRepository.save(user);
            
            return ResponseEntity.ok(new ApiResponse(true, "Student deactivated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
}