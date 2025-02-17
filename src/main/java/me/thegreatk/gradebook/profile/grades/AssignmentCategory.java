package me.thegreatk.gradebook.profile.grades;

public class AssignmentCategory implements GradeHolder {
    private String categoryName;
    private Grade grade;

    public AssignmentCategory(String categoryName, Grade grade) {
        this.categoryName = categoryName;
        this.grade = grade;
    }

    @Override
    public Grade getGrade() {
        return grade;
    }
}
