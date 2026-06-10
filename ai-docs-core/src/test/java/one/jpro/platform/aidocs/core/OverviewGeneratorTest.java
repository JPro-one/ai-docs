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
        var entry = DocEntry.of("com.example", "my-lib", "1.0.0");

        String result = OverviewGenerator.generate(lines, entry, 1);

        assertThat(result).contains("# my-lib (1.0.0)");
        assertThat(result).contains("Full content: DOCUMENTATION.md");
        assertThat(result).contains("- My Library (lines 1-11) — Some intro text.");
        assertThat(result).contains("  - Getting Started (lines 4-7) — Step 1: add dependency.");
        assertThat(result).contains("  - API Reference (lines 8-11) — The main class is Foo.");
    }

    @Test
    void singleChapter() {
        var lines = List.of(
                "# Overview",
                "This is the only chapter."
        );
        var entry = DocEntry.of("org.example", "single", "2.0.0");

        String result = OverviewGenerator.generate(lines, entry, 1);

        // Top-level chapters are always kept regardless of size
        assertThat(result).contains("- Overview (lines 1-2) — This is the only chapter.");
    }

    @Test
    void emptyDocument() {
        var entry = DocEntry.of("org.example", "empty", "1.0.0");

        String result = OverviewGenerator.generate(List.of(), entry, 1);

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
        var entry = DocEntry.of("org.example", "noheadings", "1.0.0");

        String result = OverviewGenerator.generate(lines, entry, 1);

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
        var entry = DocEntry.of("org.example", "nested", "1.0.0");

        String result = OverviewGenerator.generate(lines, entry, 1);

        assertThat(result).contains("- Top Level (lines 1-14) — Intro.");
        assertThat(result).contains("  - Section A (lines 3-10) — Content A.");
        assertThat(result).contains("    - Subsection A1 (lines 7-10) — Detail line 1.");
        assertThat(result).contains("  - Section B (lines 11-14) — Content B.");
    }

    @Test
    void childrenCollapsedForShortChapters() {
        var lines = List.of(
                "# Top Level",             // 1
                "Intro.",                   // 2
                "## Big Section",           // 3
                "Line 1.",                  // 4
                "Line 2.",                  // 5
                "Line 3.",                  // 6
                "### Sub of Big",           // 7
                "Detail.",                  // 8
                "## Small Section",         // 9
                "Brief.",                   // 10
                "### Sub of Small",         // 11
                "Hidden detail."            // 12
        );
        var entry = DocEntry.of("org.example", "filtered", "1.0.0");

        String result = OverviewGenerator.generate(lines, entry, 5);

        // All chapters are always shown
        assertThat(result).contains("- Top Level (lines 1-12)");
        // Big Section has 6 lines (>= 5), its children are shown
        assertThat(result).contains("  - Big Section (lines 3-8)");
        assertThat(result).contains("    - Sub of Big (lines 7-8)");
        // Small Section has 4 lines (< 5), shown but children collapsed
        assertThat(result).contains("  - Small Section (lines 9-12)");
        assertThat(result).doesNotContain("Sub of Small");
    }

    @Test
    void topLevelChaptersAlwaysKeptRegardlessOfSize() {
        var lines = List.of(
                "# Short Chapter",
                "Brief."
        );
        var entry = DocEntry.of("org.example", "short-top", "1.0.0");

        String result = OverviewGenerator.generate(lines, entry, 1);

        // Top-level chapters are never filtered even if short
        assertThat(result).contains("- Short Chapter (lines 1-2) — Brief.");
    }

    @Test
    void allSameLevelHeadings() {
        var lines = List.of(
                "## Chapter One",
                "Content one.",
                "## Chapter Two",
                "Content two."
        );
        var entry = DocEntry.of("org.example", "flat", "1.0.0");

        String result = OverviewGenerator.generate(lines, entry, 1);

        // All at min depth — always kept regardless of size
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
        var entry = DocEntry.of("org.example", "long", "1.0.0");

        String result = OverviewGenerator.generate(lines, entry, 1);

        assertThat(result).contains("...");
        assertThat(result).doesNotContain(longLine);
    }

    @Test
    void singleLineChapter() {
        var lines = List.of(
                "# Only Heading"
        );
        var entry = DocEntry.of("org.example", "single-line", "1.0.0");

        String result = OverviewGenerator.generate(lines, entry, 1);

        assertThat(result).contains("- Only Heading (lines 1-1)");
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
        var entry = DocEntry.of("org.example", "deep", "1.0.0");

        String result = OverviewGenerator.generate(lines, entry, 1);

        assertThat(result).contains("- Getting Started (lines 1-17) — Overview.");
        assertThat(result).contains("  - Creating a project (lines 3-13) — Project setup.");
        assertThat(result).contains("    - From index.html (lines 6-9) — HTML approach line 1.");
        assertThat(result).contains("    - From Gradle (lines 10-13) — Gradle approach line 1.");
        assertThat(result).contains("  - API Reference (lines 14-17) — API docs.");
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
        var entry = DocEntry.of("org.example", "deploy", "1.0.0");

        String result = OverviewGenerator.generate(lines, entry, 1);

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
        var entry = DocEntry.of("org.example", "setup", "1.0.0");

        String result = OverviewGenerator.generate(lines, entry, 1);

        // The summary should be the real text, not the bash comment
        assertThat(result).contains("— Real description here.");
        assertThat(result).doesNotContain("bash comment");
    }

    @Test
    void headerUsesDisplayNameFromPom() {
        var lines = List.of("# Title", "Content.");
        var pom = new PomMetadata("My Pretty Library", null, null, null, null);
        var entry = DocEntry.of("com.example", "my-lib", "1.0.0").withPomMetadata(pom);

        String result = OverviewGenerator.generate(lines, entry, 1);

        assertThat(result).contains("# My Pretty Library (1.0.0)");
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
        var entry = DocEntry.of("com.example", "my-lib", "1.0.0");

        OverviewGenerator.generate(overviewFile, docFile, entry, 1);

        assertThat(overviewFile).exists();
        String content = Files.readString(overviewFile);
        assertThat(content).contains("# my-lib (1.0.0)");
        assertThat(content).contains("- My Library");
    }

    @Test
    void tildeCodeBlocksDoNotCreateFalseHeadings() {
        var entry = DocEntry.of("org.example", "tilde", "1.0.0");
        var lines = List.of(
                "# Main",
                "Intro text.",
                "~~~python",
                "# this is a comment, not a heading",
                "print('hello')",
                "~~~",
                "## Real Section",
                "Content here."
        );

        String result = OverviewGenerator.generate(lines, entry, 1);

        assertThat(result).contains("- Main");
        assertThat(result).contains("- Real Section");
        assertThat(result).doesNotContain("this is a comment");
    }
}
