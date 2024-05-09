package com.example.examseatingsystem;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;


public class EXAMSEATINGSYSTEM extends Application {

    private Map<Course, List<Student>> seatingArrangement;
    public EXAMSEATINGSYSTEM()
    {}

    public EXAMSEATINGSYSTEM(Map<Course, List<Student>> seatingArrangement) {
        this.seatingArrangement = new HashMap<>();
    }
    @Override
    public void init() throws Exception {
        super.init();
        this.seatingArrangement = new HashMap<>();
        Map<Course, List<Student>> seatingArrangement;
        // Initialize your seatingArrangement here
        // You might want to populate seatingArrangement with data here
    }
    public void launchApp() {
        launch();
    }


    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Seating Plan");

        // Create a GridPane to  lay out the seating plan
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        // Define row counter for adding elements to the GridPane
        int row = 0;

        // Iterate over the seatingArrangement map
        for (Map.Entry<Course, List<Student>> entry : seatingArrangement.entrySet()) {
            Course course = entry.getKey();
            List<Student> studentsInCourse = entry.getValue();

            // Add course label to the GridPane
            Label courseLabel = new Label("Course: " + course.getName());
            GridPane.setColumnSpan(courseLabel, 2);
            gridPane.add(courseLabel, 0, row);
            row++;

            // Iterate over students in the course and add their information to the GridPane
            for (Student student : studentsInCourse) {
                Label nameLabel = new Label("Name: " + student.getName());
                gridPane.add(nameLabel, 0, row);

                Label eligibleLabel = new Label("Eligible: " + (student.isEligible() ? "Yes" : "No"));
                gridPane.add(eligibleLabel, 1, row);

                row++;
            }

            // Add an empty row between courses
            row++;
        }

        // Create a Scene with the GridPane
        Scene scene = new Scene(gridPane, 400, 500);

