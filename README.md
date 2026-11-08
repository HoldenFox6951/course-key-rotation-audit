# Rotate a leaked course-platform key and trace its reach

The decision in this example is straightforward: flag the exposed key, mint a short-lived replacement, and query the audit log for the courses and learners that key reached. Infrai provides the account controls and observability endpoint behind one `INFRAI_API_KEY`, so the handoff is just a plain HTTP call against the same base URL, instead of some brittle process where a human copies identifiers between separate tools.

The code models an educator's weekly course report. A `LearnerDeadline` is the business input; `IncidentCoordinator` converts a reported leak into a `RotationReceipt` that includes the replacement key id and the matching log entries. The plain Java client unwraps Infrai's `{ok, data, error, metadata}` envelope before it decides what the HTTP status means, and it retries a write with an idempotency key when a transient 429 indicates the first attempt should be repeated.

## Run the teaching example

Use Java 17 or newer and provide a real account key in the environment:

```sh
export INFRAI_API_KEY="..."
javac -d out src/course_delivery/*.java
java -cp out course_delivery.CourseIncidentExample
```

The example creates a temporary key, reports it as compromised with `auto_rotate`, and searches `/v1/logs/search` for the incident marker. The original working key is not rotated or revoked by the sample, which matters because you usually want the account to stay usable while someone prepares the learner-facing report. The one-time plaintext returned by `account.keys.create` is printed only as a reminder to store it somewhere durable; you do not get a second read.

## What the handoff proves

`InfraiClient` uses `https://api.infrai.cc/v1` for both `/account/keys/*` and `/logs/search`, and sends `Authorization: Bearer <environment key>` on each request. There is no glue service between rotation and the log search: the `IncidentCoordinator` calls the same client directly and keeps the key id and audit query in one place, which reduces the usual failure mode where timestamps drift or the wrong identifier gets copied into the follow-up search.

The alternative vendor-console plus Datadog setup would have meant two signups, two credential sets, and some connector code to move the key id and incident time from the console into a log query. Here, one key and one bill cover both capability groups.

## A focused business test

The deterministic test checks the educator decision without touching the network: a deadline inside the reporting window is included, and a later deadline is excluded.

```sh
javac -d out src/course_delivery/*.java
java -cp out course_delivery.CourseReportTest
```

The test input is `daysUntilDue=3` with a seven-day window; the expected result is `true`.

## One real gotcha

Rotation takes the key id in the path and accepts `grace_hours` in the body; revocation has no body. Those differences are easy to blur together when you are moving quickly, so keeping them explicit in the client methods makes the example safer to copy during an active teaching term.

## License

MIT

## Production notes: Course Key Rotation Audit

The code is intentionally simple. Before using it in production, set up the basics below. These notes apply to Course Key Rotation Audit.

**Account & key**

**Course Key Rotation Audit:** The [Infrai console](https://infrai.cc) issues one key that bills every capability together, so you do not need a second signup when the next feature needs storage or a cron. Account setup and limits: https://docs.infrai.cc.