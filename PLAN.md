# AI Docs Plugin — Project Plan

A Gradle and Maven plugin that collects `DOCUMENTATION.md` artifacts from project dependencies and makes them accessible to AI coding assistants.

## Vision

Any Java/JVM library can publish a `DOCUMENTATION.md` alongside its jar. Any project using that library can automatically collect all available documentation and feed it to AI agents — giving them deep understanding of every dependency.

---

## Module Structure

```
ai-docs/
├── ai-docs-gradle-plugin/      # Gradle plugin
├── ai-docs-maven-plugin/       # Maven plugin
```

---

## Phase 1: Core — Collect Documentation ✅ (done)

### Gradle Plugin (`ai-docs-gradle-plugin`)
- Task `collectDocs` — resolves all dependencies, downloads `DOCUMENTATION.md` (classifier) artifacts
- Generates a navigable file structure optimized for AI agents (see below)
- Gracefully skips dependencies that don't publish documentation

### Maven Plugin (`ai-docs-maven-plugin`)
- Goal `collect-docs` — same behavior for Maven projects
- Collects into `target/ai-docs/`

### Output Structure

```
build/ai-docs/
├── index.md                              # list of all libraries with docs
├── one.jpro.platform/
│   ├── jpro-routing-core/
│   │   ├── overview.md                   # auto-generated: chapter titles + line ranges
│   │   └── DOCUMENTATION.md              # full documentation
│   ├── jpro-file/
│   │   ├── overview.md
│   │   └── DOCUMENTATION.md
│   └── jpro-media/
│       ├── overview.md
│       └── DOCUMENTATION.md
```

**`index.md`** — lightweight entry point listing all available libraries:
```markdown
# AI Documentation Index

## Available Libraries
- one.jpro.platform:jpro-routing-core (0.5.8) — [overview](one.jpro.platform/jpro-routing-core/overview.md)
- one.jpro.platform:jpro-file (0.5.8) — [overview](one.jpro.platform/jpro-file/overview.md)
- one.jpro.platform:jpro-media (0.5.8) — [overview](one.jpro.platform/jpro-media/overview.md)
```

**`overview.md`** — auto-generated per library by parsing markdown headings:
```markdown
# jpro-routing-core (0.5.8)
Full documentation: DOCUMENTATION.md

## Chapters
- Overview (lines 1-25)
- Getting Started (lines 26-80)
- Filters API (lines 81-150)
- SEO Utilities (lines 151-200)
- AppCrawler (lines 201-260)
```

### How AI agents navigate this

1. Read `index.md` — see all available libraries (small, always cheap)
2. Read `overview.md` for the relevant library — see chapter structure
3. Read only the specific line range from `DOCUMENTATION.md` — load just the chapter needed

Three small reads instead of dumping everything into context.

---

## Phase 2: AI Agent Skill ✅ (done — SKILL.md is generated into .claude/skills/docs/)

A Claude Code skill (e.g. `/docs`) that teaches the AI agent how to use the collected documentation:

1. Reads `build/ai-docs/index.md` to see what's available
2. When the user asks about a library, reads the relevant `overview.md`
3. Reads specific line ranges from `DOCUMENTATION.md` as needed

The skill acts as instructions — it tells the agent the convention and file structure so it knows where to look.

---

## Phase 3: Publish Documentation (Convention) ✅ (done — see "For Library Authors" in README)

Publishing is just convention — no plugin needed. Library authors:
1. Write a `DOCUMENTATION.md`
2. Attach it as a Maven artifact with classifier `DOCUMENTATION` and extension `md`

---

## Phase 4: Smart Features

### Auto-discovery
- If a dependency doesn't publish `DOCUMENTATION.md`, fall back to:
  1. Check if the dependency has a `README.md` in its source jar
  2. Check if there's an `llms.txt` at the project's website

### Aggregation
- For multi-module projects, aggregate all sub-module docs into one

---

## Phase 5: Ecosystem

### Publishing
- Publish both plugins to Gradle Plugin Portal / Maven Central

### Convention promotion
- Document the `DOCUMENTATION` classifier convention
- Encourage adoption by submitting PRs to popular libraries

---

## Nice-to-Have Ideas

- **Dependency diff docs** — when upgrading a dependency, show what changed in the docs
- **Source jar indexing** — as a fallback, extract Javadoc comments from source jars and convert to markdown
  (basic version done: sources-index.md lists all source files with line counts)

---

## Open Improvements

- **Publishing** — publish to Gradle Plugin Portal / Maven Central (license: Apache-2.0, done).
