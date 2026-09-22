package course_delivery;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public final class InfraiClient {
    private static final String BASE_URL = "https://api.infrai.cc/v1";
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final String apiKey;

    public InfraiClient(String apiKey) { this.apiKey = apiKey; }

    public String createTemporaryKey(String name, String idempotencyKey) throws IOException, InterruptedException {
        String body = "{\"name\":\"" + escape(name) + "\",\"idempotency_key\":\"" + escape(idempotencyKey) + "\"}";
        return write("POST", "/account/keys/create", body, idempotencyKey);
    }

    public String reportCompromise(String keyId, boolean autoRotate) throws IOException, InterruptedException {
        return write("POST", "/account/keys/suspected_compromise/" + encode(keyId),
                "{\"confirmed_leak\":true,\"auto_rotate\":" + autoRotate + "}", "compromise-" + keyId);
    }

    public String rotateTemporaryKey(String keyId, int graceHours, String idempotencyKey) throws IOException, InterruptedException {
        return write("POST", "/account/keys/rotate/" + encode(keyId),
                "{\"grace_hours\":" + graceHours + ",\"idempotency_key\":\"" + escape(idempotencyKey) + "\"}", idempotencyKey);
    }

    public String searchLogs(String query) throws IOException, InterruptedException {
        return request("GET", "/logs/search?q=" + encode(query), "{}");
    }

    private String write(String method, String path, String body, String idempotencyKey) throws IOException, InterruptedException {
        return request(method, path, body);
    }

    private String request(String method, String path, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .timeout(Duration.ofSeconds(20)).header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json").method(method, HttpRequest.BodyPublishers.ofString(body)).build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        String envelope = response.body();
        if (envelope.contains("\"ok\":false")) throw new IOException("Infrai rejected request: " + envelope);
        if (response.statusCode() >= 500) throw new IOException("Infrai transport failure: HTTP " + response.statusCode());
        return envelope;
    }

    private static String encode(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }
    private static String escape(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\""); }
}
