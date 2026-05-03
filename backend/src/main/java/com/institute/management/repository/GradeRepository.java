package com.institute.management.repository;

import com.institute.management.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findByStudentId(Long studentId);
    List<Grade> findByCourseId(Long courseId);
    List<Grade> findByStudentIdAndCourseId(Long studentId, Long courseId);
    Optional<Grade> findByStudentIdAndCourseIdAndExamType(Long studentId, Long courseId, Grade.ExamType examType);
    
    @Query("SELECT AVG(g.gradePoint) FROM Grade g WHERE g.student.id = ?1")
    Double calculateGPA(Long studentId);
    
    @Query("SELECT AVG(g.gradePoint) FROM Grade g WHERE g.student.id = ?1 AND g.course.id = ?2")
    Double calculateCourseGPA(Long studentId, Long courseId);
}