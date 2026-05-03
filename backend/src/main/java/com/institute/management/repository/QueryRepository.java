package com.institute.management.repository;

import com.institute.management.entity.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QueryRepository extends JpaRepository<Query, Long> {
    List<Query> findByStudentId(Long studentId);
    List<Query> findByStatus(Query.QueryStatus status);
    List<Query> findByCategory(Query.QueryCategory category);
    List<Query> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    List<Query> findByStatusOrderByCreatedAtAsc(Query.QueryStatus status);
}