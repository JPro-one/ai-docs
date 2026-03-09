package one.jpro.platform.aidocs.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/**
 * Generates a combined context.md file that gives an AI agent a single file
 * to read for a complete picture of all available library documentation.
 */
public class ContextGenerator {

    /**
     * Generates a context.md file combining all library information.
     *
     * @param contextPath path to write the context.md
     * @param outputDir the root output directory (used to read DOCUMENTATION.md files)
     * @param entries the collected documentation entries
     * @param contextMinLines sub-chapters shorter than this are omitted from context
     */
    public static void generate(Path contextPath, Path outputDir, List<DocEntry> entries, int contextMinLines) throws IOException {
        String content = generate(outputDir, entries, contextMinLines);
        Files.writeString(contextPath, content);
    }

    /**
     * Generates context content from documentation entries.
     *
     * @param contextMinLines sub-chapters shorter than this are omitted from context
     */
    static String generate(Path outputDir, List<DocEntry> entries, int contextMinLines) throws IOException {
        var sb = new StringBuilder();
        sb.append("# Project Documentation Context\n\n");
        sb.append("This file provides a combined overview of all available library documentation.\n");
        sb.append("For full details on any library, read its DOCUMENTATION.md file.\n\n");

        var sorted = entries.stream()
                .sorted(Comparator.comparing(DocEntry::coordinate))
                .toList();

        for (DocEntry entry : sorted) {
            sb.append("## ").append(entry.coordinate());
            if (!entry.displayName().equals(entry.name())) {
                sb.append(" (").append(entry.displayName()).append(")");
            }
            sb.append("\n");
            String desc = entry.effectiveDescription();
            if (desc != null) {
                sb.append(desc).append("\n");
            }
            PomMetadata pom = entry.pomMetadata();
            if (pom != null && (pom.url() != null || pom.scmUrl() != null || pom.license() != null)) {
                boolean hasPrev = false;
                if (pom.url() != null) {
                    sb.append("[Homepage](").append(pom.url()).append(")");
                    hasPrev = true;
                }
                if (pom.scmUrl() != null) {
                    if (hasPrev) sb.append(" · ");
                    sb.append("[Repository](").append(pom.scmUrl()).append(")");
                    hasPrev = true;
                }
                if (pom.license() != null) {
                    if (hasPrev) sb.append(" · ");
                    sb.append(pom.license());
                }
                sb.append("\n");
            }
            // Reference available files
            Path docPath = outputDir.resolve(entry.relativePath()).resolve("DOCUMENTATION.md");
            if (Files.exists(docPath)) {
                sb.append("Full docs: ").append(entry.relativePath()).append("/DOCUMENTATION.md\n");
            }
            if (entry.hasSources()) {
                sb.append("Sources: ").append(entry.relativePath()).append("/sources-index.md\n");
            }
            if (entry.hasChangelog()) {
                sb.append("Changelog: ").append(entry.relativePath()).append("/changelog-overview.md\n");
            }
            sb.append("\n");

            // Generate chapter listing directly from DOCUMENTATION.md with context threshold
            if (Files.exists(docPath)) {
                List<String> docLines = Files.readAllLines(docPath);
                List<OverviewGenerator.ChapterInfo> chapters = OverviewGenerator.parseChapters(docLines);
                if (!chapters.isEmpty()) {
                    int minDepth = chapters.stream()
                            .mapToInt(OverviewGenerator.ChapterInfo::depth).min().orElse(1);
                    sb.append("### Chapters\n");
                    for (OverviewGenerator.ChapterInfo ch : OverviewGenerator.filterChapters(chapters, contextMinLines)) {
                        int indent = ch.depth() - minDepth;
                        int lineCount = ch.lineCount();
                        sb.append("  ".repeat(indent));
                        sb.append("- ").append(ch.title())
                                .append(" (").append(lineCount).append(lineCount == 1 ? " line " : " lines ")
                                .append(ch.startLine()).append("-").append(ch.endLine()).append(")");
                        if (ch.summary() != null) {
                            sb.append(" — ").append(ch.summary());
                        }
                        sb.append("\n");
                    }
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
