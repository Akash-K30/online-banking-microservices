# Transaction Service (Saga Orchestrator)

Spring Boot 3 / Java 17 microservice implementing the transfer flow from the bank
application's design docs.

## Patterns used (and one dropped on purpose)
- **State** - `saga/state/*State` implement `TransactionState`, one per
  `TransactionStatus`. Each decides whether a fraud verdict is valid to act on given
  the transaction's current status.
- **Saga (orchestration)** - `TransactionSagaOrchestrator` drives hold -> debit ->
  credit against Account Service once a fraud verdict arrives, with compensating
  actions on partial failure (see below).
- **Template Method - deliberately not used.** The transfer has one shape today
  (single source, single destination). `TransactionSagaOrchestrator.runHoldDebitCredit`
  is a plain sequential method rather than an abstract skeleton with hook methods -
  simpler to read, and there's nothing to vary yet. Revisit only if a second transfer
  *type* is added that genuinely needs to override individual steps.

## What was added beyond the original design doc
The original schema had no state between "hold placed" and "completed", and no way
to answer `GET /transactions?userId=` (the table only stores account ids). Both are
addressed here:
- **`DEBITED` status** - tracks the moment between debit and credit succeeding, so a
  credit failure is recognized as "money already left the source account" and
  triggers a compensating reversal credit (same `refId`/transactionId, so it lands on
  the same ledger trail) rather than just being marked `FAILED`.
- **`account_owner_cache` table**, populated from `account.events` (`AccountOpened`),
  denormalizing `accountId -> userId` so the userId query doesn't need a synchronous
  call to Account Service on every transfer.
- **`StaleTransactionReconciler`** - a scheduled sweep for transactions stuck without
  a fraud verdict (safe to auto-fail, nothing external has happened) vs. stuck with
  funds reserved/moved (logged for manual follow-up rather than guessed at).
- **Idempotency race handling** - a second concurrent request with the same
  `idempotencyKey` returns the first request's result instead of a duplicate-key
  error, and a reused key with *different* transfer details (amount/accounts) is
  rejected as `409 Conflict` rather than silently returning stale data.

## Known limitations / follow-ups
- **No outbox pattern.** `TransferService.initiateTransfer` saves to Postgres and
  publishes to Kafka in the same method; if the service crashes between the publish
  and the transaction commit, a consumer could react to an event for a transaction
  that isn't visible in the DB yet (or, if the commit then rolls back, one that never
  existed). Account Service's design already uses an outbox table + relay for this
  exact reason - Transaction Service should get the same treatment before production
  use.
- **`REVIEW` fraud verdicts are treated as failures.** There's no manual-adjudication
  queue yet; `FraudCheckState` fails the transfer with a distinct reason string so
  it's at least visible in logs/queries, but a human-review workflow isn't built.
- **Cross-service Kafka JSON typing.** `FraudVerdictMessage` and `AccountEventMessage`
  are consumed by logical type name (`fraudVerdict`, `accountEvent` in
  `application.yml`'s `spring.json.type.mapping`), not by class name - Fraud Service
  and Account Service's producers need to publish with matching `__TypeId__` header
  values for this to deserialize correctly, since their producer classes don't exist
  on this service's classpath.
- Not compiled/tested against real Spring Boot/Kafka/Postgres dependencies in this
  environment (no Maven Central access here) - written carefully by hand, but build
  and run it locally before trusting it.

## Running locally
Needs Postgres (`transaction_db`) and Kafka reachable at the URLs in
`application.yml`, plus Account Service at `account-service.base-url`.

```
mvn spring-boot:run
```
