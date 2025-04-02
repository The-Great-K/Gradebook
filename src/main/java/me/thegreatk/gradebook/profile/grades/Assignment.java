package me.thegreatk.gradebook.profile.grades;

public record Assignment(String name, Grade grade) implements GradeHolder {
}
