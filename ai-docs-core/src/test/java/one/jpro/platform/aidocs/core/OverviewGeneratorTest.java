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
                "The main class is Foo.",
                "More API details.",
                "Even more details."
        );
        var entry = new DocEntry("com.example", "my-lib", "1.0.0");

        String result = OverviewGenerator.generate(lines, entry);

        assertThat(result).contains("# my-lib (1.0.0)");
        assertThat(result).contains("Full documentation: DOCUMENTATION.md");
        assertThat(result).contains("- My Library (11 lines 1-11) — Some intro text.");
        assertThat(result).contains("  - Getting Started (4 lines 4-7) — Step 1: add dependency.");
        assertThat(result).contains("  - API Reference (4 lines 8-11) — The main class is Foo.");
    }

    @Test
    void singleChapter() {
        var lines = List.of(
                "# Overview",
                "This is the only chapter."
        );
        var entry = new DocEntry("org.example", "single", "2.0.0");

        String result = OverviewGenerator.generate(lines, entry);

        // Top-level chapters are always kept regardless of size
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
                "More content A.",
                "Even more A.",
                "### Subsection A1",
                "Detail line 1.",
                "Detail line 2.",
                "Detail line 3.",
                "## Section B",
                "Content B.",
                "More B.",
                "Even more B."
        );
        var entry = new DocEntry("org.example", "nested", "1.0.0");

        String result = OverviewGenerator.generate(lines, entry);

        assertThat(result).contains("- Top Level (14 lines 1-14) — Intro.");
        assertThat(result).contains("  - Section A (8 lines 3-10) — Content A.");
        assertThat(result).contains("    - Subsection A1 (4 lines 7-10) — Detail line 1.");
        assertThat(result).contains("  - Section B (4 lines 11-14) — Content B.");
    }

    @Test
    void shortSubChaptersFilteredFromOverview() {
        var lines = List.of(
                "# Top Level",
                "Intro.",
                "## Big Section",
                "Line 1.",
                "Line 2.",
                "Line 3.",
                "## Tiny Section",
                "Brief."
        );
        var entry = new DocEntry("org.example", "filtered", "1.0.0");

        String result = OverviewGenerator.generate(lines, entry);

        // Top-level always kept
        assertThat(result).contains("- Top Level (8 lines 1-8)");
        // Big Section has 4 lines (>= 3), kept
        assertThat(result).contains("  - Big Section (4 lines 3-6)");
        // Tiny Section has 2 lines (< 3), filtered
        assertThat(result).doesNotContain("Tiny Section");
    }

    @Test
    void topLevelChaptersAlwaysKeptRegardlessOfSize() {
        var lines = List.of(
                "# Short Chapter",
                "Brief."
        );
        var entry = new DocEntry("org.example", "short-top", "1.0.0");

        String result = OverviewGenerator.generate(lines, entry);

        // Top-level chapters are never filtered even if short
        assertThat(result).contains("- Short Chapter (2 lines 1-2) — Brief.");
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

        // All at min depth — always kept regardless of size
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
                "# Getting Started",       // 1
                "Overview.",                // 2
                "## Creating a project",    // 3
                "Project setup.",           // 4
                "More setup details.",      // 5
                "### From index.html",      // 6
                "HTML approach line 1.",    // 7
                "HTML approach line 2.",    // 8
                "HTML approach line 3.",    // 9
                "### From Gradle",          // 10
                "Gradle approach line 1.", // 11
                "Gradle approach line 2.", // 12
                "Gradle approach line 3.", // 13
                "## API Reference",         // 14
                "API docs.",                // 15
                "More API docs.",           // 16
                "Even more API."            // 17
        );
        var entry = new DocEntry("org.example", "deep", "1.0.0");

        String result = OverviewGenerator.generate(lines, entry);

        assertThat(result).contains("- Getting Started (17 lines 1-17) — Overview.");
        assertThat(result).contains("  - Creating a project (11 lines 3-13) — Project setup.");
        assertThat(result).contains("    - From index.html (4 lines 6-9) — HTML approach line 1.");
        assertThat(result).contains("    - From Gradle (4 lines 10-13) — Gradle approach line 1.");
        assertThat(result).contains("  - API Reference (4 lines 14-17) — API docs.");
    }

    @Test
    void hashInsideCodeBlocksIgnored() {
        var lines = List.of(
                "# Deployment",
                "How to deploy.",
                "",
                "## Docker",
                "Use this Dockerfile:",
                "",
                "```docker",
                "FROM ubuntu:24.04",
                "# Install dependencies",
                "RUN apt-get update",
                "# Configure the server",
                "RUN echo done",
                "```",
                "",
                "## Configuration",
                "Set these properties:",
                "",
                "```properties",
                "# Set the server count",
                "server.count=1",
                "```",
                "",
                "After configuration, restart."
        );
        var entry = new DocEntry("org.example", "deploy", "1.0.0");

        String result = OverviewGenerator.generate(lines, entry);

        // Real headings should be present
        assertThat(result).contains("- Deployment");
        assertThat(result).contains("Docker");
        assertThat(result).contains("Configuration");
        // Comments inside code blocks must NOT appear as chapters
        assertThat(result).doesNotContain("Install dependencies");
        assertThat(result).doesNotContain("Configure the server");
        assertThat(result).doesNotContain("Set the server count");
    }

    @Test
    void codeBlockDoesNotLeakSummary() {
        var lines = List.of(
                "# Setup",
                "",
                "```bash",
                "# This is a bash comment",
                "echo hello",
                "```",
                "",
                "Real description here."
        );
        var entry = new DocEntry("org.example", "setup", "1.0.0");

        String result = OverviewGenerator.generate(lines, entry);

        // The summary should be the real text, not the bash comment
        assertThat(result).contains("— Real description here.");
        assertThat(result).doesNotContain("bash comment");
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
    }
}
