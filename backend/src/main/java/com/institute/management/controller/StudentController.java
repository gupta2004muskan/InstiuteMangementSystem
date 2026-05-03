package com.institute.management.controller;

import com.institute.management.dto.ApiResponse;
import com.institute.management.entity.*;
import com.institute.management.repository.*;
import com.institute.management.security.UserPrincipal;
import com.institute.management.service.FileStorageService;
import com.institute.management.service.PDFService;
import com.institute.management.service.QuerySolverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private GradeRepository gradeRepository;
    
    @Autowired
    private QuerySolverService querySolverService;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private PDFService pdfService;
    
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            Student student = studentRepository.findByUserId(userPrincipal.getId())
                    .orElseThrow(() -> new RuntimeException("Student profile not found"));
            return ResponseEntity.ok(new ApiResponse(true, "Profile retrieved", student));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @GetMapping("/courses")
    public ResponseEntity<?> getMyCourses(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            Student student = studentRepository.findByUserId(userPrincipal.getId())
                    .orElseThrow(() -> new RuntimeException("Student not found"));
            
            List<Enrollment> enrollments = enrollmentRepository.findByStudentIdAndStatus(
                    student.getId(), Enrollment.EnrollmentStatus.ACTIVE);
            
            return ResponseEntity.ok(new ApiResponse(true, "Courses retrieved", enrollments));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @GetMapping("/available-courses")
    public ResponseEntity<?> getAvailableCourses(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            Student student = studentRepository.findByUserId(userPrincipal.getId())
                    .orElseThrow(() -> new RuntimeException("Student not found"));
            
            List<Course> courses = courseRepository.findByDepartmentAndSemester(
                    student.getDepartment(), student.getSemester());
            
            return ResponseEntity.ok(new ApiResponse(true, "Available courses retrieved", courses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @PostMapping("/enroll/{courseId}")
    public ResponseEntity<?> enrollInCourse(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                           @PathVariable Long courseId) {
        try {
            Student student = studentRepository.findByUserId(userPrincipal.getId())
                    .orElseThrow(() -> new RuntimeException("Student not found"));
            
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Course not found"));
            
            if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Already enrolled in this course"));
            }
            
            Enrollment enrollment = new Enrollment();
            enrollment.setStudent(student);
            enrollment.setCourse(course);
            enrollment.setStatus(Enrollment.EnrollmentStatus.ACTIVE);
            
            enrollmentRepository.save(enrollment);
            
            return ResponseEntity.ok(new ApiResponse(true, "Successfully enrolled in course"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @GetMapping("/attendance")
    public ResponseEntity<?> getMyAttendance(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            Student student = studentRepository.findByUserId(userPrincipal.getId())
                    .orElseThrow(() -> new RuntimeException("Student not found"));
            
            List<Enrollment> enrollments = enrollmentRepository.findByStudentIdAndStatus(
                    student.getId(), Enrollment.EnrollmentStatus.ACTIVE);
            
            Map<String, Object> attendanceData = new HashMap<>();
            
            for (Enrollment enrollment : enrollments) {
                Long present = attendanceRepository.countPresentByStudentAndCourse(
                        student.getId(), enrollment.getCourse().getId());
                Long total = attendanceRepository.countTotalByStudentAndCourse(
                        student.getId(), enrollment.getCourse().getId());
                
                double percentage = total > 0 ? (present * 100.0 / total) : 0.0;
                
                Map<String, Object> courseAttendance = new HashMap<>();
                courseAttendance.put("courseName", enrollment.getCourse().getCourseName());
                courseAttendance.put("present", present);
                courseAttendance.put("total", total);
                courseAttendance.put("percentage", percentage);
                
                attendanceData.put(enrollment.getCourse().getCourseCode(), courseAttendance);
            }
            
            return ResponseEntity.ok(new ApiResponse(true, "Attendance retrieved", attendanceData));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @GetMapping("/grades")
    public ResponseEntity<?> getMyGrades(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            Student student = studentRepository.findByUserId(userPrincipal.getId())
                    .orElseThrow(() -> new RuntimeException("Student not found"));
            
            List<Grade> grades = gradeRepository.findByStudentId(student.getId());
            Double gpa = gradeRepository.calculateGPA(student.getId());
            
            Map<String, Object> gradeData = new HashMap<>();
            gradeData.put("grades", grades);
            gradeData.put("gpa", gpa);
            
            return ResponseEntity.ok(new ApiResponse(true, "Grades retrieved", gradeData));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @GetMapping("/report-card")
    public ResponseEntity<Resource> downloadReportCard(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            Student student = studentRepository.findByUserId(userPrincipal.getId())
                    .orElseThrow(() -> new RuntimeException("Student not found"));
            
            byte[] pdfBytes = pdfService.generateReportCard(student);
            ByteArrayResource resource = new ByteArrayResource(pdfBytes);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=report_card_" + student.getRollNumber() + ".pdf")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/upload-document")
    public ResponseEntity<?> uploadDocument(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                           @RequestParam("file") MultipartFile file) {
        try {
            Student student = studentRepository.findByUserId(userPrincipal.getId())
                    .orElseThrow(() -> new RuntimeException("Student not found"));
            
            String fileName = fileStorageService.storeFile(file);
            student.setDocumentPath(fileName);
            studentRepository.save(student);
            
            return ResponseEntity.ok(new ApiResponse(true, "Document uploaded successfully", fileName));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @PostMapping("/query")
    public ResponseEntity<?> submitQuery(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                        @RequestBody Map<String, String> queryData) {
        try {
            Student student = studentRepository.findByUserId(userPrincipal.getId())
                    .orElseThrow(() -> new RuntimeException("Student not found"));
            
            Query query = querySolverService.submitQuery(
                    student.getId(),
                    queryData.get("subject"),
                    queryData.get("question"),
                    Query.QueryCategory.valueOf(queryData.get("category"))
            );
            
            return ResponseEntity.ok(new ApiResponse(true, "Query submitted successfully", query));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @GetMapping("/queries")
    public ResponseEntity<?> getMyQueries(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            Student student = studentRepository.findByUserId(userPrincipal.getId())
                    .orElseThrow(() -> new RuntimeException("Student not found"));
            
            List<Query> queries = querySolverService.getStudentQueries(student.getId());
            
            return ResponseEntity.ok(new ApiResponse(true, "Queries retrieved", queries));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
}