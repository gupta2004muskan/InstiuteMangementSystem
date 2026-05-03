package com.institute.management.service;

import com.institute.management.entity.Course;
import com.institute.management.entity.Grade;
import com.institute.management.entity.Student;
import com.institute.management.repository.GradeRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PDFService {
    
    @Autowired
    private GradeRepository gradeRepository;
    
    public byte[] generateReportCard(Student student) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // Header
            Paragraph header = new Paragraph("INSTITUTE MANAGEMENT SYSTEM")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(header);
            
            Paragraph reportTitle = new Paragraph("STUDENT REPORT CARD")
                    .setFontSize(16)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(reportTitle);
            
            // Student Information
            document.add(new Paragraph("Student Information").setBold().setFontSize(14));
            
            Table studentTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                    .useAllAvailableWidth();
            
            addTableRow(studentTable, "Roll Number:", student.getRollNumber());
            addTableRow(studentTable, "Name:", student.getUser().getFirstName() + " " + student.getUser().getLastName());
            addTableRow(studentTable, "Email:", student.getUser().getEmail());
            addTableRow(studentTable, "Department:", student.getDepartment());
            addTableRow(studentTable, "Semester:", student.getSemester());
            
            document.add(studentTable);
            document.add(new Paragraph("\n"));
            
            // Grades
            document.add(new Paragraph("Academic Performance").setBold().setFontSize(14));
            
            List<Grade> grades = gradeRepository.findByStudentId(student.getId());
            
            if (!grades.isEmpty()) {
                Table gradeTable = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2, 2, 2}))
                        .useAllAvailableWidth();
                
                // Header row
                gradeTable.addHeaderCell(createHeaderCell("Course"));
                gradeTable.addHeaderCell(createHeaderCell("Exam Type"));
                gradeTable.addHeaderCell(createHeaderCell("Marks"));
                gradeTable.addHeaderCell(createHeaderCell("Grade"));
                gradeTable.addHeaderCell(createHeaderCell("GPA"));
                
                // Data rows
                for (Grade grade : grades) {
                    Course course = grade.getCourse();
                    gradeTable.addCell(new Cell().add(new Paragraph(course.getCourseName())));
                    gradeTable.addCell(new Cell().add(new Paragraph(grade.getExamType().toString())));
                    gradeTable.addCell(new Cell().add(new Paragraph(
                            String.format("%.2f/%.2f", grade.getMarksObtained(), grade.getTotalMarks()))));
                    gradeTable.addCell(new Cell().add(new Paragraph(grade.getLetterGrade())));
                    gradeTable.addCell(new Cell().add(new Paragraph(
                            grade.getGradePoint() != null ? String.format("%.2f", grade.getGradePoint()) : "N/A")));
                }
                
                document.add(gradeTable);
                
                // Overall GPA
                Double overallGPA = gradeRepository.calculateGPA(student.getId());
                if (overallGPA != null) {
                    document.add(new Paragraph("\n"));
                    Paragraph gpaText = new Paragraph(String.format("Overall GPA: %.2f", overallGPA))
                            .setBold()
                            .setFontSize(12);
                    document.add(gpaText);
                }
            } else {
                document.add(new Paragraph("No grades available yet.").setItalic());
            }
            
            // Footer
            document.add(new Paragraph("\n\n"));
            Paragraph footer = new Paragraph(
                    "Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")))
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT);
            document.add(footer);
            
            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF report card", e);
        }
    }
    
    private void addTableRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setBold()));
        table.addCell(new Cell().add(new Paragraph(value)));
    }
    
    private Cell createHeaderCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER);
    }
}