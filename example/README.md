# AI Docs Example

Standalone example project to manually test the ai-docs Gradle plugin.

## Usage

1. First, publish the plugin to your local Maven repository:

```bash
cd ..
./gradlew publishToMavenLocal
```

2. Then run the collectDocs task:

```bash
cd example
gradle collectDocs
```

3. Inspect the output:

```bash
ls build/ai-docs/
cat build/ai-docs/index.md
```
