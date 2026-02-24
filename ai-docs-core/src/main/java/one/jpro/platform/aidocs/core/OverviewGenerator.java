package one.jpro.platform.aidocs.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    /**
     * Generates overview content from documentation lines.
     */
    static String generate(List<String> lines, DocEntry entry) {
        var sb = new StringBuilder();
        sb.append("# ").append(entry.name()).append(" (").append(entry.version()).append(")\n");
        sb.append("Full documentation: DOCUMENTATION.md\n\n");
        sb.append("## Chapters\n");

        String currentHeading = null;
        int currentStart = -1;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith("#")) {
                if (currentHeading != null) {
                    sb.append("- ").append(currentHeading)
                            .append(" (lines ").append(currentStart).append("-").append(i).append(")\n");
                }
                currentHeading = line.replaceFirst("^#+\\s*", "");
                currentStart = i + 1;
            }
        }

        if (currentHeading != null) {
            sb.append("- ").append(currentHeading)
                    .append(" (lines ").append(currentStart).append("-").append(lines.size()).append(")\n");
        }

        return sb.toString();
    }
}
