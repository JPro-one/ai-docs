package one.jpro.platform.aidocs.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IndexGeneratorTest {

    @Test
    void multipleEntries() {
        var entries = List.of(
                new DocEntry("one.jpro.platform", "jpro-file", "0.5.8", "File handling library."),
                new DocEntry("one.jpro.platform", "jpro-routing-core", "0.5.8", "Routing framework."),
                new DocEntry("com.example", "other-lib", "2.0.0", "Another library.")
        );

        String result = IndexGenerator.generate(entries);

        assertThat(result).contains("# AI Documentation Index");
        // Should be sorted by coordinate
        assertThat(result).contains("com.example:other-lib:2.0.0");
        assertThat(result).contains("one.jpro.platform:jpro-file:0.5.8");
        assertThat(result).contains("one.jpro.platform:jpro-routing-core:0.5.8");
        // Should have overview links
        assertThat(result).contains("[overview](com.example/other-lib/overview.md)");
        assertThat(result).contains("[overview](one.jpro.platform/jpro-file/overview.md)");
        // Should have descriptions indented below each entry
        assertThat(result).contains("  File handling library.");
        assertThat(result).contains("  Routing framework.");
        assertThat(result).contains("  Another library.");
    }

    @Test
    void entryWithoutDescription() {
        var entries = List.of(
                new DocEntry("org.example", "no-desc", "1.0.0")
        );

        String result = IndexGenerator.generate(entries);

        assertThat(result).contains("- org.example:no-desc:1.0.0 — [overview](org.example/no-desc/overview.md)");
        // No description line
        assertThat(result).doesNotContain("  ");
    }

    @Test
    void entryWithDescription() {
        var entries = List.of(
                new DocEntry("org.example", "my-lib", "1.0.0", "A useful library.")
        );

        String result = IndexGenerator.generate(entries);

        assertThat(result).contains("- org.example:my-lib:1.0.0 — [overview](org.example/my-lib/overview.md)");
        assertThat(result).contains("  A useful library.");
    }

    @Test
    void emptyEntries() {
        String result = IndexGenerator.generate(List.of());

        assertThat(result).contains("# AI Documentation Index");
        assertThat(result).contains("## Available Libraries");
        assertThat(result.lines().filter(l -> l.startsWith("- ")).count()).isZero();
    }

    @Test
    void singleEntry() {
        var entries = List.of(
                new DocEntry("org.example", "my-lib", "1.0.0")
        );

        String result = IndexGenerator.generate(entries);

        assertThat(result).contains("- org.example:my-lib:1.0.0 — [overview](org.example/my-lib/overview.md)");
    }

    @Test
    void entryWithPomMetadata() {
        var pom = new PomMetadata("My Library", "A useful lib.", "https://example.com", "Apache-2.0");
        var entries = List.of(
                new DocEntry("org.example", "my-lib", "1.0.0", "Doc description.", false, pom)
        );

        String result = IndexGenerator.generate(entries);

        // Should show display name in parentheses
        assertThat(result).contains("org.example:my-lib:1.0.0 (My Library)");
        // Description comes from DOCUMENTATION.md (takes precedence over POM)
        assertThat(result).contains("  Doc description.");
        // Homepage and license from POM
        assertThat(result).contains("[Homepage](https://example.com)");
        assertThat(result).contains("Apache-2.0");
    }

    @Test
    void entryWithPomFallbackDescription() {
        var pom = new PomMetadata("My Library", "POM description.", "https://example.com", "MIT");
        var entries = List.of(
                new DocEntry("org.example", "my-lib", "1.0.0", null, false, pom)
        );

        String result = IndexGenerator.generate(entries);

        // effectiveDescription falls back to POM description
        assertThat(result).contains("  POM description.");
        assertThat(result).contains("[Homepage](https://example.com)");
        assertThat(result).contains("MIT");
    }

    @Test
    void entryWithPomNoDisplayNameDifference() {
        // When POM name equals artifact name, no parenthetical shown
        var pom = new PomMetadata("my-lib", null, null, null);
        var entries = List.of(
                new DocEntry("org.example", "my-lib", "1.0.0", null, false, pom)
        );

        String result = IndexGenerator.generate(entries);

        assertThat(result).doesNotContain("(my-lib)");
    }

    @Test
    void writeToFile(@TempDir Path tempDir) throws IOException {
        var entries = List.of(
                new DocEntry("com.example", "lib-a", "1.0.0", "Library A."),
                new DocEntry("com.example", "lib-b", "2.0.0", "Library B.")
        );

        Path indexFile = tempDir.resolve("index.md");
        IndexGenerator.generate(indexFile, entries);

        assertThat(indexFile).exists();
        String content = Files.readString(indexFile);
        assertThat(content).contains("lib-a");
        assertThat(content).contains("lib-b");
        assertThat(content).contains("  Library A.");
        assertThat(content).contains("  Library B.");
    }
}
