# AGENTS.md

## Purpose and scope

These instructions apply to the entire repository. They are the default rules for every human or AI contributor unless a more specific `AGENTS.md` exists deeper in the directory tree.

Car Rental is a Java 26, Spring Boot 4 application built with Gradle. Keep changes small, production-ready, and consistent with the rules below.

## Working agreement

- Read the relevant production code, tests, configuration, and migrations before editing.
- Preserve unrelated work. Do not reformat, rename, or reorganize code outside the requested scope.
- Prefer the smallest coherent change that fully solves the problem.
- Do not add a dependency when the JDK, Spring, or an existing dependency already provides a clear solution.
- Do not introduce abstractions for hypothetical future use. Extract an abstraction when it represents a real boundary, variation, or repeated concept.
- Keep generated sources out of manual edits. GraphQL-generated code belongs to the configured code-generation task.
- Never commit secrets, tokens, credentials, private customer data, or machine-specific configuration.
- Before handing off a change, run the narrowest relevant tests and then the full test suite when practical.

## Commands

Use the Gradle wrapper so local and CI behavior stay aligned.

```powershell
.\gradlew.bat test
.\gradlew.bat build
.\gradlew.bat test --tests "fully.qualified.TestClassName"
```

On Unix-like systems, use the equivalent `./gradlew` commands.

## Clean Architecture

### Organize by business capability

Prefer feature-first packages below `pro.inperfect.car_rental`, for example:

```text
<feature>/
  domain/
  application/
  adapter/in/web/
  adapter/out/persistence/
```

Create only the packages a feature actually needs. Shared code must represent a genuinely shared concept, not a convenient dumping ground. Avoid generic packages such as `common`, `util`, `helper`, `service`, or `model` unless their scope and ownership are unmistakable.

### Dependency direction

Dependencies point inward:

```text
adapters -> application -> domain
```

- The domain contains business rules, entities, value objects, domain services, and repository interfaces expressed in domain language. It must not depend on Spring, JPA, HTTP, GraphQL, databases, or other infrastructure.
- The application layer coordinates use cases and transaction boundaries. It depends on domain types and may declare ports for non-repository capabilities required from the outside world.
- Input adapters translate HTTP, GraphQL, messaging, or scheduled work into application commands and queries.
- Output adapters implement domain repository interfaces and application ports for remote services, clocks, identifiers, and other infrastructure.
- Configuration wires implementations to ports. Business code must not locate dependencies through the application context.
- Never make an inward layer depend on an adapter DTO, JPA entity, Spring annotation, or transport exception.

### Domain design

- Model behavior with domain language. Prefer meaningful value objects over primitive strings and numbers when validation or behavior belongs to the concept.
- Every identifier value object uses `String value`. Never use `UUID`, numeric types, or framework-specific identifier types as the value of an identifier value object. Normalize and validate the string in the value object; UUID generation or legacy UUID persistence conversion, when unavoidable, stays inside an outer adapter and never leaks into domain, application, or transport contracts.
- Protect invariants at construction and mutation boundaries. An invalid domain object must be difficult or impossible to create.
- Keep entities focused on business behavior; do not turn them into transport or persistence bags.
- Use immutable types by default. Make mutation explicit and controlled.
- Represent expected business outcomes explicitly, using domain-specific results or exceptions. Do not leak SQL, HTTP, or vendor exceptions inward.
- Inject nondeterministic concerns such as time, randomness, and identifier generation through ports when they affect business behavior.

### Use cases and ports

- Give each use case one clear business purpose and an intention-revealing name.
- Keep controllers and GraphQL handlers thin: validate transport shape, map input, invoke one use case, and map the result.
- Put repository interfaces next to the domain model they persist and name their operations in domain language. Define other ports from the application's perspective and keep them technology-neutral.
- Do not expose `JpaRepository`, persistence entities, HTTP response types, or framework contexts through an application port.
- Put transaction boundaries around application use cases that require atomicity, not around controllers or domain objects.

### Persistence and external systems

- Keep JPA entities and domain-repository implementations in output adapters. Map explicitly between persistence and domain models.
- Evolve the database through versioned Flyway migrations. Never edit an already-applied migration; add a new one.
- Pin production-relevant container and service versions. Do not rely on `latest` outside disposable local experiments.
- Treat network calls as unreliable: set timeouts, map errors, and apply retries or circuit breakers only when the operation is safe and the behavior is tested.
- Keep observability at boundaries. Logs, metrics, and traces must not contain secrets or sensitive personal data.

## Clean Code

### Names and structure

- Use names from the business domain. A good name explains intent without requiring a comment.
- Classes and interfaces are nouns or roles; methods are verbs or questions. Boolean names read naturally as predicates.
- Keep one primary responsibility per class and one level of abstraction per method.
- Prefer short methods with early returns over deep nesting. Extract logic when the extracted name adds meaning.
- Avoid boolean flag arguments. Use separate methods, commands, or strategy types when behavior differs.
- Avoid magic values. Give business constants names and units.
- Use records for immutable data carriers when they fit; use classes when identity, lifecycle, or encapsulated behavior matters.
- Keep code formatting and conventions consistent within a project.
- Do not use "magic" strings, use contsnts instead, you can group them in classes.
- Prefer objects instead of generic Map<> structures, only use Map<> where appropriate like dynamic contract etc. 

### APIs and behavior

