package one.jpro.platform.aidocs.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        var mainEntries = sorted.stream().filter(e -> !e.testOnly()).toList();
        var testEntries = sorted.stream().filter(DocEntry::testOnly).toList();

        // Identical guide sets (e.g. the full JavaFX docs mirrored in every module's
        // javadoc jar) are listed once and referenced afterwards
        Map<List<String>, String> seenGuideSets = new HashMap<>();

        for (DocEntry entry : mainEntries) {
            writeEntry(sb, outputDir, entry, contextMinLines, seenGuideSets);
        }

        if (!testEntries.isEmpty()) {
            sb.append("# Test Dependencies\n\n");
            sb.append("Libraries that only appear on test classpaths.\n\n");
            for (DocEntry entry : testEntries) {
                writeEntry(sb, outputDir, entry, contextMinLines, seenGuideSets);
            }
        }

        return sb.toString();
    }

    private static void writeEntry(StringBuilder sb, Path outputDir, DocEntry entry,
                                   int contextMinLines, Map<List<String>, String> seenGuideSets) throws IOException {
        {
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
            if (entry.hasJavadocGuides()) {
                String firstWithSameGuides = seenGuideSets.putIfAbsent(entry.javadocGuideTitles(), entry.coordinate());
                if (firstWithSameGuides != null) {
                    sb.append("Guides: same as ").append(firstWithSameGuides).append("\n");
                } else {
                    sb.append("Guides (").append(String.join(", ", entry.javadocGuideTitles()))
                            .append("): ").append(entry.relativePath()).append("/javadoc-index.md\n");
                }
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
                        OverviewGenerator.appendChapter(sb, ch.title(), ch.depth(), minDepth,
                                ch.startLine(), ch.endLine(), ch.summary());
                    }
                }
            }
            sb.append("\n");
        }
    }
}
