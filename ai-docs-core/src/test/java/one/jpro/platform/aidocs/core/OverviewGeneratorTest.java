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
        // Top-level heading spans entire document (encompasses sub-chapters)
        assertThat(result).contains("- My Library (lines 1-9) — Some intro text.");
        // Sub-headings indented 2 spaces, Getting Started ends where API Reference starts
        assertThat(result).contains("  - Getting Started (lines 4-7) — Step 1: add dependency.");
        assertThat(result).contains("  - API Reference (lines 8-9) — The main class is Foo.");
    }

    @Test
    void singleChapter() {
        var lines = List.of(
                "# Overview",
                "This is the only chapter."
        );
        var entry = new DocEntry("org.example", "single", "2.0.0");

        String result = OverviewGenerator.generate(lines, entry);

        assertThat(result).contains("- Overview (lines 1-2) — This is the only chapter.");
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

        // Top Level (depth 1) spans entire doc including all sub-chapters
        assertThat(result).contains("- Top Level (lines 1-8) — Intro.");
        // Section A (depth 2) spans through its child Subsection A1, up to Section B
        assertThat(result).contains("  - Section A (lines 3-6) — Content A.");
        // Subsection A1 (depth 3) ends at Section B boundary
        assertThat(result).contains("    - Subsection A1 (lines 5-6) — Detail.");
        // Section B (depth 2) has no following sibling, goes to end
        assertThat(result).contains("  - Section B (lines 7-8) — Content B.");
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

        // Same depth siblings don't encompass each other
        assertThat(result).contains("- Chapter One (lines 1-2) — Content one.");
        assertThat(result).contains("- Chapter Two (lines 3-4) — Content two.");
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

        // Title encompasses all sub-chapters
        assertThat(result).contains("- Title (lines 1-6)");
        assertThat(result).doesNotContain("- Title (lines 1-6) —");
        assertThat(result).contains("  - Empty Section (lines 3-4)");
        assertThat(result).contains("  - Next Section (lines 5-6) — Has content.");
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

        // Getting Started spans all sub-chapters
        assertThat(result).contains("- Getting Started (lines 1-10) — Overview.");
        // Creating a project spans its children, ends at API Reference
        assertThat(result).contains("  - Creating a project (lines 3-8) — Project setup.");
        // Leaf sub-sections end at siblings
        assertThat(result).contains("    - From index.html (lines 5-6) — HTML approach.");
        assertThat(result).contains("    - From Gradle (lines 7-8) — Gradle approach.");
        // API Reference goes to end
        assertThat(result).contains("  - API Reference (lines 9-10) — API docs.");
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
