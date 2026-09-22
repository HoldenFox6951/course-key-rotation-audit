package course_delivery;

public final class CourseIncidentExample {
    public static void main(String[] args) throws Exception {
        String apiKey = System.getenv("INFRAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) throw new IllegalStateException("Set INFRAI_API_KEY first");
        String keyId = System.getenv("INFRAI_TEMP_KEY_ID");
        if (keyId == null || keyId.isBlank()) throw new IllegalStateException("Set INFRAI_TEMP_KEY_ID first");
        InfraiClient infrai = new InfraiClient(apiKey);
        infrai.rotateTemporaryKey(keyId, 2, "rotate-course-incident-1");
        String report = new IncidentCoordinator(infrai).prepareReport(keyId, "course-algebra-101");
        System.out.println("Blast-radius log envelope: " + report);
    }
}
