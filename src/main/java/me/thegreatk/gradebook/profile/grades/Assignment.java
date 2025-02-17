package me.thegreatk.gradebook.profile.grades;

public class Assignment implements GradeHolder {
    private final Grade grade;
    private final String name;

    public Assignment(Grade grade, String name) {
        this.grade = grade;
        this.name = name;
    }

    @Override
    public Grade getGrade() {
        return grade;
    }

    public String getName() {
        return name;
    }
}
