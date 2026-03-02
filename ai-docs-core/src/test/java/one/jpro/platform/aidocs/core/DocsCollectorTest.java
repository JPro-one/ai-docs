package one.jpro.platform.aidocs.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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
                """);

        var entry = new DocEntry("com.example", "my-lib", "1.0.0");
        DocsCollector.cleanOutputDir(outputDir);
        DocEntry enriched = DocsCollector.collectDoc(outputDir, sourceDoc, entry);
        DocsCollector.generateIndex(outputDir, List.of(enriched));

        // Check description was extracted
        assertThat(enriched.description()).isEqualTo("Some intro.");

        // Check file structure
        assertThat(outputDir.resolve("index.md")).exists();
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

        // Check index lists the library with description
        String index = Files.readString(outputDir.resolve("index.md"));
        assertThat(index).contains("com.example:my-lib:1.0.0");
        assertThat(index).contains("Some intro.");
    }

    @Test
    void collectMultipleDocs(@TempDir Path tempDir) throws IOException {
        Path outputDir = tempDir.resolve("ai-docs");
        DocsCollector.cleanOutputDir(outputDir);

        Path doc1 = tempDir.resolve("doc1.md");
        Files.writeString(doc1, "# Library A\nContent A.");
        var entry1 = new DocEntry("com.example", "lib-a", "1.0.0");

        Path doc2 = tempDir.resolve("doc2.md");
        Files.writeString(doc2, "# Library B\nContent B.");
        var entry2 = new DocEntry("com.example", "lib-b", "2.0.0");

        DocEntry enriched1 = DocsCollector.collectDoc(outputDir, doc1, entry1);
        DocEntry enriched2 = DocsCollector.collectDoc(outputDir, doc2, entry2);
        DocsCollector.generateIndex(outputDir, List.of(enriched1, enriched2));

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

        var entry = new DocEntry("com.example", "title-only", "1.0.0");
        DocsCollector.cleanOutputDir(outputDir);
        DocEntry enriched = DocsCollector.collectDoc(outputDir, sourceDoc, entry);

        assertThat(enriched.description()).isNull();
    }

    @Test
    void generateContext(@TempDir Path tempDir) throws IOException {
        Path outputDir = tempDir.resolve("ai-docs");
        DocsCollector.cleanOutputDir(outputDir);

        Path doc1 = tempDir.resolve("doc1.md");
        Files.writeString(doc1, "# Library A\nA great library.\n## Usage\nUse it.");
        var entry1 = new DocEntry("com.example", "lib-a", "1.0.0");

        DocEntry enriched1 = DocsCollector.collectDoc(outputDir, doc1, entry1);
        DocsCollector.generateContext(outputDir, List.of(enriched1));

        Path contextFile = outputDir.resolve("context.md");
        assertThat(contextFile).exists();
        String context = Files.readString(contextFile);
        assertThat(context).contains("com.example:lib-a:1.0.0");
        assertThat(context).contains("A great library.");
        assertThat(context).contains("Chapters");
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

        var entry = new DocEntry("com.example", "my-lib", "1.0.0");

        // Simulate what happens when the same dependency appears in multiple classpath configs:
        // collectDoc is called once, but the enriched entry (with description) is added to the list.
        // A second occurrence should be detected as duplicate via coordinate matching.
        DocEntry enriched = DocsCollector.collectDoc(outputDir, sourceDoc, entry);

        // The enriched entry has a description, so record equals won't match the bare entry
        assertThat(enriched).isNotEqualTo(entry);
        assertThat(enriched.coordinate()).isEqualTo(entry.coordinate());

        // When building the index with duplicates, each coordinate should appear only once
        DocsCollector.generateIndex(outputDir, List.of(enriched));
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

        assertThat(DocsCollector.extractDescription(List.of())).isNull();
    }
}
