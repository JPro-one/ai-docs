package one.jpro.platform.aidocs.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class DocsCollectorTest {

    @Test
    void collectSingleDoc(@TempDir Path tempDir) throws IOException {
        Path outputDir = tempDir.resolve("ai-docs");
        Path sourceDoc = tempDir.resolve("source.md");
        Files.writeString(sourceDoc, """
                # My Library
                Some intro.

                ## Getting Started
                Step 1.
                Step 2.
                Step 3.
                """);

        var entry = DocEntry.of("com.example", "my-lib", "1.0.0");
        DocsCollector.cleanOutputDir(outputDir);
        DocEntry enriched = DocsCollector.collectDoc(outputDir, sourceDoc, entry, 1);
        DocsCollector.generateContextAndIndex(outputDir, List.of(enriched), 50);

        // Check description was extracted
        assertThat(enriched.description()).isEqualTo("Some intro.");

        // Check file structure
        assertThat(outputDir.resolve("index.md")).exists();
        assertThat(outputDir.resolve("context.md")).exists();
        assertThat(outputDir.resolve("com.example/my-lib/DOCUMENTATION.md")).exists();
        assertThat(outputDir.resolve("com.example/my-lib/overview.md")).exists();

        // Check content is copied correctly
        String doc = Files.readString(outputDir.resolve("com.example/my-lib/DOCUMENTATION.md"));
        assertThat(doc).contains("# My Library");
        assertThat(doc).contains("## Getting Started");

        // Check overview has chapters with summaries
        String overview = Files.readString(outputDir.resolve("com.example/my-lib/overview.md"));
        assertThat(overview).contains("My Library");
        assertThat(overview).contains("Getting Started");
        assertThat(overview).contains("Some intro.");

        // Check index lists the library with line range into context.md
        String index = Files.readString(outputDir.resolve("index.md"));
        assertThat(index).contains("com.example:my-lib:1.0.0");
    }

    @Test
    void collectMultipleDocs(@TempDir Path tempDir) throws IOException {
        Path outputDir = tempDir.resolve("ai-docs");
        DocsCollector.cleanOutputDir(outputDir);

        Path doc1 = tempDir.resolve("doc1.md");
        Files.writeString(doc1, "# Library A\nContent A.");
        var entry1 = DocEntry.of("com.example", "lib-a", "1.0.0");

        Path doc2 = tempDir.resolve("doc2.md");
        Files.writeString(doc2, "# Library B\nContent B.");
        var entry2 = DocEntry.of("com.example", "lib-b", "2.0.0");

        DocEntry enriched1 = DocsCollector.collectDoc(outputDir, doc1, entry1, 5);
        DocEntry enriched2 = DocsCollector.collectDoc(outputDir, doc2, entry2, 5);
        DocsCollector.generateContextAndIndex(outputDir, List.of(enriched1, enriched2), 50);

        assertThat(outputDir.resolve("com.example/lib-a/DOCUMENTATION.md")).exists();
        assertThat(outputDir.resolve("com.example/lib-b/DOCUMENTATION.md")).exists();

        assertThat(enriched1.description()).isEqualTo("Content A.");
        assertThat(enriched2.description()).isEqualTo("Content B.");

        String index = Files.readString(outputDir.resolve("index.md"));
        assertThat(index).contains("lib-a");
        assertThat(index).contains("lib-b");
    }

    @Test
    void collectDocWithNoDescription(@TempDir Path tempDir) throws IOException {
        Path outputDir = tempDir.resolve("ai-docs");
        Path sourceDoc = tempDir.resolve("source.md");
        Files.writeString(sourceDoc, """
                # Title Only
                """);

        var entry = DocEntry.of("com.example", "title-only", "1.0.0");
        DocsCollector.cleanOutputDir(outputDir);
        DocEntry enriched = DocsCollector.collectDoc(outputDir, sourceDoc, entry, 5);

        assertThat(enriched.description()).isNull();
    }

    @Test
    void generateContext(@TempDir Path tempDir) throws IOException {
        Path outputDir = tempDir.resolve("ai-docs");
        DocsCollector.cleanOutputDir(outputDir);

        Path doc1 = tempDir.resolve("doc1.md");
        Files.writeString(doc1, "# Library A\nA great library.\n## Usage\nUse it.");
        var entry1 = DocEntry.of("com.example", "lib-a", "1.0.0");

        DocEntry enriched1 = DocsCollector.collectDoc(outputDir, doc1, entry1, 5);
        DocsCollector.generateContextAndIndex(outputDir, List.of(enriched1), 50);

        Path contextFile = outputDir.resolve("context.md");
        assertThat(contextFile).exists();
        String context = Files.readString(contextFile);
        assertThat(context).contains("com.example:lib-a:1.0.0");
        assertThat(context).contains("A great library.");
        assertThat(context).contains("Chapters");

        // Index should reference lines in context.md
        Path indexFile = outputDir.resolve("index.md");
        assertThat(indexFile).exists();
        String index = Files.readString(indexFile);
        assertThat(index).contains("com.example:lib-a:1.0.0");
        assertThat(index).contains("lines");
    }

    @Test
    void cleanOutputDirRemovesOldContent(@TempDir Path tempDir) throws IOException {
        Path outputDir = tempDir.resolve("ai-docs");
        Files.createDirectories(outputDir);
        Files.writeString(outputDir.resolve("old-file.txt"), "stale content");

        DocsCollector.cleanOutputDir(outputDir);

        assertThat(outputDir).exists();
        assertThat(outputDir.resolve("old-file.txt")).doesNotExist();
    }

    @Test
    void collectSameDocTwiceProducesSingleIndexEntry(@TempDir Path tempDir) throws IOException {
        Path outputDir = tempDir.resolve("ai-docs");
        DocsCollector.cleanOutputDir(outputDir);

        Path sourceDoc = tempDir.resolve("source.md");
        Files.writeString(sourceDoc, "# My Library\nSome intro.");

        var entry = DocEntry.of("com.example", "my-lib", "1.0.0");

        // Simulate what happens when the same dependency appears in multiple classpath configs:
        // collectDoc is called once, but the enriched entry (with description) is added to the list.
        // A second occurrence should be detected as duplicate via coordinate matching.
        DocEntry enriched = DocsCollector.collectDoc(outputDir, sourceDoc, entry, 5);

        // The enriched entry has a description, so record equals won't match the bare entry
        assertThat(enriched).isNotEqualTo(entry);
        assertThat(enriched.coordinate()).isEqualTo(entry.coordinate());

        // When building the index with duplicates, each coordinate should appear only once
        DocsCollector.generateContextAndIndex(outputDir, List.of(enriched), 50);
        String index = Files.readString(outputDir.resolve("index.md"));
        long count = index.lines().filter(l -> l.contains("com.example:my-lib:1.0.0")).count();
        assertThat(count).isEqualTo(1);
    }

    @Test
    void extractDescriptionFromLines() {
        assertThat(DocsCollector.extractDescription(List.of(
                "# Title",
                "Description text."
        ))).isEqualTo("Description text.");

        assertThat(DocsCollector.extractDescription(List.of(
                "# Title",
                "",
                "After blank line."
        ))).isEqualTo("After blank line.");

        assertThat(DocsCollector.extractDescription(List.of(
                "# Title",
                "## Another heading"
        ))).isNull();

        assertThat(DocsCollector.extractDescription(List.of(
                "# Title",
                "[![Build Status](https://example.com/badge.svg)](https://example.com)",
                "![Logo](logo.png)",
                "Actual description."
        ))).isEqualTo("Actual description.");

        assertThat(DocsCollector.extractDescription(List.of())).isNull();
    }

    @Test
    void displayNameFromPomMetadata() {
        var pom = new PomMetadata("My Library", null, null, null, null);
        var entry = DocEntry.of("com.example", "my-lib", "1.0.0").withPomMetadata(pom);

        assertThat(entry.displayName()).isEqualTo("My Library");
    }

    @Test
    void displayNameFallsBackToArtifactName() {
        var entry = DocEntry.of("com.example", "my-lib", "1.0.0");

        assertThat(entry.displayName()).isEqualTo("my-lib");
    }

    @Test
    void effectiveDescriptionPrefersPomDescription() {
        var pom = new PomMetadata(null, "POM description", null, null, null);
        var entry = DocEntry.of("com.example", "my-lib", "1.0.0").withDescription("Doc description").withPomMetadata(pom);

        assertThat(entry.effectiveDescription()).isEqualTo("POM description");
    }

    @Test
    void effectiveDescriptionFallsToDocDescription() {
        var entry = DocEntry.of("com.example", "my-lib", "1.0.0").withDescription("Doc description");

        assertThat(entry.effectiveDescription()).isEqualTo("Doc description");
    }

    @Test
    void effectiveDescriptionNullWhenNeitherAvailable() {
        var entry = DocEntry.of("com.example", "my-lib", "1.0.0");

        assertThat(entry.effectiveDescription()).isNull();
    }

    @Test
    void collectSources(@TempDir Path tempDir) throws IOException {
        Path outputDir = tempDir.resolve("ai-docs");
        DocsCollector.cleanOutputDir(outputDir);

        Path jar = createTestJar(tempDir, Map.of(
                "com/example/mylib/MyClass.java", "public class MyClass {}",
                "com/example/mylib/MyService.java", "public class MyService {}"
        ));

        var entry = DocEntry.of("com.example", "my-lib", "1.0.0");
        DocsCollector.collectSources(outputDir, jar, entry);

        // The jar is referenced from the cache, not copied into the output
        assertThat(outputDir.resolve("com.example/my-lib/sources.jar")).doesNotExist();
        assertThat(outputDir.resolve("com.example/my-lib/sources-index.md")).exists();

        // sources.jar.link holds the absolute jar path, usable via $(cat sources.jar.link)
        String link = Files.readString(outputDir.resolve("com.example/my-lib/sources.jar.link"));
        assertThat(link.strip()).isEqualTo(jar.toAbsolutePath().toString());

        // Check index content
        String index = Files.readString(outputDir.resolve("com.example/my-lib/sources-index.md"));
        assertThat(index).contains("my-lib (1.0.0) — Source Index");
        assertThat(index).contains("sources.jar.link");
        assertThat(index).contains("com/example/mylib/ (2 files)");
        assertThat(index).contains("MyClass.java");
        assertThat(index).contains("MyService.java");
    }

    @Test
    void indexContainsLineRangesForEachLibrary(@TempDir Path tempDir) throws IOException {
        Path outputDir = tempDir.resolve("ai-docs");
        DocsCollector.cleanOutputDir(outputDir);

        // Create doc files so context.md has chapter listings
        Path libDir = outputDir.resolve("com.example/lib-a");
        Files.createDirectories(libDir);
        Files.writeString(libDir.resolve("DOCUMENTATION.md"), "# Lib A\nContent A.");

        var entry1 = DocEntry.of("com.example", "lib-a", "1.0.0").withDescription("A library.").withHasSources(true);
        var entry2 = DocEntry.of("com.example", "lib-b", "2.0.0").withDescription("Another library.");

        DocsCollector.generateContextAndIndex(outputDir, List.of(entry1, entry2), 50);

        String index = Files.readString(outputDir.resolve("index.md"));
        // Both libraries have line ranges
        assertThat(index).contains("com.example:lib-a:1.0.0");
        assertThat(index).contains("com.example:lib-b:2.0.0");
        // Line ranges present
        assertThat(index.lines().filter(l -> l.startsWith("- ") && l.contains("lines")).count()).isEqualTo(2);
    }

    private Path createTestJar(Path dir, Map<String, String> entries) throws IOException {
        Path jarFile = dir.resolve("test-sources.jar");
        try (var jos = new JarOutputStream(Files.newOutputStream(jarFile))) {
            for (var e : entries.entrySet()) {
                jos.putNextEntry(new JarEntry(e.getKey()));
                jos.write(e.getValue().getBytes());
                jos.closeEntry();
            }
        }
        return jarFile;
    }
}
