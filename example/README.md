# AI Docs Example

Demonstrates how an AI agent can build a real application using frameworks it has never seen before — by reading the collected documentation.

## Setup

```bash
# From the root project
./gradlew publishToMavenLocal

# From this directory
gradle collectDocs
```

## Prompt

Open this directory in Claude Code and use the following prompt:

```
Build an Expense Tracker web application using JavaFX and JPro.
Use jpro-routing for multi-page navigation and jpro-auth-routing for Google login.
Read the documentation in build/ai-docs/ to understand the frameworks.
```

The agent will discover the available libraries, learn their APIs from the collected docs, and generate the application.
