# AI Docs

A Gradle plugin that collects `DOCUMENTATION.md` artifacts from your project's dependencies and organizes them into an AI-navigable file structure — so AI coding assistants can deeply understand every library you use without blowing up their context window.

## The Problem

When an AI agent works on a Java project with many dependencies, it either loads all documentation (too much context), loads nothing (works blind), or asks you every time. None of these scale.

## The Solution

**AI Docs** introduces a simple convention:

1. **Library authors** publish a `DOCUMENTATION.md` alongside their jar (as a Maven artifact with classifier `DOCUMENTATION`)
2. **Projects** apply this plugin and run `./gradlew collectDocs`
3. **AI agents** navigate the output efficiently: index → overview → specific lines

Three small reads instead of dumping everything into context.

## Quick Start

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
    id 'one.jpro.platform.ai-docs' version '0.1.0-SNAPSHOT'
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
├── index.md                              # All libraries at a glance
├── one.jpro.platform/
│   └── jpro-routing-core/
│       ├── overview.md                   # Chapter titles + line ranges
│       └── DOCUMENTATION.md              # Full documentation
```

**index.md** — lightweight entry point:
```markdown
# AI Documentation Index

## Available Libraries
- one.jpro.platform:jpro-routing-core:0.5.8 — [overview](one.jpro.platform/jpro-routing-core/overview.md)
```

**overview.md** — chapter structure with line ranges:
```markdown
# jpro-routing-core (0.5.8)
Full documentation: DOCUMENTATION.md

## Chapters
- Overview (lines 1-25)
- Getting Started (lines 26-80)
- Filters API (lines 81-150)
```

An AI agent reads `index.md` to discover what's available, then reads `overview.md` for a specific library to see its structure, then reads only the relevant lines from `DOCUMENTATION.md`.

## How AI Agents Navigate This

1. **Read `index.md`** — see all available libraries (always small and cheap)
2. **Read `overview.md`** for the relevant library — see chapter structure with line ranges
3. **Read specific lines** from `DOCUMENTATION.md` — load only the chapter needed

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
| `ai-docs-maven-plugin` | Maven plugin (planned) |
| `example/` | Standalone project demonstrating real-world usage with JPro libraries |

## License

TBD
