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
        DocsCollector.collectDoc(outputDir, sourceDoc, entry);
        DocsCollector.generateIndex(outputDir, List.of(entry));

        // Check file structure
        assertThat(outputDir.resolve("index.md")).exists();
        assertThat(outputDir.resolve("com.example/my-lib/DOCUMENTATION.md")).exists();
        assertThat(outputDir.resolve("com.example/my-lib/overview.md")).exists();

        // Check content is copied correctly
        String doc = Files.readString(outputDir.resolve("com.example/my-lib/DOCUMENTATION.md"));
        assertThat(doc).contains("# My Library");
        assertThat(doc).contains("## Getting Started");

        // Check overview has chapters
        String overview = Files.readString(outputDir.resolve("com.example/my-lib/overview.md"));
        assertThat(overview).contains("- My Library");
        assertThat(overview).contains("- Getting Started");

        // Check index lists the library
        String index = Files.readString(outputDir.resolve("index.md"));
        assertThat(index).contains("com.example:my-lib:1.0.0");
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

        DocsCollector.collectDoc(outputDir, doc1, entry1);
        DocsCollector.collectDoc(outputDir, doc2, entry2);
        DocsCollector.generateIndex(outputDir, List.of(entry1, entry2));

        assertThat(outputDir.resolve("com.example/lib-a/DOCUMENTATION.md")).exists();
        assertThat(outputDir.resolve("com.example/lib-b/DOCUMENTATION.md")).exists();

        String index = Files.readString(outputDir.resolve("index.md"));
        assertThat(index).contains("lib-a");
        assertThat(index).contains("lib-b");
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
}
