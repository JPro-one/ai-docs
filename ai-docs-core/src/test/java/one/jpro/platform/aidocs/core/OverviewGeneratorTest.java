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
        assertThat(result).contains("- My Library (9 lines 1-9) — Some intro text.");
        assertThat(result).contains("  - Getting Started (4 lines 4-7) — Step 1: add dependency.");
        assertThat(result).contains("  - API Reference (2 lines 8-9) — The main class is Foo.");
    }

    @Test
    void singleChapter() {
        var lines = List.of(
                "# Overview",
                "This is the only chapter."
        );
        var entry = new DocEntry("org.example", "single", "2.0.0");

        String result = OverviewGenerator.generate(lines, entry);

        assertThat(result).contains("- Overview (2 lines 1-2) — This is the only chapter.");
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
        assertThat(result.lines().filter(l -> l.stripLeading().startsWith("- ")).count()).isZero();
    }

    @Test
    void parentChapterEncompassesChildren() {
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

        assertThat(result).contains("- Top Level (8 lines 1-8) — Intro.");
        assertThat(result).contains("  - Section A (4 lines 3-6) — Content A.");
        assertThat(result).contains("    - Subsection A1 (2 lines 5-6) — Detail.");
        assertThat(result).contains("  - Section B (2 lines 7-8) — Content B.");
    }

    @Test
    void allSameLevelHeadings() {
        var lines = List.of(
                "## Chapter One",
                "Content one.",
                "## Chapter Two",
                "Content two."
        );
        var entry = new DocEntry("org.example", "flat", "1.0.0");

        String result = OverviewGenerator.generate(lines, entry);

        assertThat(result).contains("- Chapter One (2 lines 1-2) — Content one.");
        assertThat(result).contains("- Chapter Two (2 lines 3-4) — Content two.");
    }

    @Test
    void summaryTruncation() {
        String longLine = "A".repeat(200);
        var lines = List.of(
                "# Title",
                longLine
        );
        var entry = new DocEntry("org.example", "long", "1.0.0");

        String result = OverviewGenerator.generate(lines, entry);

        assertThat(result).contains("...");
        assertThat(result).doesNotContain(longLine);
    }

    @Test
    void headingWithNoSummaryText() {
        var lines = List.of(
                "# Title",
                "",
                "## Empty Section",
                "",
                "## Next Section",
                "Has content."
        );
        var entry = new DocEntry("org.example", "sparse", "1.0.0");

        String result = OverviewGenerator.generate(lines, entry);

        assertThat(result).contains("- Title (6 lines 1-6)");
        assertThat(result).doesNotContain("- Title (6 lines 1-6) —");
        assertThat(result).contains("  - Empty Section (2 lines 3-4)");
        assertThat(result).contains("  - Next Section (2 lines 5-6) — Has content.");
    }

    @Test
    void singleLineChapter() {
        var lines = List.of(
                "# Only Heading"
        );
        var entry = new DocEntry("org.example", "single-line", "1.0.0");

        String result = OverviewGenerator.generate(lines, entry);

        assertThat(result).contains("- Only Heading (1 line 1-1)");
    }

    @Test
    void deeplyNestedHierarchy() {
        var lines = List.of(
                "# Getting Started",
                "Overview.",
                "## Creating a project",
                "Project setup.",
                "### From index.html",
                "HTML approach.",
                "### From Gradle",
                "Gradle approach.",
                "## API Reference",
                "API docs."
        );
        var entry = new DocEntry("org.example", "deep", "1.0.0");

        String result = OverviewGenerator.generate(lines, entry);

        assertThat(result).contains("- Getting Started (10 lines 1-10) — Overview.");
        assertThat(result).contains("  - Creating a project (6 lines 3-8) — Project setup.");
        assertThat(result).contains("    - From index.html (2 lines 5-6) — HTML approach.");
        assertThat(result).contains("    - From Gradle (2 lines 7-8) — Gradle approach.");
        assertThat(result).contains("  - API Reference (2 lines 9-10) — API docs.");
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
