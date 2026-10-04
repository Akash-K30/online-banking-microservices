# Fraud Detection Service

Consumes `transaction.events` (only `TransactionInitiated`), runs a rule engine
over it, and produces a verdict on `fraud.verdicts` — implementing section 2.4
of the design doc.

## How a message flows through this service

```
transaction.events (TransactionInitiated)
        │
        ▼
TransactionEventConsumer  ──filters non-Initiated events out
        │
        ▼
FraudCheckService.process()
        │  1. skip if transactionId already checked (idempotency)
        │  2. FraudRuleEngine.evaluate(tx)
        ▼
FraudRuleEngine  ──runs every FraudRule bean, sums weighted scores──▶ FraudVerdict
        │
        ├─▶ persist `fraud_checks` row
        ├─▶ upsert `known_devices` (so next time this device/location isn't "new")
        └─▶ FraudVerdictProducer.publish() ──▶ fraud.verdicts (keyed by transactionId)
```

## Package layout

| Package        | Contents |
|----------------|----------|
| `dto`          | `TransactionEvent` (consumed), `FraudVerdict` (produced) |
| `entity`       | `FraudCheck` (audit row), `BlacklistEntry`, `KnownDevice` — this service's own "Fraud Rules/Model Store" |
| `repository`   | Spring Data JPA repositories for the three entities |
| `rules`        | `FraudRule` interface + `VelocityRule`, `AmountThresholdRule`, `BlacklistRule`, `NewDeviceOrLocationRule` |
| `service`      | `FraudRuleEngine` (aggregates rules → verdict), `FraudCheckService` (orchestration) |
| `kafka`        | `TransactionEventConsumer`, `FraudVerdictProducer` |
| `config`       | `KafkaTopicConfig` (declares `fraud.verdicts`) |

## Design patterns

- **Strategy** — `FraudRule` is the strategy interface; `VelocityRule`,
  `AmountThresholdRule`, `BlacklistRule`, and `NewDeviceOrLocationRule` are
  interchangeable implementations. `FraudRuleEngine` doesn't know or care how
  many rules exist or what they check — Spring hands it a `List<FraudRule>` of
  every `@Component` implementing the interface, and it just sums whatever
  triggers. Adding rule #5 means writing a new class, not touching the engine.
- **Chain of Responsibility (the alternative, not implemented here)** — if you
  wanted a blacklist hit to short-circuit immediately instead of still running
  (and paying for) every other rule, you'd instead give each rule a
  `next` pointer and a `handle(tx)` method that either returns a verdict itself
  or delegates to `next.handle(tx)`. Worth doing as a follow-up exercise: it's
  a good example of Strategy vs. Chain of Responsibility being about *whether
  order and short-circuiting matter*, not just "which GoF pattern sounds
  fancier."

## Rules implemented (beyond the two named in the design doc)

The doc's skeleton mentions velocity, amount threshold, and blacklist; the new
prose also called out a "new-device/location flag," so a fourth rule
(`NewDeviceOrLocationRule`) is included, backed by a `known_devices` table
alongside `fraud_checks` and `blacklist_entries`.

| Rule | Signal | Config |
|------|--------|--------|
| `VelocityRule` | >N transactions from one account within a rolling window | `app.fraud.rules.velocity.*` |
| `AmountThresholdRule` | single transaction ≥ a fixed amount | `app.fraud.rules.amount-threshold.*` |
| `BlacklistRule` | account/device/location on a blacklist | `app.fraud.rules.blacklist.*` |
| `NewDeviceOrLocationRule` | first time we've seen this device+location for this account | `app.fraud.rules.new-device-or-location.*` |

Each rule returns a `RuleResult(triggered, score, reason)`; the engine sums
the scores of every triggered rule and compares the total against
`app.fraud.thresholds.block` / `.review` to decide `ALLOW` / `REVIEW` / `BLOCK`.
All weights and thresholds live in `application.yml` — tune them without
touching code.

## Notable trade-offs (worth mentioning in an interview)

- **Velocity is in-memory**, not backed by a query against the Transaction
  Service's own DB (this service doesn't own that data). That's fast and
  simple for a single instance, but the sliding window resets on restart and
  isn't shared if you scale this service horizontally — a real system would
  move it to Redis.
- **Idempotency** is handled with a simple `existsByTransactionId` check before
  processing, so Kafka redelivery after a consumer restart doesn't double-count
  a transaction toward velocity or produce two verdicts.
- **Failure handling** here is "log and move on" — a production system would
  route failures to a retry topic or DLT (the design doc already has
  `notification.dlt` as a precedent) instead of swallowing the exception.

## Running it

This service expects the same Kafka broker and a dedicated Postgres database
(`fraud_db`) as the rest of the banking app — see `application.yml` for the
connection details to override. Seed a row in `blacklist_entries` manually (or
via a small `CommandLineRunner`) to see `BlacklistRule` fire; the other three
rules trigger naturally once transactions start flowing from the Transaction
Service.

```bash
mvn spring-boot:run
```

Unit tests (`FraudRuleEngineTest`) use hand-written fake `FraudRule`s to check
the ALLOW/REVIEW/BLOCK threshold math without needing Kafka or Postgres —
that's the payoff of keeping rules as plain, dependency-light Strategy objects.
