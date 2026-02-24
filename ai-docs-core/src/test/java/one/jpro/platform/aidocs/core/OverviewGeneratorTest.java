package one.jpro.platform.aidocs.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OverviewGeneratorTest {

    @Test
    void simpleDocument() {
        var lines = List.of(
                "# My Library",
                "Some intro text.",
                "",
                "## Getting Started",
                "Step 1: add dependency.",
                "Step 2: use it.",
                "",
                "## API Reference",
                "The main class is Foo."
        );
        var entry = new DocEntry("com.example", "my-lib", "1.0.0");

        String result = OverviewGenerator.generate(lines, entry);

        assertThat(result).contains("# my-lib (1.0.0)");
        assertThat(result).contains("Full documentation: DOCUMENTATION.md");
        assertThat(result).contains("- My Library (lines 1-3)");
        assertThat(result).contains("- Getting Started (lines 4-7)");
        assertThat(result).contains("- API Reference (lines 8-9)");
    }

    @Test
    void singleChapter() {
        var lines = List.of(
                "# Overview",
                "This is the only chapter."
        );
        var entry = new DocEntry("org.example", "single", "2.0.0");

        String result = OverviewGenerator.generate(lines, entry);

        assertThat(result).contains("- Overview (lines 1-2)");
    }

    @Test
    void emptyDocument() {
        var entry = new DocEntry("org.example", "empty", "1.0.0");

        String result = OverviewGenerator.generate(List.of(), entry);

        assertThat(result).contains("# empty (1.0.0)");
        assertThat(result).contains("## Chapters");
        assertThat(result).doesNotContain("- ");
    }

    @Test
    void noHeadings() {
        var lines = List.of(
                "Just some text.",
                "No headings here.",
                "Plain content."
        );
        var entry = new DocEntry("org.example", "noheadings", "1.0.0");

        String result = OverviewGenerator.generate(lines, entry);

        assertThat(result).contains("## Chapters");
        // No chapter entries since there are no headings
        assertThat(result.lines().filter(l -> l.startsWith("- ")).count()).isZero();
    }

    @Test
    void nestedHeadings() {
        var lines = List.of(
                "# Top Level",
                "Intro.",
                "## Section A",
                "Content A.",
                "### Subsection A1",
                "Detail.",
                "## Section B",
                "Content B."
        );
        var entry = new DocEntry("org.example", "nested", "1.0.0");

        String result = OverviewGenerator.generate(lines, entry);

        assertThat(result).contains("- Top Level (lines 1-2)");
        assertThat(result).contains("- Section A (lines 3-4)");
        assertThat(result).contains("- Subsection A1 (lines 5-6)");
        assertThat(result).contains("- Section B (lines 7-8)");
    }

    @Test
    void writeToFile(@TempDir Path tempDir) throws IOException {
        Path docFile = tempDir.resolve("DOCUMENTATION.md");
        Files.writeString(docFile, """
                # My Library
                Some intro.

                ## Usage
                Use it like this.
                """);

        Path overviewFile = tempDir.resolve("overview.md");
        var entry = new DocEntry("com.example", "my-lib", "1.0.0");

        OverviewGenerator.generate(overviewFile, docFile, entry);

        assertThat(overviewFile).exists();
        String content = Files.readString(overviewFile);
        assertThat(content).contains("# my-lib (1.0.0)");
        assertThat(content).contains("- My Library");
        assertThat(content).contains("- Usage");
    }
}
