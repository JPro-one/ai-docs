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
                new DocEntry("one.jpro.platform", "jpro-file", "0.5.8"),
                new DocEntry("one.jpro.platform", "jpro-routing-core", "0.5.8"),
                new DocEntry("com.example", "other-lib", "2.0.0")
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
    void writeToFile(@TempDir Path tempDir) throws IOException {
        var entries = List.of(
                new DocEntry("com.example", "lib-a", "1.0.0"),
                new DocEntry("com.example", "lib-b", "2.0.0")
        );

        Path indexFile = tempDir.resolve("index.md");
        IndexGenerator.generate(indexFile, entries);

        assertThat(indexFile).exists();
        String content = Files.readString(indexFile);
        assertThat(content).contains("lib-a");
        assertThat(content).contains("lib-b");
    }
}
