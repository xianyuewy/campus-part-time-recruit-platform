package org.example.col_stu_ptj_sys.dto.student;

import lombok.Data;
import java.util.List;

@Data
public class ResumeParseResult {
    private String name;
    private String gender;
    private String phone;
    private String email;
    private String school;
    private String major;
    private String grade;
    private String availableTime;
    private String expectSalary;
    private List<String> skills;
    private String experience;
    private String selfEvaluation;
}