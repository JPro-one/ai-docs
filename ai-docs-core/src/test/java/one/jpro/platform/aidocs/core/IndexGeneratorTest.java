package one.jpro.platform.aidocs.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IndexGeneratorTest {

    /**
     * Builds a minimal context.md content string for the given entries,
     * so IndexGenerator can find section headings and compute line ranges.
     */
    private static String fakeContext(List<DocEntry> entries) {
        var sb = new StringBuilder();
        sb.append("# Project Documentation Context\n\n");
        for (DocEntry entry : entries) {
            sb.append("## ").append(entry.coordinate());
            if (!entry.displayName().equals(entry.name())) {
                sb.append(" (").append(entry.displayName()).append(")");
            }
            sb.append("\n");
            String desc = entry.effectiveDescription();
            if (desc != null) {
                sb.append(desc).append("\n");
            }
            sb.append("Full docs: ").append(entry.relativePath()).append("/DOCUMENTATION.md\n");
            sb.append("\n");
        }
        return sb.toString();
    }

    @Test
    void multipleEntries() {
        var entries = List.of(
                DocEntry.of("one.jpro.platform", "jpro-file", "0.5.8").withDescription("File handling library."),
                DocEntry.of("one.jpro.platform", "jpro-routing-core", "0.5.8").withDescription("Routing framework."),
                DocEntry.of("com.example", "other-lib", "2.0.0").withDescription("Another library.")
        );
        String context = fakeContext(entries);

        String result = IndexGenerator.generate(entries, context);

        assertThat(result).contains("# AI Documentation Index");
        assertThat(result).contains("context.md");
        // Should be sorted by coordinate
        assertThat(result).contains("com.example:other-lib:2.0.0");
        assertThat(result).contains("one.jpro.platform:jpro-file:0.5.8");
        assertThat(result).contains("one.jpro.platform:jpro-routing-core:0.5.8");
        // Should have line ranges, not overview links
        assertThat(result).doesNotContain("[overview]");
        assertThat(result.lines().filter(l -> l.startsWith("- ") && l.contains("lines")).count()).isEqualTo(3);
    }

    @Test
    void entryWithoutDescription() {
        var entries = List.of(
                DocEntry.of("org.example", "no-desc", "1.0.0")
        );
        String context = fakeContext(entries);

        String result = IndexGenerator.generate(entries, context);

        assertThat(result).contains("- org.example:no-desc:1.0.0");
        assertThat(result).contains("lines");
    }

    @Test
    void emptyEntries() {
        String result = IndexGenerator.generate(List.of(), "# Project Documentation Context\n");

        assertThat(result).contains("# AI Documentation Index");
        assertThat(result).contains("## Libraries");
        assertThat(result.lines().filter(l -> l.startsWith("- ")).count()).isZero();
    }

    @Test
    void displayNameShownWhenDifferentFromArtifact() {
        var pom = new PomMetadata("My Library", "A useful lib.", "https://example.com", null, "Apache-2.0");
        var entries = List.of(
                DocEntry.of("org.example", "my-lib", "1.0.0").withDescription("Doc description.").withPomMetadata(pom)
        );
        String context = fakeContext(entries);

        String result = IndexGenerator.generate(entries, context);

        assertThat(result).contains("org.example:my-lib:1.0.0 (My Library)");
        // Index should NOT contain descriptions, homepage, license — all in context.md
        assertThat(result).doesNotContain("Doc description.");
        assertThat(result).doesNotContain("[Homepage]");
        assertThat(result).doesNotContain("Apache-2.0");
    }

    @Test
    void lineRangesAreAccurate() {
        var entries = List.of(
                DocEntry.of("com.example", "lib-a", "1.0.0").withDescription("Library A."),
                DocEntry.of("com.example", "lib-b", "2.0.0").withDescription("Library B.")
        );
        // Build a context where we know exact line numbers (1-based):
        // 1: # Project Documentation Context
        // 2: (blank)
        // 3: ## com.example:lib-a:1.0.0
        // 4: Library A.
        // 5: Full docs: com.example/lib-a/DOCUMENTATION.md
        // 6: (blank)
        // 7: ## com.example:lib-b:2.0.0
        // 8: Library B.
        // 9: Full docs: com.example/lib-b/DOCUMENTATION.md
        // 10: (blank - trailing, trimmed)
        String context = """
                # Project Documentation Context

                ## com.example:lib-a:1.0.0
                Library A.
                Full docs: com.example/lib-a/DOCUMENTATION.md

                ## com.example:lib-b:2.0.0
                Library B.
                Full docs: com.example/lib-b/DOCUMENTATION.md
                """;

        String result = IndexGenerator.generate(entries, context);

        // lib-a: lines 3-5 (heading through content, trailing blank trimmed before next ##)
        assertThat(result).contains("lib-a:1.0.0 (3 lines 3-5)");
        // lib-b: lines 7-9 (heading through content, trailing blank trimmed)
        assertThat(result).contains("lib-b:2.0.0 (3 lines 7-9)");
    }

    @Test
    void writeToFile(@TempDir Path tempDir) throws IOException {
        var entries = List.of(
                DocEntry.of("com.example", "lib-a", "1.0.0").withDescription("Library A."),
                DocEntry.of("com.example", "lib-b", "2.0.0").withDescription("Library B.")
        );
        String context = fakeContext(entries);

        Path indexFile = tempDir.resolve("index.md");
        IndexGenerator.generate(indexFile, entries, context);

        assertThat(indexFile).exists();
        String content = Files.readString(indexFile);
        assertThat(content).contains("lib-a");
        assertThat(content).contains("lib-b");
        assertThat(content).contains("lines");
    }
}
