# AI Docs Gradle Plugin

A Gradle plugin that collects `DOCUMENTATION.md` artifacts from your project's dependencies and organizes them into an AI-navigable file structure. AI coding assistants (Claude Code, Cursor, Copilot, etc.) can use this structured output to understand the libraries your project depends on.

## Using the Plugin

How to apply the plugin, run it, configure it, and navigate its output.

### Applying the Plugin

Add the plugin to your `build.gradle`:

```groovy
plugins {
    id 'one.jpro.aidocs' version '0.1.0-SNAPSHOT'
}
```

Then run:

```bash
gradle collectDocs
```

Documentation is collected into `build/ai-docs/`.

### What Gets Scanned

The plugin scans two sets of dependencies:

1. **Project dependencies** — libraries declared in `dependencies { }` (compile/runtime classpaths)
2. **Buildscript dependencies** — plugins declared in `buildscript { dependencies { } }` or applied via `plugins { }`

For each dependency, it attempts to resolve a `DOCUMENTATION.md` classifier artifact, a `sources.jar`, and a `CHANGELOG.md` classifier artifact. Dependencies that publish none of these are silently skipped.

### Configuring the `collectDocs` Task

The `collectDocs` task is registered in the `documentation` group with these configurable properties:

| Property | Default | Description |
|---|---|---|
| `outputDirectory` | `build/ai-docs` | Where to write collected documentation |
| `overviewMinLines` | `15` | Minimum lines for a sub-chapter to appear in overview.md |
| `contextMinLines` | `150` | Minimum lines for a sub-chapter to appear in context.md |

Example:

```groovy
tasks.named('collectDocs') {
    outputDirectory = layout.buildDirectory.dir('my-docs')
    overviewMinLines = 10
    contextMinLines = 100
}
```

### Output Structure

```
build/ai-docs/
├── index.md                              # Table of contents with line ranges into context.md
├── context.md                            # Concatenated overviews of all libraries
├── {group}/
│   └── {artifact}/
│       ├── overview.md                   # Chapter index with line ranges into DOCUMENTATION.md
│       ├── DOCUMENTATION.md              # Full documentation from the library
│       ├── sources.jar                   # Source code (if published)
│       ├── sources-index.md              # Index of source files in the jar
│       ├── CHANGELOG.md                  # Changelog from the library (if published)
│       └── changelog-overview.md         # Chapter index with line ranges into CHANGELOG.md
```

### Navigating the Output

1. **Start with `index.md`** — lists every collected library with a one-line description and a line range reference into `context.md`
2. **Read `context.md`** — contains concatenated overviews of all libraries; use the line ranges from the index to jump to a specific library
3. **Drill into `{group}/{artifact}/overview.md`** — shows the chapter structure of a library's documentation with line ranges into `DOCUMENTATION.md`
4. **Read `DOCUMENTATION.md`** — the full documentation for the library
5. **Browse sources** — `sources-index.md` lists all source files in `sources.jar`
6. **Check changelogs** — `changelog-overview.md` shows version history chapters with line ranges into `CHANGELOG.md`

### AI Skill Generation

The plugin generates a Claude Code skill file at `.claude/skills/docs/SKILL.md`. This skill teaches Claude Code how to navigate the collected documentation structure automatically.

## Publishing Documentation for Your Library

How library authors can make their documentation discoverable by the plugin.

### Required and Optional Artifacts

The plugin attempts to resolve three artifacts for each dependency:

| Artifact | Classifier | Extension | Required? | Purpose |
|---|---|---|---|---|
| `DOCUMENTATION.md` | `DOCUMENTATION` | `md` | At least one of DOCUMENTATION.md, sources.jar, or CHANGELOG.md | Full library documentation |
| `sources.jar` | `sources` | `jar` | At least one of DOCUMENTATION.md, sources.jar, or CHANGELOG.md | Source code for AI to reference |
| `CHANGELOG.md` | `CHANGELOG` | `md` | At least one of DOCUMENTATION.md, sources.jar, or CHANGELOG.md | Version history and release notes |
| POM | — | `pom` | No (auto-resolved) | Extracts name, description, homepage, license |

Dependencies that publish **none** of `DOCUMENTATION.md`, `sources.jar`, or `CHANGELOG.md` are silently skipped. If at least one is present, the library appears in the index.

### Publishing with Gradle (`maven-publish`)

```groovy
publishing {
    publications {
        maven(MavenPublication) {
            from components.java

            artifact(file('DOCUMENTATION.md')) {
                classifier = 'DOCUMENTATION'
                extension = 'md'
            }

            // Optional: publish a changelog
            artifact(file('CHANGELOG.md')) {
                classifier = 'CHANGELOG'
                extension = 'md'
            }
        }
    }
}
```

### Publishing with Maven

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>build-helper-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>attach-docs</id>
            <phase>package</phase>
            <goals><goal>attach-artifact</goal></goals>
            <configuration>
                <artifacts>
                    <artifact>
                        <file>${project.basedir}/DOCUMENTATION.md</file>
                        <type>md</type>
                        <classifier>DOCUMENTATION</classifier>
                    </artifact>
                </artifacts>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### POM Metadata

The plugin automatically reads POM metadata (project name, description, homepage URL, SCM URL, license) and includes it in `context.md` and `index.md`. Make sure your POM has a meaningful `<name>` and `<description>` — these show up even if you don't publish a `DOCUMENTATION.md`.

### Writing Good DOCUMENTATION.md

How to structure your documentation so the plugin generates useful navigation.

**The first line matters.** The plugin extracts the first non-empty, non-heading line as the library's description. This description appears in `index.md` and `context.md`, so it should be a clear one-sentence summary of what the library does. It is truncated to 150 characters.

```markdown
# My Library

A reactive HTTP client for JavaFX applications with automatic retry and caching.

## Getting Started
...
```

In this example, "A reactive HTTP client for JavaFX applications with automatic retry and caching." becomes the description.

**Headings define the navigation structure.** The plugin parses `#`, `##`, `###` headings to generate `overview.md` with chapter line ranges. AI agents use these ranges to read specific sections without loading the full document. Use a clear hierarchy:

- `#` — Library title (typically one)
- `##` — Major sections (Getting Started, API Reference, Configuration, etc.)
- `###` — Sub-sections within a major section

**First line after each heading becomes its summary.** The first non-blank line after a heading is shown as a one-line summary (max 120 chars) in `overview.md` and `context.md`, helping AI agents decide which chapter to read.

**Use fenced code blocks.** The heading parser respects ` ``` ` fences — lines inside code blocks are not treated as headings. Always fence your code examples.

**Content guidelines:**

- Include code examples for common use cases — AI assistants learn best from examples
- Document the public API with method signatures, parameters, and return types
- Cover configuration options, defaults, and edge cases
- Keep it focused on what an AI assistant needs to generate correct code — skip marketing content and changelogs
