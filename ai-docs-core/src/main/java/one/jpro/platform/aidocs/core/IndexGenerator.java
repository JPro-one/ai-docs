package one.jpro.platform.aidocs.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Generates a compact index.md that serves as a table of contents for context.md.
 * Each library entry includes its line range in context.md, so an AI agent can
 * read just the relevant section — the same pattern overview.md uses for DOCUMENTATION.md.
 */
public class IndexGenerator {

    record LibrarySection(DocEntry entry, int startLine, int endLine) {}

    /**
     * Generates an index.md file with line references into the given context.md content.
     *
     * @param indexPath path to write the index.md
     * @param entries the collected documentation entries
     * @param contextContent the already-generated context.md content
     */
    public static void generate(Path indexPath, List<DocEntry> entries, String contextContent) throws IOException {
        String content = generate(entries, contextContent);
        Files.writeString(indexPath, content);
    }

    /**
     * Generates index content with line references into context.md.
     */
    static String generate(List<DocEntry> entries, String contextContent) {
        List<LibrarySection> sections = findSections(entries, contextContent);

        var sb = new StringBuilder();
        sb.append("# AI Documentation Index\n\n");
        sb.append("Compact table of contents for context.md.\n");
        sb.append("Read a library's section by line range: lines START-END of context.md\n\n");
        sb.append("## Libraries\n");

        for (LibrarySection section : sections) {
            if (!section.entry().testOnly()) {
                appendSection(sb, section);
            }
        }

        if (sections.stream().anyMatch(s -> s.entry().testOnly())) {
            sb.append("\n## Test Dependencies\n");
            for (LibrarySection section : sections) {
                if (section.entry().testOnly()) {
                    appendSection(sb, section);
                }
            }
        }

        return sb.toString();
    }

    private static void appendSection(StringBuilder sb, LibrarySection section) {
        DocEntry entry = section.entry();
        sb.append("- ").append(entry.coordinate());
        if (!entry.displayName().equals(entry.name())) {
            sb.append(" (").append(entry.displayName()).append(")");
        }
        sb.append(" (lines ").append(section.startLine())
                .append("-").append(section.endLine()).append(")");
        String desc = entry.effectiveDescription();
        if (desc != null) {
            sb.append(" — ").append(desc);
        }
        sb.append("\n");
    }

    /**
     * Finds the line range of each library's section in context.md by scanning for ## headings.
     */
    static List<LibrarySection> findSections(List<DocEntry> entries, String contextContent) {
        List<String> lines = contextContent.lines().toList();
        var sorted = entries.stream()
                .sorted(Comparator.comparing(DocEntry::coordinate))
                .toList();

        List<LibrarySection> sections = new ArrayList<>();
        for (DocEntry entry : sorted) {
            String prefix = "## " + entry.coordinate();
            int startLine = -1;
            int endLine = lines.size();

            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).startsWith(prefix)) {
                    startLine = i + 1; // 1-based
                    // Find end: next ## heading or end of file
                    for (int j = i + 1; j < lines.size(); j++) {
                        if (lines.get(j).startsWith("## ")) {
                            endLine = j; // exclusive, so last content line is j-1 → 1-based: j
                            break;
                        }
                    }
                    break;
                }
            }

            if (startLine != -1) {
                // Trim trailing blank lines
                while (endLine > startLine && lines.get(endLine - 1).isBlank()) {
                    endLine--;
                }
                sections.add(new LibrarySection(entry, startLine, endLine));
            }
        }
        return sections;
    }
}
