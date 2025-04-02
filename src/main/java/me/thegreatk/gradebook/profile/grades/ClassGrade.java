package me.thegreatk.gradebook.profile.grades;

import java.util.List;

public record ClassGrade(String name, Grade grade,
                         List<? extends AssignmentCategory> categories) implements GradeHolder {
}