- Make public APIs minimal and explicit. Default to the narrowest useful visibility.
- Validate untrusted input at system boundaries and enforce business invariants in the domain.
- Do not use `null` to encode multiple meanings. Prefer empty collections, explicit optional results, or domain-specific outcomes as appropriate.
- Do not use `Optional` for fields or parameters. Use it only for a deliberately optional return value.
- Catch exceptions only when adding context, translating across a boundary, compensating, or recovering. Never swallow failures.
- Preserve interrupt status and causal chains when handling concurrency or wrapped exceptions.
- Keep side effects visible. A query must not silently mutate state.
- Make idempotency explicit for operations that may be retried.

### Spring and Java

- Do NOT use `var` keyword in java always specify type explicitly.
- Prefer constructor injection. Do not use field injection.
- Use configuration properties for grouped application settings; validate required settings at startup.
- Keep framework annotations at the outer layers. Domain code stays plain Java.
- Use Lombok sparingly. Do not use `@Data` on domain or persistence entities; generated equality, mutation, and `toString` behavior can violate invariants or expose data.
- Follow the repository's formatter and existing style. Do not mix tabs and spaces within a file.
- Import referenced classes and use their simple names. Use fully qualified class names only when simple names clash in the same file. For static helpers that may be ambiguous, import the owning class and qualify through it, for example `Comparator.comparing(...)` rather than a static `comparing(...)` import.
- Comments explain why, constraints, or non-obvious trade-offs. Remove comments that merely narrate the code.
- Delete dead code instead of commenting it out. Track deferred work in an issue, not a vague `TODO`; a necessary TODO must include context and a tracking reference.
- do not reassign fields in record constructors
example how not to do:
```java
record InChannel(ChannelId channelId) implements ChannelScope {
        public InChannel {
            channelId = Objects.requireNonNull(channelId, "channel id must not be null");
        }
    }
```
do this instead:
```java
record InChannel(ChannelId channelId) implements ChannelScope {
        public InChannel {
            Objects.requireNonNull(channelId, "channel id must not be null");
        }
    }
```

## SMART test rules

Every test must be **Specific, Meaningful, Autonomous, Repeatable, and Timely**.

### Specific

- Verify one behavior or one cohesive scenario.
- Name every test using `given_<condition_or_state>_when_<action>_should_<effect>`, for example `given_empty_cart_when_checkout_is_invoked_should_reject_checkout`.
- Structure every executable test body with explicit `// given`, `// when`, and `// then` comments immediately before its preparation, action, and assertion stages. Keep this format for all future tests.
- Assert outcomes, state changes, and boundary interactions that form the contract. Avoid asserting private implementation details.
- A failing assertion should clearly identify the broken behavior.

### Meaningful

- Test business rules, public contracts, mappings, persistence behavior, security boundaries, and important failure paths.
- Every production bug fix includes a regression test that fails before the fix and passes after it.
- Prefer assertions with precise expected values. Avoid assertion-free tests and weak checks such as "not null" when exact behavior is known.
- Do not chase coverage numbers with tests that cannot catch a realistic regression.
- The application-context smoke test in `CarRentalApplicationTests` verifies startup wiring only; do not use it as a substitute for behavior-focused tests.

### Autonomous

- A test can run alone, in any order, and in parallel unless the tested resource makes parallelism impossible.
- Tests must not depend on another test, shared mutable state, pre-existing local data, the developer's machine, or a manually started service.
- Create required data in the test and clean it through lifecycle-managed fixtures.
- Replace external services at the boundary with fakes or stubs for unit and slice tests. Use Testcontainers for infrastructure behavior that must be real.
- Never call uncontrolled production or public network services from the test suite.

### Repeatable

- The same code and inputs produce the same result locally and in CI.
- Control time, randomness, generated identifiers, locale, time zone, and ordering whenever they affect assertions.
- Do not use sleeps to coordinate asynchronous behavior. Wait for a condition with an explicit timeout.
- Pin container images and isolate database state. Do not rely on Testcontainers' `latest` image tag.
- A flaky test is a defect: fix its cause promptly; do not hide it with retries or permanently disable it.

### Timely

- Add or update tests in the same change as the production behavior.
- Keep the feedback loop fast: use plain unit tests for domain rules, focused Spring slices for one boundary, and full-context integration tests only for cross-layer wiring or workflows.
- Mock only architectural boundaries. Prefer real domain collaborators so refactoring does not cause irrelevant test failures.
- Keep fixtures small and readable. Use builders or object mothers only when they improve intent and keep valid defaults visible.
- Keep all Java tests in the default Gradle `test` suite under `src/test`; do not create separate component, integration, or test-fixtures source sets.
- Put reusable fixtures under `src/test/java/pro/inperfect/car_rental/fixtures`.
- Tests that require the complete Spring context extend `base.AbstractWholeContextTest`. It is the only class annotated with `@SpringBootTest`, allowing every whole-context test to reuse the same cached context. Plain unit tests do not extend it.

## Test selection guide

- **Domain unit test:** pure business rule; no Spring context, database, filesystem, or network.
- **Application unit test:** use-case orchestration with port fakes or mocks; no Spring context.
- **Web/GraphQL slice test:** transport validation, serialization, authorization, and error mapping.
- **Persistence integration test:** mappings, constraints, queries, and migrations against PostgreSQL via Testcontainers.
- **Application integration test:** a small number of critical workflows and application wiring by extending `base.AbstractWholeContextTest`.

Use the lowest test level that proves the behavior. Do not duplicate the same assertions at every layer.

## Definition of done

A change is complete only when:

- architecture boundaries and dependency direction remain intact;
- behavior is covered at the appropriate test level, including relevant failure paths;
- migrations, configuration, API schemas, and operational documentation are updated when affected;
- relevant targeted tests pass;
- `./gradlew test` passes, or any environment limitation is reported clearly;
- no secrets, debug output, disabled tests, flaky waits, or unrelated formatting changes are included.