        // Set the Scene to the Stage and show the Stage
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void generateSeatingPlan(Map<Course, List<Student>> seatingArrangement) {
        Application.launch(EXAMSEATINGSYSTEM.class);
    }
    public static void main(String[] args) {

        String excelFilePath = "C:\\Users\\kushsinha\\Downloads\\att.xlsx"; // Replace with the path to your input Excel file
        System.out.println("HI INPUT");
        try {
            List<Student> students = readStudentsFromExcel(excelFilePath);
            if (students.isEmpty()) {
                // Display error message or prompt the user to provide valid data
                System.out.println("No data found in the Excel file. Please ensure the file is not empty.");
                return;
            }

            // Determine eligibility for each student
            determineStudentEligibility(students);

            // Sort courses based on the number of students
            List<Course> courses = sortCoursesByStudentCount(students);
            if (courses.isEmpty()) {
                // Display error message or prompt the user to provide valid data
                System.out.println("No courses found. Please ensure the data in the Excel file is valid.");
                return;
            }

            // Allocate seating arrangement
            Map<Course, List<Student>> seatingArrangement = allocateSeatingArrangement(students, courses);
            if (seatingArrangement.isEmpty()) {
                // Display error message or prompt the user to provide valid data
                System.out.println("Unable to allocate seating arrangement. Please check the data format and try again.");
                return;
            }

            // Generate attendance sheet
            generateAttendanceSheet(seatingArrangement);

            // Generate classroom seating plan
            generateSeatingPlan(seatingArrangement);

            seatingArrangement = new HashMap<>();
            for (Student student : students) {
                Course course = new Course(student.getCourse(), 0); // You may need to adjust this if you have course-specific data
                seatingArrangement.computeIfAbsent(course, k -> new ArrayList<>()).add(student);
            }
            EXAMSEATINGSYSTEM examSeatingSystem = new EXAMSEATINGSYSTEM(seatingArrangement);

            // Now seatingArrangement contains the course-wise seating arrangement
            // Pass this seating arrangement to your JavaFX application
            EXAMSEATINGSYSTEM seatingSystem = new EXAMSEATINGSYSTEM(seatingArrangement);
            seatingSystem.launchApp();



        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static List<Student> readStudentsFromExcel(String excelFilePath) throws IOException {
        FileInputStream fis = new FileInputStream(new File(excelFilePath));
        Workbook workbook = WorkbookFactory.create(fis);
        Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet

        List<Student> students = new ArrayList<>();

        for (Row row : sheet) {
            Cell nameCell = row.getCell(0);
            Cell courseCell = row.getCell(1);
            Cell attendanceCell = row.getCell(2);
            Cell gradeCell = row.getCell(3);

            String name = nameCell.getStringCellValue();
            String course = courseCell.getStringCellValue();
            int attendance = (int) attendanceCell.getNumericCellValue();
            int grade = (int) gradeCell.getNumericCellValue();

            Student student = new Student(name, course, attendance, grade);
            students.add(student);
        }

        workbook.close();
        fis.close();
        for(Student student : students) {
            System.out.println("Name: " + student.getName() + ", Course: " + student.getCourse() + ", Attendance: " + student.getAttendance() + ", Grade: " + student.getGrade());
        }

        return students;
    }

    private static void determineStudentEligibility(List<Student> students) {
        for (Student student : students) {
            // Determine eligibility based on your criteria
            boolean isEligible = student.getAttendance() >= 75;
            student.setEligible(isEligible);
        }
        for(Student student:students){
            System.out.println("Name:"+student.getName()+",Eligible:"+student.isEligible());
        }
    }

    private static List<Course> sortCoursesByStudentCount(List<Student> students) {
        Map<String, Integer> courseStudentCount = new HashMap<>();

        for (Student student : students) {
            String course = student.getCourse();
            courseStudentCount.put(course, courseStudentCount.getOrDefault(course, 0) + 1);
        }

        List<Course> courses = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : courseStudentCount.entrySet()) {
            String course = entry.getKey();
            int studentCount = entry.getValue();
            Course c = new Course(course, studentCount);
            courses.add(c);
        }

        Collections.sort(courses);
        for(Course course: courses) {
            System.out.println("Course: " + course.getName() + ", Student Count: " + course.getStudentCount());
        }

        return courses;
    }

    private static Map<Course, List<Student>> allocateSeatingArrangement(List<Student> students, List<Course> courses) {
        Map<Course, List<Student>> seatingArrangement = new HashMap<>();

        for (Course course : courses) {
            List<Student> studentsInCourse = new ArrayList<>();

            for (Student student : students) {
                if (student.getCourse().equals(course.getName()) && student.isEligible()) {
                    studentsInCourse.add(student);
                }
            }

            seatingArrangement.put(course, studentsInCourse);
        }
        for (Map.Entry<Course, List<Student>> entry : seatingArrangement.entrySet()) {
            Course course = entry.getKey();
            List<Student> studentsInCourse = entry.getValue();
            System.out.println("Course: " + course.getName() + ", Students: " + studentsInCourse.size());
        }

        return seatingArrangement;
    }

    private static void generateAttendanceSheet(Map<Course, List<Student>> seatingArrangement) throws IOException {
        Workbook workbook = WorkbookFactory.create(true);
        Sheet sheet = workbook.createSheet("Attendance");

        int rowNum = 0;
        for (Map.Entry<Course, List<Student>> entry : seatingArrangement.entrySet()) {
            Course course = entry.getKey();
            List<Student> studentsInCourse = entry.getValue();

            // Write course name
            Row courseRow = sheet.createRow(rowNum++);
            courseRow.createCell(0).setCellValue("Course: " + course.getName());

            // Write header
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.createCell(0).setCellValue("Name");
            headerRow.createCell(1).setCellValue("Eligible");

            // Write student details
            for (Student student : studentsInCourse) {
                Row studentRow = sheet.createRow(rowNum++);
                studentRow.createCell(0).setCellValue(student.getName());
                studentRow.createCell(1).setCellValue(student.isEligible());
            }

            rowNum++; // Add an empty row between courses
        }
        System.out.println("Hi File");
        FileOutputStream fos = new FileOutputStream("C:\\Users\\kushsinha\\Downloads\\stt1.xlsx"); // Output file path for attendance sheet
        workbook.write(fos);
        workbook.close();
        fos.close();
    }


}

class Student {
    private String name;
    private String course;
    private int attendance;
    private int grade;
    private boolean eligible;

    public Student(String name, String course, int attendance, int grade) {
        this.name = name;
        this.course = course;
        this.attendance =  attendance;
        this.grade = grade;

        this.eligible = true; // Initially set as ineligible
    }

    public String getName() {
        return name;
    }

    public String getCourse() {
        return course;
    }

    public int getAttendance() {
        return attendance;
    }

    public int getGrade() {
        return grade;
    }

    public boolean isEligible() {
        return eligible;
    }

    public void setEligible(boolean eligible) {
        this.eligible = eligible;
    }
}

class Course implements Comparable<Course> {
    private String name;
    private int studentCount;

    public Course(String name, int studentCount) {
        this.name = name;
        this.studentCount = studentCount;
    }

    public String getName() {
        return name;
    }

    public int getStudentCount() {
        return studentCount;
    }

    @Override
    public int compareTo(Course other) {
        // Sort courses based on student count in descending order
        return Integer.compare(other.studentCount, this.studentCount);
    }
}
