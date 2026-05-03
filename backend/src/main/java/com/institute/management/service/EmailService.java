package com.institute.management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    public void sendWelcomeEmail(String toEmail, String firstName, String role) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to Institute Management System");
            message.setText(String.format(
                "Dear %s,\n\n" +
                "Welcome to the Institute Management System!\n\n" +
                "Your account has been successfully created with the role: %s\n\n" +
                "You can now log in to access your dashboard and explore the features available to you.\n\n" +
                "If you have any questions, please don't hesitate to contact us.\n\n" +
                "Best regards,\n" +
                "Institute Management Team",
                firstName, role
            ));
            
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }
    }
    
    public void sendGradeNotification(String toEmail, String studentName, String courseName, 
                                     String examType, Double marks, String grade) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Grade Update - " + courseName);
            message.setText(String.format(
                "Dear %s,\n\n" +
                "Your grades have been updated for the following:\n\n" +
                "Course: %s\n" +
                "Exam Type: %s\n" +
                "Marks Obtained: %.2f\n" +
                "Grade: %s\n\n" +
                "You can view your complete grade report by logging into the student portal.\n\n" +
                "Best regards,\n" +
                "Institute Management Team",
                studentName, courseName, examType, marks, grade
            ));
            
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send grade notification: " + e.getMessage());
        }
    }
    
    public void sendQueryAnswerEmail(String toEmail, String studentName, String subject, 
                                    String question, String answer) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Query Answered - " + subject);
            message.setText(String.format(
                "Dear %s,\n\n" +
                "Your query has been answered:\n\n" +
                "Subject: %s\n\n" +
                "Your Question:\n%s\n\n" +
                "Answer:\n%s\n\n" +
                "If you need further clarification, please submit a follow-up query.\n\n" +
                "Best regards,\n" +
                "Institute Management Team",
                studentName, subject, question, answer
            ));
            
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send query answer email: " + e.getMessage());
        }
    }
    
    public void sendAttendanceAlert(String toEmail, String studentName, String courseName, 
                                   Double attendancePercentage) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Attendance Alert - " + courseName);
            message.setText(String.format(
                "Dear %s,\n\n" +
                "This is to inform you that your attendance in %s is %.2f%%.\n\n" +
                "Please ensure you maintain the minimum required attendance of 75%% to be eligible for examinations.\n\n" +
                "If you have any concerns, please contact your course instructor.\n\n" +
                "Best regards,\n" +
                "Institute Management Team",
                studentName, courseName, attendancePercentage
            ));
            
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send attendance alert: " + e.getMessage());
        }
    }
}