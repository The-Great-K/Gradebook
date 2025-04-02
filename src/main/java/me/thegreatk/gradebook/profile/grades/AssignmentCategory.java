package me.thegreatk.gradebook.profile.grades;

import java.util.ArrayList;
import java.util.List;

public class AssignmentCategory implements GradeHolder {
    private final String name;
    private final Grade grade;
    private final List<Assignment> assignments;

    public AssignmentCategory(String name, Grade grade) {
        this.name = name;
        this.grade = grade;
        this.assignments = new ArrayList<>();
    }

    public AssignmentCategory(String name, Grade grade, List<Assignment> assignments) {
        this.name = name;
        this.grade = grade;
        this.assignments = assignments;
    }

    public void addAssignment(Assignment assignment) {
        assignments.add(assignment);
    }

    public String name() {
        return name;
    }

    @Override
    public Grade grade() {
        return grade;
    }

    public List<Assignment> assignments() {
        return assignments;
    }
}
