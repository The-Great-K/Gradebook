package me.thegreatk.gradebook.profile.grades;

import java.util.*;

public class Grade {
    public static final float NOT_ASSESSED = -0.1f;
    private final float earnedPoints, totalPoints;

    private final Map<Float, String> gradeMarkLegend; // Max grade | Associated letter

    public Grade(float earnedPoints, float totalPoints) {
        this.earnedPoints = earnedPoints;
        this.totalPoints = totalPoints;
        this.gradeMarkLegend = GradeMarkLegend.getDefault();
    }

    public Grade(float earnedPoints, float totalPoints, Map<Float, String> gradeMarkLegend) {
        this.earnedPoints = earnedPoints;
        this.totalPoints = totalPoints;
        this.gradeMarkLegend = gradeMarkLegend;
    }

    public float getScore() {
        return totalPoints >= 0.0f ? Math.round((earnedPoints / totalPoints) * 100f) / 100f : NOT_ASSESSED;
    }

    public float getRawScore() {
        return totalPoints >= 0.0f ? earnedPoints / totalPoints : NOT_ASSESSED;
    }

    public String getLetter() {
        return GradeMarkLegend.getLetterGrade(getGradeMarkLegend(), getScore());
    }

    public Map<Float, String> getGradeMarkLegend() {
        return gradeMarkLegend;
    }

    public static class GradeMarkLegend {
        private static final Map<Float, String> DEFAULT = Map.ofEntries(
                Map.entry(100.00f, "A"),
                Map.entry(92.99f, "A-"),
                Map.entry(89.99f, "B+"),
                Map.entry(86.99f, "B"),
                Map.entry(82.99f, "B-"),
                Map.entry(79.99f, "C+"),
                Map.entry(76.99f, "C"),
                Map.entry(72.99f, "C-"),
                Map.entry(69.99f, "D+"),
                Map.entry(66.99f, "D"),
                Map.entry(62.99f, "D-"),
                Map.entry(59.99f, "F")
        );

        public static Map<Float, String> getDefault() {
            return DEFAULT;
        }

        public static String getLetterGrade(Map<Float, String> gradeMarkLegend, float score) {
            if (score == NOT_ASSESSED) return "N/A";

            if (gradeMarkLegend.keySet().isEmpty()) throw new IllegalArgumentException("Empty grademark legend!");

            List<Float> scoresOrdered = new LinkedList<>(gradeMarkLegend.keySet());
            scoresOrdered.sort(Comparator.naturalOrder()); // ascending order

            for (float comparedScore : scoresOrdered) {
                if (!(score > comparedScore)) {
                    return gradeMarkLegend.get(comparedScore);
                }
            }

            if (score >= 100.0f) return "A+";

            throw new IllegalArgumentException("Unexpected score amount inputted");
        }
    }
}
