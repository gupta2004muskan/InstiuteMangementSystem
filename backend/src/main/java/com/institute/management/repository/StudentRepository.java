package com.institute.management.repository;

import com.institute.management.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByRollNumber(String rollNumber);
    Optional<Student> findByUserId(Long userId);
    List<Student> findByDepartment(String department);
    List<Student> findByDepartmentAndSemester(String department, String semester);
    Boolean existsByRollNumber(String rollNumber);
}