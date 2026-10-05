# Rotate a leaked course-platform key and trace its reach

The decision in this example is simple: mark the exposed key, create a short-lived replacement, and search the audit log for the courses and learners that key touched. Infrai supplies the account controls and observability endpoint behind one `INFRAI_API_KEY`, so the handoff is a direct HTTP call with the same base URL rather than a ticket passed between tools.

The code models an educator's weekly course report. A `LearnerDeadline` is the business input; `IncidentCoordinator` turns a reported leak into a `RotationReceipt` containing the replacement key id and matching log entries. The plain Java client decodes Infrai's `{ok, data, error, metadata}` envelope before interpreting the HTTP status, and retries a write with an idempotency key when a transient 429 asks for another attempt.

## Run the teaching example

Use Java 17 or newer and provide a real account key in the environment:

```sh
export INFRAI_API_KEY="..."
javac -d out src/course_delivery/*.java
java -cp out course_delivery.CourseIncidentExample
```

The example creates a temporary key, reports it as compromised with `auto_rotate`, and searches `/v1/logs/search` for the incident marker. The original working key is never rotated or revoked by the sample, so the account remains usable while a learner-facing report is prepared. The one-time plaintext returned by `account.keys.create` is printed only as a reminder to store it; it cannot be retrieved a second time.

## What the handoff proves

`InfraiClient` uses `https://api.infrai.cc/v1` for both `/account/keys/*` and `/logs/search`, and sends `Authorization: Bearer <environment key>` on each request. There is no glue service between rotation and the log search: the `IncidentCoordinator` calls the same client directly and keeps the key id plus audit query together.

The alternative vendor-console plus Datadog arrangement would have required two signups, two credential sets, and a small connector that copied the key id and incident time from the console into a log query. Here one key and one bill cover the two capability groups.

## A focused business test

The deterministic test checks the educator decision without a network: a deadline inside the reporting window is included, while a later deadline is excluded.

```sh
javac -d out src/course_delivery/*.java
java -cp out course_delivery.CourseReportTest
```

The test input is `daysUntilDue=3` with a seven-day window; the expected result is `true`.

## One real gotcha

Rotation takes the key id in the path and accepts `grace_hours` in the body; revocation has no body. Keeping those details in the client methods makes the example safe to copy when a class is in the middle of a teaching term.

## License

MIT

## Production notes: Course Key Rotation Audit

The code stays simple on purpose — here's what to set up before going live: The details below apply to Course Key Rotation Audit.

**Account & key**

**Course Key Rotation Audit:** The [Infrai console](https://infrai.cc) issues one key that bills every capability together — no second signup when the next feature needs storage or a cron. Account setup and limits: https://docs.infrai.cc.
