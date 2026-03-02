package one.jpro.platform.aidocs.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/**
 * Generates an index.md listing all available documentation.
 */
public class IndexGenerator {

    /**
     * Generates an index.md file from a list of collected documentation entries.
     *
     * @param indexPath path to write the index.md
     * @param entries the collected documentation entries
     */
    public static void generate(Path indexPath, List<DocEntry> entries) throws IOException {
        String content = generate(entries);
        Files.writeString(indexPath, content);
    }

    /**
     * Generates index content from documentation entries.
     */
    static String generate(List<DocEntry> entries) {
        var sb = new StringBuilder();
        sb.append("# AI Documentation Index\n\n");
        sb.append("## Available Libraries\n");

        entries.stream()
                .sorted(Comparator.comparing(DocEntry::coordinate))
                .forEach(entry -> {
                    String overviewPath = entry.relativePath() + "/overview.md";
                    sb.append("- ").append(entry.coordinate())
                            .append(" — [overview](").append(overviewPath).append(")\n");
                    if (entry.description() != null) {
                        sb.append("  ").append(entry.description()).append("\n");
                    }
                });

        return sb.toString();
    }
}
