# AI Docs - Project Guide

## What This Is

A Gradle plugin that collects `DOCUMENTATION.md` artifacts from Java/JVM project dependencies and organizes them into an AI-navigable file structure. Library authors publish a `DOCUMENTATION.md` alongside their jar using the Maven classifier `DOCUMENTATION` with extension `md`. This plugin resolves those artifacts and generates an index + per-library overviews with chapter line ranges.

## Module Structure

- **ai-docs-core** — Reusable Java library: `DocEntry`, `DocsCollector`, `IndexGenerator`, `OverviewGenerator`
- **ai-docs-gradle-plugin** — Gradle plugin (`one.jpro.aidocs`) that registers the `collectDocs` task. Depends on `ai-docs-core`.
- **ai-docs-maven-plugin** — Stub, not yet implemented
- **example/** — Standalone test project (not included in the main build). Must be published locally first.

## Build & Test

```bash
./gradlew build                  # Build + run all tests (unit + functional)
./gradlew test                   # Unit tests only
./gradlew functionalTest         # Plugin integration tests (in ai-docs-gradle-plugin)
./gradlew publishToMavenLocal    # Publish to ~/.m2 for local testing
```

To test the example project:
```bash
./gradlew publishToMavenLocal    # from root
cd example && gradle collectDocs # from example/
```

## Tech Stack

- Java 17, Gradle 9.2.1
- JUnit 5 + AssertJ for testing
- Gradle TestKit for functional tests
- No runtime dependencies beyond the Java standard library

## Key Conventions

- Plugin ID: `one.jpro.aidocs`
- Group: `one.jpro.aidocs`, Version: `0.1.0-SNAPSHOT`
- Task output goes to `build/ai-docs/`
- The `collectDocs` task only scans configurations whose name contains "classpath"
- Dependencies without a `DOCUMENTATION.md` artifact are silently skipped (logged at debug level)

## Output Structure

```
build/ai-docs/
├── index.md                    # Lists all libraries with links to overviews
├── {group}/
│   └── {artifact}/
│       ├── overview.md         # Auto-generated chapter index with line ranges
│       └── DOCUMENTATION.md    # Full documentation copied from the artifact
```

## Code Layout

```
ai-docs-core/src/main/java/one/jpro/platform/aidocs/core/
├── DocEntry.java           # Record: group, name, version
├── DocsCollector.java      # Orchestrator: collectDoc, generateIndex, cleanOutputDir
├── IndexGenerator.java     # Generates index.md
└── OverviewGenerator.java  # Parses headings → overview.md with line ranges

ai-docs-gradle-plugin/src/main/java/one/jpro/platform/aidocs/gradle/
├── AiDocsPlugin.java       # Plugin entry point, registers collectDocs task
└── CollectDocsTask.java    # Task: resolves DOCUMENTATION classifier artifacts
```
