package one.jpro.platform.aidocs.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates an overview.md for a DOCUMENTATION.md file by parsing markdown headings
 * and recording their line ranges. This allows AI agents to read specific chapters
 * without loading the full document.
 */
public class OverviewGenerator {

    /**
     * Generates an overview.md file from a DOCUMENTATION.md file.
     *
     * @param overviewPath path to write the overview.md
     * @param docPath path to the source DOCUMENTATION.md
     * @param entry the documentation entry metadata
     */
    public static void generate(Path overviewPath, Path docPath, DocEntry entry) throws IOException {
        List<String> lines = Files.readAllLines(docPath);
        String content = generate(lines, entry);
        Files.writeString(overviewPath, content);
    }

    private record Heading(String title, int depth, int startLine, String summary) {}

    /**
     * Generates overview content from documentation lines.
     */
    static String generate(List<String> lines, DocEntry entry) {
        var sb = new StringBuilder();
        sb.append("# ").append(entry.name()).append(" (").append(entry.version()).append(")\n");
        sb.append("Full documentation: DOCUMENTATION.md\n\n");
        sb.append("## Chapters\n");

        // First pass: collect all headings with their metadata
        List<Heading> headings = new ArrayList<>();
        String currentHeading = null;
        int currentDepth = 0;
        int currentStart = -1;
        String currentSummary = null;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith("#")) {
                if (currentHeading != null) {
                    headings.add(new Heading(currentHeading, currentDepth, currentStart, currentSummary));
                }
                currentDepth = headingDepth(line);
                currentHeading = line.replaceFirst("^#+\\s*", "");
                currentStart = i + 1;
                currentSummary = null;
            } else if (currentHeading != null && currentSummary == null && !line.isBlank()) {
                currentSummary = truncate(line.strip(), 120);
            }
        }
        if (currentHeading != null) {
            headings.add(new Heading(currentHeading, currentDepth, currentStart, currentSummary));
        }

        if (headings.isEmpty()) return sb.toString();

        // Find minimum depth for indentation
        int minDepth = headings.stream().mapToInt(Heading::depth).min().orElse(1);

        // Second pass: compute end lines and emit.
        // A heading's end line = the line before the next heading at the same or shallower depth,
        // or the end of the document if no such heading exists.
        for (int i = 0; i < headings.size(); i++) {
            Heading h = headings.get(i);
            int endLine = lines.size(); // default: end of document
            for (int j = i + 1; j < headings.size(); j++) {
                if (headings.get(j).depth() <= h.depth()) {
                    endLine = headings.get(j).startLine() - 1; // line before that heading's # line
                    break;
                }
            }
            appendChapter(sb, h.title(), h.depth(), minDepth, h.startLine(), endLine, h.summary());
        }

        return sb.toString();
    }

    private static int headingDepth(String line) {
        int depth = 0;
        while (depth < line.length() && line.charAt(depth) == '#') {
            depth++;
        }
        return depth;
    }

    private static void appendChapter(StringBuilder sb, String heading, int depth, int minDepth,
                                       int startLine, int endLine, String summary) {
        int indent = depth - minDepth;
        sb.append("  ".repeat(indent));
        sb.append("- ").append(heading)
                .append(" (lines ").append(startLine).append("-").append(endLine).append(")");
        if (summary != null) {
            sb.append(" — ").append(summary);
        }
        sb.append("\n");
    }

    private static String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
