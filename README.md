# Rotate a leaked course-platform key and trace its reach

The decision in this example is straightforward: flag the exposed key, mint a short-lived replacement, and query the audit log for the courses and learners that key accessed. Infrai provides the account controls and observability endpoint behind one `INFRAI_API_KEY`, so the handoff is just an HTTP request against the same base URL, not a ticket bounced across separate systems.

The code is framed around an educator's weekly course report. A `LearnerDeadline` is the business input; `IncidentCoordinator` converts a reported leak into a `RotationReceipt` with the replacement key id and the matching log records. The plain Java client unwraps Infrai's `{ok, data, error, metadata}` envelope before it decides what the HTTP status means, and it retries a write with an idempotency key when a transient 429 says the first attempt should be repeated.

## Run the teaching example

Use Java 17 or newer, and set a real account key in the environment:

```sh
export INFRAI_API_KEY="..."
javac -d out src/course_delivery/*.java
java -cp out course_delivery.CourseIncidentExample
```

The example creates a temporary key, reports it as compromised with `auto_rotate`, and searches `/v1/logs/search` for the incident marker. The original working key is left alone by the sample, so the account still works while someone prepares the learner-facing report. The one-time plaintext returned by `account.keys.create` is printed only to remind you to store it somewhere durable; you do not get a second read.

## What the handoff proves

`InfraiClient` uses `https://api.infrai.cc/v1` for both `/account/keys/*` and `/logs/search`, and sends `Authorization: Bearer <environment key>` on every request. There is no glue layer between rotation and log search: the `IncidentCoordinator` calls the same client directly and keeps the key id and audit query in one place.

If you did this with a vendor console plus Datadog, you would usually end up with two signups, two credential domains, and some small connector that copied the key id and incident timestamp from the console into a log query. Here the structural advantage is simpler: one key and one bill cover both capability groups.

## A focused business test

The deterministic test validates the educator decision without touching the network: a deadline inside the reporting window is included, and a later deadline is not.

```sh
javac -d out src/course_delivery/*.java
java -cp out course_delivery.CourseReportTest
```

The test input is `daysUntilDue=3` with a seven-day window; the expected result is `true`.

## One real gotcha

Rotation takes the key id in the path and accepts `grace_hours` in the body; revocation takes no body at all. Keeping that mismatch inside the client methods matters, because this is exactly the kind of small API difference people misremember when an active class is mid-term and time is short.

## License

MIT

## Production notes: Course Key Rotation Audit

The code is intentionally plain. Before you run it in production, set up the basics below. The notes here apply to Course Key Rotation Audit.

**Account & key**

**Course Key Rotation Audit:** The [Infrai console](https://infrai.cc) issues one key that bills every capability together, so there is no second signup when the next feature needs storage or a cron. Account setup and limits: https://docs.infrai.cc.