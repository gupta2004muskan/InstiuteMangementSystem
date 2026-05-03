package com.institute.management.repository;

import com.institute.management.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCourseCode(String courseCode);
    List<Course> findByDepartment(String department);
    List<Course> findByDepartmentAndSemester(String department, String semester);
    List<Course> findByFacultyId(Long facultyId);
    List<Course> findByActiveTrue();
    Boolean existsByCourseCode(String courseCode);
}