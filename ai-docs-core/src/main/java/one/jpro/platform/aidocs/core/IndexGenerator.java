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
                    sb.append("- ").append(entry.coordinate());
                    if (!entry.displayName().equals(entry.name())) {
                        sb.append(" (").append(entry.displayName()).append(")");
                    }
                    sb.append(" — [overview](").append(overviewPath).append(")");
                    if (entry.hasSources()) {
                        String sourcesPath = entry.relativePath() + "/sources-index.md";
                        sb.append(" | [sources](").append(sourcesPath).append(")");
                    }
                    sb.append("\n");
                    String desc = entry.effectiveDescription();
                    if (desc != null) {
                        sb.append("  ").append(desc);
                        PomMetadata pom = entry.pomMetadata();
                        if (pom != null) {
                            if (pom.url() != null) {
                                sb.append(" [Homepage](").append(pom.url()).append(")");
                            }
                            if (pom.license() != null) {
                                sb.append(" · ").append(pom.license());
                            }
                        }
                        sb.append("\n");
                    } else {
                        PomMetadata pom = entry.pomMetadata();
                        if (pom != null && (pom.url() != null || pom.license() != null)) {
                            sb.append("  ");
                            if (pom.url() != null) {
                                sb.append("[Homepage](").append(pom.url()).append(")");
                            }
                            if (pom.license() != null) {
                                if (pom.url() != null) sb.append(" · ");
                                sb.append(pom.license());
                            }
                            sb.append("\n");
                        }
                    }
                });

        return sb.toString();
    }
}
