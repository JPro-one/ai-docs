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

    /** Sub-chapters shorter than this are omitted from overview.md */
    static final int OVERVIEW_MIN_LINES = 3;
    /** Sub-chapters shorter than this are omitted from context.md */
    static final int CONTEXT_MIN_LINES = 30;

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

    record ChapterInfo(String title, int depth, int startLine, int endLine, String summary) {
        int lineCount() { return endLine - startLine + 1; }
    }

    /**
     * Generates overview content from documentation lines (uses OVERVIEW_MIN_LINES threshold).
     */
    static String generate(List<String> lines, DocEntry entry) {
        return generateChapterListing(lines, entry, OVERVIEW_MIN_LINES);
    }

    /**
     * Generates chapter listing with a configurable minimum line threshold for sub-chapters.
     */
    static String generateChapterListing(List<String> lines, DocEntry entry, int minLineCount) {
        var sb = new StringBuilder();
        sb.append("# ").append(entry.name()).append(" (").append(entry.version()).append(")\n");
        sb.append("Full documentation: DOCUMENTATION.md\n\n");
        sb.append("## Chapters\n");

        List<ChapterInfo> chapters = parseChapters(lines);
        if (chapters.isEmpty()) return sb.toString();

        int minDepth = chapters.stream().mapToInt(ChapterInfo::depth).min().orElse(1);

        for (ChapterInfo ch : chapters) {
            // Always keep top-level chapters; skip sub-chapters below the threshold
            if (ch.depth() > minDepth && ch.lineCount() < minLineCount) continue;
            appendChapter(sb, ch.title(), ch.depth(), minDepth, ch.startLine(), ch.endLine(), ch.summary());
        }

        return sb.toString();
    }

    /**
     * Parses documentation lines into a list of chapters with computed line ranges.
     */
    static List<ChapterInfo> parseChapters(List<String> lines) {
        // First pass: collect all headings with their metadata
        record Heading(String title, int depth, int startLine, String summary) {}
        List<Heading> headings = new ArrayList<>();
        String currentHeading = null;
        int currentDepth = 0;
        int currentStart = -1;
        String currentSummary = null;

        boolean inCodeBlock = false;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith("```")) {
                inCodeBlock = !inCodeBlock;
                continue;
            }
            if (inCodeBlock) continue;
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

        // Second pass: compute end lines.
        // A heading's end line = the line before the next heading at the same or shallower depth,
        // or the end of the document if no such heading exists.
        List<ChapterInfo> chapters = new ArrayList<>();
        for (int i = 0; i < headings.size(); i++) {
            Heading h = headings.get(i);
            int endLine = lines.size();
            for (int j = i + 1; j < headings.size(); j++) {
                if (headings.get(j).depth() <= h.depth()) {
                    endLine = headings.get(j).startLine() - 1;
                    break;
                }
            }
            chapters.add(new ChapterInfo(h.title(), h.depth(), h.startLine(), endLine, h.summary()));
        }
        return chapters;
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
        int lineCount = endLine - startLine + 1;
        sb.append("  ".repeat(indent));
        sb.append("- ").append(heading)
                .append(" (").append(lineCount).append(lineCount == 1 ? " line " : " lines ")
                .append(startLine).append("-").append(endLine).append(")");
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
