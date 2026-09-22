package course_delivery;

public final class IncidentCoordinator {
    private final InfraiClient infrai;

    public IncidentCoordinator(InfraiClient infrai) { this.infrai = infrai; }

    public String prepareReport(String leakedKeyId, String courseId) throws Exception {
        infrai.reportCompromise(leakedKeyId, true);
        return infrai.searchLogs("key_id=" + leakedKeyId + " course_id=" + courseId);
    }
}
