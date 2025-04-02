package me.thegreatk.gradebook.profile.grades;

import java.util.List;

public class WeightedAssignmentCategory extends AssignmentCategory {
    private final float weight;

    public WeightedAssignmentCategory(String categoryName, Grade grade, float weight) {
        super(categoryName, grade);
        this.weight = weight;
    }

    public WeightedAssignmentCategory(String categoryName, Grade grade, List<Assignment> assignments, float weight) {
        super(categoryName, grade, assignments);
        this.weight = weight;
    }

    public float getWeight() {
        return weight;
    }
}
