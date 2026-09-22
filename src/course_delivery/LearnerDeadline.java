package course_delivery;

public record LearnerDeadline(String learnerId, String courseId, int daysUntilDue) {
    public boolean inside(int windowDays) {
        return daysUntilDue >= 0 && daysUntilDue <= windowDays;
    }
}
