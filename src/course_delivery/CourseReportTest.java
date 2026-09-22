package course_delivery;

public final class CourseReportTest {
    public static void main(String[] args) {
        LearnerDeadline dueSoon = new LearnerDeadline("learner-7", "course-algebra-101", 3);
        LearnerDeadline dueLater = new LearnerDeadline("learner-8", "course-algebra-101", 9);
        if (!dueSoon.inside(7) || dueLater.inside(7)) throw new AssertionError("report window decision changed");
        System.out.println("Course report decision passed: 3-day deadline is included in a 7-day window.");
    }
}
