# Notification Service

Leaf consumer in the banking microservices system. It listens to
`transaction.events`, `account.events`, `user.events`, turns the ones that
matter into a notification, and never publishes anything except
`notification.dlt` when a delivery fails.

## Run locally

```bash
# 1. Postgres + Kafka need to be reachable at the hosts in application.yml
createdb notification_db

# 2. Build & run
mvn spring-boot:run
```

`spring.sql.init.mode=always` runs `schema.sql` on startup, so the two
tables (`notification_log`, `user_contact`) exist before Hibernate
validates against them.

## Package map

```
model/         Notification (data carrier), NotificationLog + UserContact (JPA entities), enums
event/         POJOs mirroring the Kafka payloads (TransactionEvent, AccountEvent, UserEvent)
channel/       Strategy: NotificationChannel interface + SmsChannel / EmailChannel
dispatcher/    NotificationDispatcher — the Strategy "context"
template/      Template Method: NotificationTemplate + 8 concrete templates + resolver
consumer/      @KafkaListener classes, one per topic
service/       NotificationService — the orchestration glue
dlt/           DeadLetterProducer — publishes to notification.dlt
config/        Kafka consumer factory wiring
```

## How one event becomes one notification

```
Kafka topic --> XxxEventConsumer --> NotificationService.handleXxxEvent()
                                          |
                                          v
                          NotificationTemplateResolver.resolve(eventType)
                                          |  (Template Method: build subject/body/channel)
                                          v
                                     Notification
                                          |
                                          v
                          NotificationDispatcher.dispatch()
                                          |  (Strategy: pick Sms/EmailChannel by type)
                                          v
                              channel.send() -> NotificationLog(SENT|FAILED)
                                          |
                                (on failure) -> notification.dlt
```

## The two patterns from the design doc

**Strategy — `NotificationChannel` / `NotificationDispatcher`**
`NotificationDispatcher` holds a `Map<ChannelType, NotificationChannel>` built
from every `NotificationChannel` bean Spring finds. `dispatch()` never has an
`if (type == SMS) ... else if (type == EMAIL) ...` — it's one map lookup.
Adding WhatsApp later is "write `WhatsAppChannel`, annotate `@Component`",
full stop; `NotificationDispatcher` and everything above it are untouched.
That's Open/Closed in practice, not just as a slogan.

**Template Method — `NotificationTemplate`**
`build()` is `final`: subject → body → channel → assemble, in that fixed
order, for every event type. Each concrete template
(`TransactionFailedTemplate`, `AccountFrozenTemplate`, ...) only overrides
`buildSubject()` / `buildBody()`, and optionally the `preferredChannel()`
hook when a notification is urgent enough to warrant SMS over the EMAIL
default. `NotificationTemplateResolver` picks the right one the same way
the dispatcher picks a channel — both are "inject a List, index/filter by a
key" — which is worth pointing out if you get asked to compare the two
patterns in an interview: Strategy swaps an *algorithm*, Template Method
fixes the *skeleton* and swaps *steps inside it*. Here they're solving two
different problems that happen to sit next to each other in the same
request.

## One design decision worth defending: `user_contact`

`account.events` / `transaction.events` only carry a `userId`, not an email
or phone number — and this service doesn't own user data, User Service
does. The tempting shortcut is a synchronous call to User Service per
notification; that quietly re-couples two services Kafka was supposed to
decouple, and now Notification Service is down whenever User Service is.

Instead, `UserEventConsumer` writes every `UserRegistered` / `KycUpdated`
event into a local `user_contact` table — a private read-only cache, not a
shared table. Notification Service is trading strict consistency (contact
info could be briefly stale right after a KYC update) for availability and
decoupling. This is the "event-carried state transfer" pattern; if asked
in an interview why not just call User Service, that trade-off is the
answer.

## Where this is intentionally a stub

`SmsChannel` / `EmailChannel` just `System.out.printf` instead of calling a
real provider — swap the body of `send()` for Twilio/SES/etc. when wiring
this up against something real. Everything around them (Strategy lookup,
logging, DLT-on-failure) is unaffected by that swap, which is itself a
small proof that the interface boundary is in the right place.
