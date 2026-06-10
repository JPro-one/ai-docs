package one.jpro.platform.aidocs.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EntriesFileTest {

    @Test
    void roundTripsAllFields(@TempDir Path tempDir) throws IOException {
        var pom = new PomMetadata("My Lib", "A test library", "https://example.com",
                "https://github.com/example/lib", "Apache-2.0");
        var full = DocEntry.of("com.example", "my-lib", "1.0.0")
                .withDescription("Does things.")
                .withHasSources(true)
                .withHasChangelog(true)
                .withPomMetadata(pom);
        var minimal = DocEntry.of("org.other", "bare", "2.0");

        Path file = tempDir.resolve("entries.dat");
        EntriesFile.write(file, List.of(full, minimal));
        List<DocEntry> read = EntriesFile.read(file);

        assertThat(read).containsExactly(full, minimal);
    }

    @Test
    void handlesSpecialCharacters(@TempDir Path tempDir) throws IOException {
        var entry = DocEntry.of("com.example", "my-lib", "1.0.0")
                .withDescription("Tabs\tand\nnewlines — and ümlauts");

        Path file = tempDir.resolve("entries.dat");
        EntriesFile.write(file, List.of(entry));

        assertThat(EntriesFile.read(file)).containsExactly(entry);
    }

    @Test
    void emptyList(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("entries.dat");
        EntriesFile.write(file, List.of());

        assertThat(EntriesFile.read(file)).isEmpty();
    }
}
