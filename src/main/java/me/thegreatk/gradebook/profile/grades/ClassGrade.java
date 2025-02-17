package me.thegreatk.gradebook.profile.grades;

import java.util.List;

public class ClassGrade implements GradeHolder {
    private final String className;
    private final Grade grade;
    private final List<? extends AssignmentCategory> categories;

    public ClassGrade(String className, Grade grade, List<? extends AssignmentCategory> categories) {
        this.className = className;
        this.grade = grade;
        this.categories = categories;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public Grade getGrade() {
        return grade;
    }
}
