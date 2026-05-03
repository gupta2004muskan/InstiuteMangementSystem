package com.institute.management.controller;

import com.institute.management.dto.ApiResponse;
import com.institute.management.entity.*;
import com.institute.management.repository.*;
import com.institute.management.security.UserPrincipal;
import com.institute.management.service.EmailService;
import com.institute.management.service.QuerySolverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/faculty")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
public class FacultyController {
    
    @Autowired
    private FacultyRepository facultyRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private GradeRepository gradeRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private QuerySolverService querySolverService;
    
    @Autowired
    private EmailService emailService;
    
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            Faculty faculty = facultyRepository.findByUserId(userPrincipal.getId())
                    .orElseThrow(() -> new RuntimeException("Faculty profile not found"));
            return ResponseEntity.ok(new ApiResponse(true, "Profile retrieved", faculty));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @GetMapping("/courses")
    public ResponseEntity<?> getMyCourses(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            Faculty faculty = facultyRepository.findByUserId(userPrincipal.getId())
                    .orElseThrow(() -> new RuntimeException("Faculty not found"));
            
            List<Course> courses = courseRepository.findByFacultyId(faculty.getId());
            return ResponseEntity.ok(new ApiResponse(true, "Courses retrieved", courses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @GetMapping("/course/{courseId}/students")
    public ResponseEntity<?> getCourseStudents(@PathVariable Long courseId) {
        try {
            List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
            return ResponseEntity.ok(new ApiResponse(true, "Students retrieved", enrollments));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @PostMapping("/attendance/mark")
    public ResponseEntity<?> markAttendance(@RequestBody Map<String, Object> attendanceData) {
        try {
            Long courseId = Long.parseLong(attendanceData.get("courseId").toString());
            LocalDate date = LocalDate.parse(attendanceData.get("date").toString());
            List<Map<String, Object>> studentAttendance = 
                    (List<Map<String, Object>>) attendanceData.get("attendance");
            
            for (Map<String, Object> record : studentAttendance) {
                Long studentId = Long.parseLong(record.get("studentId").toString());
                String status = record.get("status").toString();
                
                Student student = studentRepository.findById(studentId)
                        .orElseThrow(() -> new RuntimeException("Student not found"));
                
                Course course = courseRepository.findById(courseId)
                        .orElseThrow(() -> new RuntimeException("Course not found"));
                
                Attendance attendance = attendanceRepository
                        .findByStudentIdAndCourseIdAndDate(studentId, courseId, date)
                        .orElse(new Attendance());
                
                attendance.setStudent(student);
                attendance.setCourse(course);
                attendance.setDate(date);
                attendance.setStatus(Attendance.AttendanceStatus.valueOf(status));
                
                attendanceRepository.save(attendance);
                
                // Check attendance percentage and send alert if low
                Long present = attendanceRepository.countPresentByStudentAndCourse(studentId, courseId);
                Long total = attendanceRepository.countTotalByStudentAndCourse(studentId, courseId);
                double percentage = total > 0 ? (present * 100.0 / total) : 0.0;
                
                if (percentage < 75.0) {
                    emailService.sendAttendanceAlert(
                            student.getUser().getEmail(),
                            student.getUser().getFirstName(),
                            course.getCourseName(),
                            percentage
                    );
                }
            }
            
            return ResponseEntity.ok(new ApiResponse(true, "Attendance marked successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @PostMapping("/grade/add")
    public ResponseEntity<?> addGrade(@RequestBody Map<String, Object> gradeData) {
        try {
            Long studentId = Long.parseLong(gradeData.get("studentId").toString());
            Long courseId = Long.parseLong(gradeData.get("courseId").toString());
            String examType = gradeData.get("examType").toString();
            Double marksObtained = Double.parseDouble(gradeData.get("marksObtained").toString());
            Double totalMarks = Double.parseDouble(gradeData.get("totalMarks").toString());
            
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Student not found"));
            
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Course not found"));
            
            Grade grade = gradeRepository
                    .findByStudentIdAndCourseIdAndExamType(
                            studentId, courseId, Grade.ExamType.valueOf(examType))
                    .orElse(new Grade());
            
            grade.setStudent(student);
            grade.setCourse(course);
            grade.setExamType(Grade.ExamType.valueOf(examType));
            grade.setMarksObtained(marksObtained);
            grade.setTotalMarks(totalMarks);
            
            // Calculate letter grade and grade point
            double percentage = (marksObtained / totalMarks) * 100;
            if (percentage >= 90) {
                grade.setLetterGrade("A+");
                grade.setGradePoint(4.0);
            } else if (percentage >= 85) {
                grade.setLetterGrade("A");
                grade.setGradePoint(3.7);
            } else if (percentage >= 80) {
                grade.setLetterGrade("A-");
                grade.setGradePoint(3.3);
            } else if (percentage >= 75) {
                grade.setLetterGrade("B+");
                grade.setGradePoint(3.0);
            } else if (percentage >= 70) {
                grade.setLetterGrade("B");
                grade.setGradePoint(2.7);
            } else if (percentage >= 65) {
                grade.setLetterGrade("B-");
                grade.setGradePoint(2.3);
            } else if (percentage >= 60) {
                grade.setLetterGrade("C+");
                grade.setGradePoint(2.0);
            } else if (percentage >= 55) {
                grade.setLetterGrade("C");
                grade.setGradePoint(1.7);
            } else if (percentage >= 50) {
                grade.setLetterGrade("C-");
                grade.setGradePoint(1.3);
            } else {
                grade.setLetterGrade("F");
                grade.setGradePoint(0.0);
            }
            
            gradeRepository.save(grade);
            
            // Send email notification
            emailService.sendGradeNotification(
                    student.getUser().getEmail(),
                    student.getUser().getFirstName(),
                    course.getCourseName(),
                    examType,
                    marksObtained,
                    grade.getLetterGrade()
            );
            
            return ResponseEntity.ok(new ApiResponse(true, "Grade added successfully", grade));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @GetMapping("/queries/pending")
    public ResponseEntity<?> getPendingQueries() {
        try {
            List<Query> queries = querySolverService.getPendingQueries();
            return ResponseEntity.ok(new ApiResponse(true, "Pending queries retrieved", queries));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @PostMapping("/query/{queryId}/answer")
    public ResponseEntity<?> answerQuery(@PathVariable Long queryId,
                                        @RequestBody Map<String, String> answerData,
                                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            String answer = answerData.get("answer");
            String answeredBy = userPrincipal.getUsername();
            
            Query query = querySolverService.answerQuery(queryId, answer, answeredBy);
            
            return ResponseEntity.ok(new ApiResponse(true, "Query answered successfully", query));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
}