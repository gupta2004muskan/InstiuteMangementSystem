package com.institute.management.service;

import com.institute.management.entity.Query;
import com.institute.management.entity.Student;
import com.institute.management.repository.QueryRepository;
import com.institute.management.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class QuerySolverService {
    
    @Autowired
    private QueryRepository queryRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Value("${openai.api.key:}")
    private String openaiApiKey;
    
    public Query submitQuery(Long studentId, String subject, String question, Query.QueryCategory category) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        Query query = new Query();
        query.setStudent(student);
        query.setSubject(subject);
        query.setQuestion(question);
        query.setCategory(category);
        query.setStatus(Query.QueryStatus.PENDING);
        
        Query savedQuery = queryRepository.save(query);
        
        // Try to answer with AI if API key is configured
        if (openaiApiKey != null && !openaiApiKey.isEmpty() && !openaiApiKey.equals("your-openai-api-key-here")) {
            try {
                String aiAnswer = generateAIAnswer(question, category);
                savedQuery.setAnswer(aiAnswer);
                savedQuery.setStatus(Query.QueryStatus.ANSWERED);
                savedQuery.setAnsweredBy("AI Assistant");
                savedQuery.setAnsweredAt(LocalDateTime.now());
                savedQuery = queryRepository.save(savedQuery);
                
                // Send email notification
                emailService.sendQueryAnswerEmail(
                    student.getUser().getEmail(),
                    student.getUser().getFirstName(),
                    subject,
                    question,
                    aiAnswer
                );
            } catch (Exception e) {
                // If AI fails, query remains pending for manual answer
                System.err.println("AI answer generation failed: " + e.getMessage());
            }
        }
        
        return savedQuery;
    }
    
    private String generateAIAnswer(String question, Query.QueryCategory category) {
        // Simplified AI answer generation
        // In production, integrate with OpenAI API or similar service
        
        String contextPrompt = buildContextPrompt(category);
        
        // For now, return a template response
        // In production, call OpenAI API here
        return "Thank you for your question. " + contextPrompt + 
               "\n\nRegarding your query: &quot;" + question + "&quot;\n\n" +
               "This is an automated response. For more detailed information, please contact your faculty advisor or visit the administrative office.\n\n" +
               "If you need further assistance, please submit a follow-up query or contact us directly.";
    }
    
    private String buildContextPrompt(Query.QueryCategory category) {
        return switch (category) {
            case ACADEMIC -> "For academic-related queries, please refer to your course syllabus and consult with your course instructor.";
            case ADMINISTRATIVE -> "For administrative matters, please contact the administrative office during working hours (9 AM - 5 PM).";
            case TECHNICAL -> "For technical issues, please contact the IT support team at support@institute.edu.";
            case COURSE_RELATED -> "For course-specific questions, please check your course materials or contact your course instructor.";
            case EXAM_RELATED -> "For exam-related queries, please refer to the examination schedule and guidelines on the student portal.";
            default -> "For general queries, please contact the help desk or visit the student services office.";
        };
    }
    
    public Query answerQuery(Long queryId, String answer, String answeredBy) {
        Query query = queryRepository.findById(queryId)
                .orElseThrow(() -> new RuntimeException("Query not found"));
        
        query.setAnswer(answer);
        query.setStatus(Query.QueryStatus.ANSWERED);
        query.setAnsweredBy(answeredBy);
        query.setAnsweredAt(LocalDateTime.now());
        
        Query savedQuery = queryRepository.save(query);
        
        // Send email notification
        emailService.sendQueryAnswerEmail(
            query.getStudent().getUser().getEmail(),
            query.getStudent().getUser().getFirstName(),
            query.getSubject(),
            query.getQuestion(),
            answer
        );
        
        return savedQuery;
    }
    
    public List<Query> getStudentQueries(Long studentId) {
        return queryRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
    }
    
    public List<Query> getPendingQueries() {
        return queryRepository.findByStatusOrderByCreatedAtAsc(Query.QueryStatus.PENDING);
    }
    
    public Query getQueryById(Long queryId) {
        return queryRepository.findById(queryId)
                .orElseThrow(() -> new RuntimeException("Query not found"));
    }
}