package one.jpro.platform.aidocs.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ContextGeneratorTest {

    @Test
    void singleLibrary(@TempDir Path tempDir) throws IOException {
        Path outputDir = tempDir.resolve("ai-docs");
        Path libDir = outputDir.resolve("com.example/my-lib");
        Files.createDirectories(libDir);

        // Create a DOCUMENTATION.md with chapters
        var docLines = new ArrayList<String>();
        docLines.add("# My Lib");
        docLines.add("A great library.");
        docLines.add("## Getting Started");
        for (int i = 0; i < 40; i++) docLines.add("Content line " + i);
        Files.writeString(libDir.resolve("DOCUMENTATION.md"), String.join("\n", docLines));

        var entry = DocEntry.of("com.example", "my-lib", "1.0.0").withDescription("A great library.");

        String result = ContextGenerator.generate(outputDir, List.of(entry), 30);

        assertThat(result).contains("# Project Documentation Context");
        assertThat(result).contains("## com.example:my-lib:1.0.0");
        assertThat(result).contains("A great library.");
        assertThat(result).contains("com.example/my-lib/DOCUMENTATION.md");
        assertThat(result).contains("### Chapters");
        // Both chapters are >= 30 lines or top-level
        assertThat(result).contains("- My Lib");
        assertThat(result).contains("Getting Started");
    }

    @Test
    void childrenCollapsedForShortChaptersInContext(@TempDir Path tempDir) throws IOException {
        Path outputDir = tempDir.resolve("ai-docs");
        Path libDir = outputDir.resolve("com.example/my-lib");
        Files.createDirectories(libDir);

        // Big Section has sub-chapters shown; Small Section's sub-chapters are collapsed
        var docLines = new ArrayList<String>();
        docLines.add("# My Lib");
        docLines.add("Intro.");
        docLines.add("## Big Section");
        for (int i = 0; i < 35; i++) docLines.add("Big content " + i);
        docLines.add("### Sub of Big");
        for (int i = 0; i < 5; i++) docLines.add("Sub big content " + i);
        docLines.add("## Small Section");
        for (int i = 0; i < 5; i++) docLines.add("Small content " + i);
        docLines.add("### Sub of Small");
        docLines.add("Hidden detail.");
        Files.writeString(libDir.resolve("DOCUMENTATION.md"), String.join("\n", docLines));

        var entry = DocEntry.of("com.example", "my-lib", "1.0.0").withDescription("A library.");

        String result = ContextGenerator.generate(outputDir, List.of(entry), 30);

        // All chapters shown
        assertThat(result).contains("- My Lib");
        // Big Section (>= 30 lines), children shown
        assertThat(result).contains("Big Section");
        assertThat(result).contains("Sub of Big");
        // Small Section (< 30 lines), shown but children collapsed
        assertThat(result).contains("Small Section");
        assertThat(result).doesNotContain("Sub of Small");
    }

    @Test
    void multipleLibraries(@TempDir Path tempDir) throws IOException {
        Path outputDir = tempDir.resolve("ai-docs");
        Files.createDirectories(outputDir.resolve("com.example/lib-a"));
        Files.createDirectories(outputDir.resolve("com.example/lib-b"));

        Files.writeString(outputDir.resolve("com.example/lib-a/DOCUMENTATION.md"),
                "# Lib A\nIntro A.");
        Files.writeString(outputDir.resolve("com.example/lib-b/DOCUMENTATION.md"),
                "# Lib B\nIntro B.");

        var entries = List.of(
                DocEntry.of("com.example", "lib-a", "1.0.0").withDescription("Library A."),
                DocEntry.of("com.example", "lib-b", "2.0.0").withDescription("Library B.")
        );

        String result = ContextGenerator.generate(outputDir, entries, 30);

        assertThat(result).contains("## com.example:lib-a:1.0.0");
        assertThat(result).contains("Library A.");
        assertThat(result).contains("## com.example:lib-b:2.0.0");
        assertThat(result).contains("Library B.");
    }

    @Test
    void libraryWithoutDescription(@TempDir Path tempDir) throws IOException {
        Path outputDir = tempDir.resolve("ai-docs");
        Path libDir = outputDir.resolve("org.example/no-desc");
        Files.createDirectories(libDir);

        Files.writeString(libDir.resolve("DOCUMENTATION.md"), "# No Desc\nContent.");

        var entry = DocEntry.of("org.example", "no-desc", "1.0.0");

        String result = ContextGenerator.generate(outputDir, List.of(entry), 30);

        assertThat(result).contains("## org.example:no-desc:1.0.0");
        assertThat(result).contains("org.example/no-desc/DOCUMENTATION.md");
    }

    @Test
    void libraryWithPomMetadata(@TempDir Path tempDir) throws IOException {
        Path outputDir = tempDir.resolve("ai-docs");
        Path libDir = outputDir.resolve("org.example/pom-lib");
        Files.createDirectories(libDir);

        Files.writeString(libDir.resolve("DOCUMENTATION.md"), "# POM Lib\nContent.");

        var pom = new PomMetadata("POM Library", "A POM-described lib.", "https://example.com", null, "MIT");
        var entry = DocEntry.of("org.example", "pom-lib", "1.0.0").withPomMetadata(pom);

        String result = ContextGenerator.generate(outputDir, List.of(entry), 30);

        assertThat(result).contains("## org.example:pom-lib:1.0.0 (POM Library)");
        // effectiveDescription falls back to POM
        assertThat(result).contains("A POM-described lib.");
        assertThat(result).contains("[Homepage](https://example.com)");
        assertThat(result).contains("MIT");
    }

    @Test
    void sourcesOnlyLibraryOmitsDocPath(@TempDir Path tempDir) throws IOException {
        Path outputDir = tempDir.resolve("ai-docs");
        Files.createDirectories(outputDir.resolve("org.example/src-only"));

        // No DOCUMENTATION.md — only sources
        var entry = DocEntry.of("org.example", "src-only", "1.0.0").withHasSources(true);

        String result = ContextGenerator.generate(outputDir, List.of(entry), 30);

        assertThat(result).contains("## org.example:src-only:1.0.0");
        assertThat(result).doesNotContain("Full docs:");
        assertThat(result).contains("Sources: org.example/src-only/sources-index.md");
    }

    @Test
    void libraryWithDocsAndSources(@TempDir Path tempDir) throws IOException {
        Path outputDir = tempDir.resolve("ai-docs");
        Path libDir = outputDir.resolve("com.example/both");
        Files.createDirectories(libDir);
        Files.writeString(libDir.resolve("DOCUMENTATION.md"), "# Both\nContent.");

        var entry = DocEntry.of("com.example", "both", "1.0.0").withDescription("Has both.").withHasSources(true);

        String result = ContextGenerator.generate(outputDir, List.of(entry), 30);

        assertThat(result).contains("Full docs: com.example/both/DOCUMENTATION.md");
        assertThat(result).contains("Sources: com.example/both/sources-index.md");
    }

    @Test
    void emptyEntries(@TempDir Path tempDir) throws IOException {
        Path outputDir = tempDir.resolve("ai-docs");
        Files.createDirectories(outputDir);

        String result = ContextGenerator.generate(outputDir, List.of(), 30);

        assertThat(result).contains("# Project Documentation Context");
        assertThat(result).doesNotContain("##");
    }

    @Test
    void writeToFile(@TempDir Path tempDir) throws IOException {
        Path outputDir = tempDir.resolve("ai-docs");
        Path libDir = outputDir.resolve("com.example/my-lib");
        Files.createDirectories(libDir);

        Files.writeString(libDir.resolve("DOCUMENTATION.md"), "# My Lib\nContent.");

        var entry = DocEntry.of("com.example", "my-lib", "1.0.0").withDescription("A lib.");
        Path contextFile = outputDir.resolve("context.md");

        ContextGenerator.generate(contextFile, outputDir, List.of(entry), 30);

        assertThat(contextFile).exists();
        String content = Files.readString(contextFile);
        assertThat(content).contains("# Project Documentation Context");
        assertThat(content).contains("com.example:my-lib:1.0.0");
    }
}
