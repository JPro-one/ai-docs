package one.jpro.platform.aidocs.core;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Reads and writes a line-based file of {@link DocEntry} values. Used to pass collected
 * entries from per-project partial tasks to the aggregating task.
 */
public class EntriesFile {

    public static final String FILE_NAME = "entries.dat";

    public static void write(Path file, List<DocEntry> entries) throws IOException {
        var sb = new StringBuilder();
        for (DocEntry e : entries) {
            PomMetadata pom = e.pomMetadata();
            String guideTitles = e.javadocGuideTitles().stream()
                    .map(EntriesFile::enc).reduce((a, b) -> a + ";" + b).orElse("");
            sb.append(String.join("\t",
                    enc(e.group()), enc(e.name()), enc(e.version()),
                    enc(e.description()),
                    String.valueOf(e.hasSources()), String.valueOf(e.hasChangelog()),
                    guideTitles,
                    enc(pom == null ? null : pom.name()),
                    enc(pom == null ? null : pom.description()),
                    enc(pom == null ? null : pom.url()),
                    enc(pom == null ? null : pom.scmUrl()),
                    enc(pom == null ? null : pom.license())
            )).append("\n");
        }
        Files.writeString(file, sb.toString());
    }

    public static List<DocEntry> read(Path file) throws IOException {
        List<DocEntry> entries = new ArrayList<>();
        for (String line : Files.readAllLines(file)) {
            if (line.isBlank()) continue;
            String[] f = line.split("\t", -1);
            List<String> guideTitles = f[6].isEmpty() ? List.of()
                    : Stream.of(f[6].split(";")).map(EntriesFile::dec).toList();
            var entry = DocEntry.of(dec(f[0]), dec(f[1]), dec(f[2]))
                    .withDescription(dec(f[3]))
                    .withHasSources(Boolean.parseBoolean(f[4]))
                    .withHasChangelog(Boolean.parseBoolean(f[5]))
                    .withJavadocGuideTitles(guideTitles);
            var pom = new PomMetadata(dec(f[7]), dec(f[8]), dec(f[9]), dec(f[10]), dec(f[11]));
            if (pom.name() != null || pom.description() != null || pom.url() != null
                    || pom.scmUrl() != null || pom.license() != null) {
                entry = entry.withPomMetadata(pom);
            }
            entries.add(entry);
        }
        return entries;
    }

    private static String enc(String s) {
        return s == null ? "" : URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static String dec(String s) {
        return s.isEmpty() ? null : URLDecoder.decode(s, StandardCharsets.UTF_8);
    }
}
