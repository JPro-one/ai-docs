# AI Docs - Project Guide

## What This Is

Gradle and Maven plugins that collect `DOCUMENTATION.md` artifacts (plus changelogs and sources jars) from Java/JVM project dependencies and organize them into an AI-navigable file structure. Library authors publish a `DOCUMENTATION.md` alongside their jar using the Maven classifier `DOCUMENTATION` with extension `md`. The plugins resolve those artifacts and generate an index, a combined context file, per-library overviews with chapter line ranges, and a Claude Code skill file.

## Module Structure

- **ai-docs-core** — Reusable Java library: `DocEntry`, `DocsCollector`, `IndexGenerator`, `OverviewGenerator`, `ContextGenerator`, `SourcesIndexGenerator`, `SkillGenerator`, `PomParser`/`PomMetadata`, `BuildTool`
- **ai-docs-gradle-plugin** — Gradle plugin (`one.jpro.aidocs`) that registers the `collectDocs` task. Depends on `ai-docs-core`.
- **ai-docs-maven-plugin** — Maven plugin with the `collect-docs` goal, collects into `target/ai-docs/`
- **example/** — Standalone Gradle test project (not included in the main build). Must be published locally first.
- **example-maven/** — Standalone Maven test project using the Maven plugin.

## Build & Test

```bash
./gradlew build                  # Build + run all tests (unit + functional)
./gradlew test                   # Unit tests only
./gradlew functionalTest         # Plugin integration tests (in ai-docs-gradle-plugin)
./gradlew publishToMavenLocal    # Publish to ~/.m2 for local testing
```

To test the example projects:
```bash
./gradlew publishToMavenLocal    # from root
cd example && gradle collectDocs # from example/
cd example-maven && mvn one.jpro.aidocs:ai-docs-maven-plugin:collect-docs
```

## Tech Stack

- Java 17, Gradle 9.2.1
- JUnit 5 + AssertJ for testing
- Gradle TestKit for functional tests
- No runtime dependencies beyond the Java standard library

## Key Conventions

- Plugin ID: `one.jpro.aidocs`
- Group: `one.jpro.aidocs`, Version: `0.1.0-SNAPSHOT`
- Task output goes to `build/ai-docs/` (Maven: `target/ai-docs/`)
- The `collectDocs` task only scans configurations whose name contains "classpath" (including buildscript and subproject configurations)
- Per dependency, the plugins try to resolve: `DOCUMENTATION@md`, `sources@jar`, `CHANGELOG@md`, and the POM (for metadata + parent-POM traversal)
- Dependencies without any of these artifacts are silently skipped (logged at debug level)
- A skill file is generated at `.claude/skills/docs/SKILL.md` from the root `SKILL.md` template (copied into ai-docs-core resources at build time)

## Output Structure

```
build/ai-docs/
├── context.md                  # Combined overview of all libraries (descriptions, chapters, links)
├── index.md                    # Compact ToC with line ranges into context.md
├── {group}/
│   └── {artifact}/
│       ├── overview.md             # Auto-generated chapter index with line ranges
│       ├── DOCUMENTATION.md        # Full documentation copied from the artifact
│       ├── CHANGELOG.md            # If published, plus changelog-overview.md
│       ├── sources.jar             # If published, plus...
│       └── sources-index.md        # ...source files by package with line counts
.claude/skills/docs/SKILL.md    # Generated skill telling agents how to navigate the docs
```

## Code Layout

```
ai-docs-core/src/main/java/one/jpro/platform/aidocs/core/
├── DocEntry.java               # Record: group, name, version + description/sources/changelog/POM flags
├── DocsCollector.java          # Orchestrator: collectDoc/Sources/Changelog, generateContextAndIndex, generateSkill
├── IndexGenerator.java         # Generates index.md (line ranges into context.md)
├── ContextGenerator.java       # Generates combined context.md
├── OverviewGenerator.java      # Parses headings → overview.md with line ranges
├── SourcesIndexGenerator.java  # Lists source files in sources.jar by package
├── SkillGenerator.java         # Generates SKILL.md from the template (Gradle/Maven variants)
├── PomParser.java              # Extracts POM metadata + parent coordinates
├── PomMetadata.java            # Record: name, description, url, scmUrl, license
└── BuildTool.java              # GRADLE/MAVEN, used to tailor SKILL.md

ai-docs-gradle-plugin/src/main/java/one/jpro/platform/aidocs/gradle/
├── AiDocsPlugin.java           # Plugin entry point, registers collectDocs task
└── CollectDocsTask.java        # Task: scans configurations, resolves artifacts, traverses parent POMs

ai-docs-maven-plugin/src/main/java/one/jpro/platform/aidocs/maven/
└── CollectDocsMojo.java        # Mojo: same flow via Aether resolution
```
