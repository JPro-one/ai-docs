# AI Docs

Gradle and Maven plugins that collect `DOCUMENTATION.md` artifacts, changelogs, and sources from your project's dependencies and organize them into an AI-navigable file structure — so AI coding assistants can deeply understand every library you use without blowing up their context window.

## The Problem

When an AI agent works on a Java project with many dependencies, it either loads all documentation (too much context), loads nothing (works blind), or asks you every time. None of these scale.

## The Solution

**AI Docs** introduces a simple convention:

1. **Library authors** publish a `DOCUMENTATION.md` alongside their jar (as a Maven artifact with classifier `DOCUMENTATION`)
2. **Projects** apply this plugin and run `./gradlew collectDocs` (or `mvn ai-docs:collect-docs`)
3. **AI agents** navigate the output efficiently: index → overview → specific lines

Three small reads instead of dumping everything into context.

## Quick Start (Gradle)

### 1. Apply the Plugin

```gradle
// settings.gradle
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}
```

```gradle
// build.gradle
plugins {
    id 'java'
    id 'one.jpro.aidocs' version '0.1.0-SNAPSHOT'
}
```

### 2. Collect Documentation

```bash
./gradlew collectDocs
```

This collects all dependency docs into `build/ai-docs/` and installs a skill file at `.claude/skills/docs/SKILL.md` so AI agents automatically know the documentation is available.

### 3. Inspect the Output

```
build/ai-docs/
├── context.md                            # Combined overview of all libraries
├── index.md                              # Compact ToC with line ranges into context.md
├── one.jpro.platform/
│   └── jpro-routing-core/
│       ├── overview.md                   # Chapter titles + line ranges
│       ├── DOCUMENTATION.md              # Full documentation
│       ├── CHANGELOG.md                  # If published, plus changelog-overview.md
│       ├── sources.jar.link              # Path of the sources jar in the local cache
│       └── sources-index.md              # All source files by package, with unzip commands
```

A `SKILL.md` is also generated at `.claude/skills/docs/` so AI agents discover the documentation automatically.

**index.md** — lightweight entry point with line ranges into `context.md`:
```markdown
# AI Documentation Index

## Libraries
- one.jpro.platform:jpro-routing-core:0.5.8 (JPro Routing Core) (lines 91-106) — A framework for building JPro/JavaFX applications...
```

**overview.md** — chapter structure with line ranges and summaries:
```markdown
# JPro Routing Core (0.5.8)
Full content: DOCUMENTATION.md

## Chapters
- Overview (lines 1-25) — A framework for building JPro/JavaFX applications.
- Getting Started (lines 26-80) — Add the dependency to your build.
- Filters API (lines 81-150) — Filters transform routes.
```

## Quick Start (Maven)

Add the plugin to your `pom.xml`:

```xml
<plugin>
    <groupId>one.jpro.aidocs</groupId>
    <artifactId>ai-docs-maven-plugin</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</plugin>
```

Then run:

```bash
mvn one.jpro.aidocs:ai-docs-maven-plugin:collect-docs
```

The output is collected into `target/ai-docs/` with the same structure.

## Configuration (Gradle)

```gradle
aiDocs {
    includeBuildscript = true   // also document buildscript/plugin-classpath dependencies (default: false)
}
```

The generated `.claude/skills/docs/SKILL.md` contains a marker comment — remove it to take ownership of the file; the plugin will then leave it untouched.

## How AI Agents Navigate This

1. **Read `context.md`** — combined overview of all libraries (best for small-to-medium projects), or `index.md` to find the right library's line range first
2. **Read `overview.md`** for the relevant library — see chapter structure with line ranges
3. **Read specific lines** from `DOCUMENTATION.md` — load only the chapter needed
4. **Dig into sources** when docs aren't enough — `sources-index.md` lists every source file (javadoc included); read one via `unzip -p "$(cat sources.jar.link)" <path>` or extract them all for grepping

## For Library Authors

To make your library's documentation available to AI agents, publish a `DOCUMENTATION.md` as a Maven artifact:

- **Classifier:** `DOCUMENTATION`
- **Extension:** `md`

The artifact coordinate looks like: `com.example:my-lib:1.0.0:DOCUMENTATION@md`

Libraries that don't publish this artifact are silently skipped — no errors, no warnings.

## Try It

The [`example/`](example/) directory is a project with JPro dependencies. Set it up and let an AI agent build a full app:

```bash
./gradlew publishToMavenLocal   # from root
cd example
./gradlew collectDocs           # collect dependency docs
```

Then open the `example/` directory in Claude Code and prompt:

```
Build an Expense Tracker web application using JavaFX and JPro.
Use jpro-routing for multi-page navigation and jpro-auth-routing for Google login.
Read the documentation in build/ai-docs/ to understand the frameworks.
```

The agent discovers the available libraries, learns their APIs from the collected docs, and generates the application — without any prior knowledge of these frameworks.

## Building from Source

**Requirements:** Java 17+, Gradle 9.2+

```bash
# Build and run all tests
./gradlew build

# Publish to local Maven repository
./gradlew publishToMavenLocal

# Try the example project
cd example
./gradlew collectDocs
cat build/ai-docs/index.md
```

## Project Structure

| Module | Description |
|--------|-------------|
| `ai-docs-core` | Reusable library for document collection and index/overview generation |
| `ai-docs-gradle-plugin` | Gradle plugin providing the `collectDocs` task |
| `ai-docs-maven-plugin` | Maven plugin providing the `collect-docs` goal |
| `example/` | Standalone Gradle project demonstrating real-world usage with JPro libraries |
| `example-maven/` | Standalone Maven project demonstrating the Maven plugin |

## License

[Apache License 2.0](LICENSE)
