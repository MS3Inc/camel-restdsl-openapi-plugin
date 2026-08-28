# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.2.0] - unreleased

Modernization to a Java 21 / Camel 4 baseline. The headline change is that the
**code this plugin generates** now compiles against Camel 4; it did not before.

### Fixed

- **Generated REST routes did not compile against Camel 4.** The generator
  emitted `.to(direct("op").getUri())`, and Camel 4 removed `getUri()` from the
  endpoint builders. Every route of every generated project failed with
  "cannot find symbol: method getUri()". Now emits `.to(direct("op"))`, using
  the `RestDefinition.to(EndpointProducerBuilder)` overload.
- **OpenAPI 3.1 documents could not be read.** swagger-parser 2.0.24 predates
  3.1 support and returned `null`, which surfaced as a `NullPointerException`
  from `generateOperationInfoList`. Fixed by the parser upgrade below.
- **Unparseable specifications produced a bare `NullPointerException`.** The
  parser returns `null` rather than throwing for an unreachable path, malformed
  YAML, or an unsupported spec version. The generator now fails with a message
  naming the offending document.

### Changed

- Java baseline raised to 21 via `maven.compiler.release`. The previous
  `maven.compiler.source/target` of 1.7 was dead configuration: the build section
  overrode it with an explicit `<source>11</source>`, so the effective level was
  11. Both are replaced by a single property.
- `swagger-parser-v3` 2.0.24 -> 2.1.47. Output for OpenAPI 3.0 documents is
  byte-for-byte unchanged; 3.1 documents now parse.
- Maven plugin toolchain brought current for Maven 3.9.x:
  `maven-plugin-api`/`maven-core`/`maven-artifact`/`maven-compat` 3.3.9 -> 3.9.16,
  `maven-plugin-annotations` and `maven-plugin-plugin` 3.6.0 -> 3.15.2,
  `maven-compiler-plugin` 3.8.0 -> 3.15.0,
  `maven-plugin-testing-harness` 3.3.0 -> 3.5.1.
  The 4.0.0-beta/rc line was deliberately not adopted: this plugin has to run
  under the Maven 3.9.x that builds the archetypes.
- `goalPrefix` is now declared explicitly as `camel-restdsl-openapi`.
  `maven-plugin-plugin` 3.7+ requires it because the artifactId does not follow
  the `maven-*-plugin` / `*-maven-plugin` convention. This affects only the short
  invocation form; the archetype invokes the plugin by full coordinates.

### Added

- `GeneratedCodeCompilesTest` — compiles the generator's *output* against Camel
  4.18.4 with the in-process `javax.tools` compiler, for both a 3.0 and a 3.1
  specification. The existing golden-file tests only ever compared characters;
  nothing verified the emitted code was valid Java. This is the test that caught
  the `getUri()` regression.
- An OpenAPI 3.1 fixture (`oas-31-sample.yaml`) exercising 3.1-specific
  constructs (type arrays for nullability, numeric `exclusiveMinimum`), with
  golden files and operation-list coverage.
- A regression test for the unparseable-specification error message.

### Known issues

- Regeneration is effectively single-shot. Both writers substitute a marker
  comment (`// REST DSL routes`, `// Implementation routes`) into an existing
  file. The marker is consumed on first run, so subsequent runs find nothing to
  replace and rewrite the file unchanged — including `RoutesGenerated`, which is
  supposed to track the specification. See the note in the 0.2.0 migration
  discussion; not addressed here because it changes the plugin's contract.
- The emitted request-validation line still references `OpenApi4jValidator` from
  `camel-rest-extensions`. If that validator is retired, this generator must be
  updated in step with it.

## [0.1.7] - 2021-03-29

Last released version prior to modernization.
