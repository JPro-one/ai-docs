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
- Gradle: each project gets a `collectDocsPartial` task (auto-applied to subprojects) writing to `build/ai-docs-partial/`; the `collectDocs` task aggregates all partials. Artifact resolution happens at configuration time (inside the task configuration action, so only when the task is requested) — the tasks themselves are configuration-cache compatible
- Only configurations whose name contains "classpath" are scanned; buildscript configurations only with `aiDocs { includeBuildscript = true }` (default off)
- The generated SKILL.md contains a marker comment; if a user removes it (took ownership), the plugins never overwrite the file
- Generator regressions are caught by `BaselineRegressionTest`, which diffs generated output for pinned release deps against `ai-docs-gradle-plugin/src/functionalTest/resources/baseline/` (regenerate with `-DupdateBaseline=true`)
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
│       ├── sources.jar.link        # Absolute path of the sources jar in the local cache (not copied)
│       └── sources-index.md        # Source files by package with line counts + unzip commands
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
├── SourcesIndexGenerator.java  # Lists source files of the sources jar by package (jar referenced via sources.jar.link)
├── SkillGenerator.java         # Generates SKILL.md from the template (Gradle/Maven variants)
├── PomParser.java              # Extracts POM metadata + parent coordinates
├── PomMetadata.java            # Record: name, description, url, scmUrl, license
├── EntriesFile.java            # Serializes DocEntry lists between partial and aggregate tasks
└── BuildTool.java              # GRADLE/MAVEN, used to tailor SKILL.md

ai-docs-gradle-plugin/src/main/java/one/jpro/platform/aidocs/gradle/
├── AiDocsPlugin.java           # Entry point: applies partial plugin to self + subprojects, registers collectDocs
├── AiDocsPartialPlugin.java    # Registers the per-project collectDocsPartial task
├── DocsResolver.java           # Configuration-time artifact resolution + parent-POM traversal
├── ModuleSpec.java             # Resolved module artifacts, encoded as task input strings
├── CollectDocsPartialTask.java # Writes one project's docs into build/ai-docs-partial/
└── CollectDocsTask.java        # Aggregates all partials into build/ai-docs/, generates index/context/skill

ai-docs-maven-plugin/src/main/java/one/jpro/platform/aidocs/maven/
└── CollectDocsMojo.java        # Mojo: same flow via Aether resolution
```
