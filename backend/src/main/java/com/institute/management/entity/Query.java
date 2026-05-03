package com.institute.management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "queries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Query {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    @Column(nullable = false)
    private String subject;
    
    @Column(length = 2000, nullable = false)
    private String question;
    
    @Column(length = 5000)
    private String answer;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueryStatus status = QueryStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueryCategory category;
    
    private String answeredBy; // "AI" or faculty name
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    private LocalDateTime answeredAt;
    
    public enum QueryStatus {
        PENDING, ANSWERED, CLOSED
    }
    
    public enum QueryCategory {
        ACADEMIC, ADMINISTRATIVE, TECHNICAL, GENERAL, COURSE_RELATED, EXAM_RELATED
    }
}