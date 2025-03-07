package me.thegreatk.gradebook.profile.grades;

import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class ClassGrade implements GradeHolder {
    private final String className;
    private final Grade grade;
    private final WebElement classLink;
    private final List<? extends AssignmentCategory> categories;

    public ClassGrade(String className, Grade grade, WebElement classLink, boolean weightedCategories) {
        this.className = className;
        this.grade = grade;
        this.classLink = classLink;
        if (weightedCategories) {
            categories = new ArrayList<>();
        } else {
            categories = new ArrayList<WeightedAssignmentCategory>();
        }
    }

    public List<? extends AssignmentCategory> getCategories() {
        // TODO: generate assignment categories
        return categories;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public Grade getGrade() {
        return grade;
    }
}
