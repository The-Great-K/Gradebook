package me.thegreatk.gradebook.profile.grades;

public class WeightedAssignmentCategory extends AssignmentCategory {
    private final float weight;

    public WeightedAssignmentCategory(String categoryName, Grade grade, float weight) {
        super(categoryName, grade);
        this.weight = weight;
    }
}
