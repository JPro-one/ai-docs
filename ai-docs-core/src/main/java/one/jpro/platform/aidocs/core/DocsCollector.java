package one.jpro.platform.aidocs.core;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

/**
 * Orchestrates the documentation collection process. Given a list of documentation
 * files and their metadata, writes the full AI-navigable file structure:
 *
 * <pre>
 * outputDir/
 * ├── index.md
 * ├── group/
 * │   └── artifact/
 * │       ├── overview.md
 * │       └── DOCUMENTATION.md
 * </pre>
 *
 * Build-tool plugins (Gradle, Maven) handle dependency resolution and call this class
 * with the resolved files.
 */
public class DocsCollector {

    /**
     * Writes a single documentation file into the output structure and generates its overview.
     * Returns the entry enriched with a description extracted from the documentation.
     *
     * @param outputDir the root output directory (e.g. build/ai-docs)
     * @param sourceFile the resolved DOCUMENTATION.md file
     * @param entry metadata about the dependency
     * @param overviewMinLines sub-chapters shorter than this are omitted from overview.md
     * @return the entry with description populated from the documentation
     */
    public static DocEntry collectDoc(Path outputDir, Path sourceFile, DocEntry entry, int overviewMinLines) throws IOException {
        Path libDir = outputDir.resolve(entry.group()).resolve(entry.name());
        Files.createDirectories(libDir);

        Path docTarget = libDir.resolve("DOCUMENTATION.md");
        Files.copy(sourceFile, docTarget, StandardCopyOption.REPLACE_EXISTING);

        List<String> lines = Files.readAllLines(docTarget);
        String description = extractDescription(lines);
        DocEntry enriched = entry.withDescription(description);

        Path overviewTarget = libDir.resolve("overview.md");
        OverviewGenerator.generate(overviewTarget, docTarget, enriched, overviewMinLines);

        return enriched;
    }

    /**
     * Extracts the first non-empty, non-heading, non-badge line from the documentation as a description.
     */
    static String extractDescription(List<String> lines) {
        for (String line : lines) {
            if (!line.isBlank() && !line.startsWith("#")
                    && !line.startsWith("![") && !line.startsWith("[![")) {
                String trimmed = line.strip();
                if (trimmed.length() > 150) {
                    return trimmed.substring(0, 147) + "...";
                }
                return trimmed;
            }
        }
        return null;
    }

    /**
     * Copies a CHANGELOG.md into the output structure and generates its changelog-overview.md.
     *
     * @param outputDir the root output directory (e.g. build/ai-docs)
     * @param changelogFile the resolved CHANGELOG.md file
     * @param entry metadata about the dependency
     * @param overviewMinLines sub-chapters shorter than this are omitted from changelog-overview.md
     */
    public static void collectChangelog(Path outputDir, Path changelogFile, DocEntry entry, int overviewMinLines) throws IOException {
        Path libDir = outputDir.resolve(entry.group()).resolve(entry.name());
        Files.createDirectories(libDir);

        Path changelogTarget = libDir.resolve("CHANGELOG.md");
        Files.copy(changelogFile, changelogTarget, StandardCopyOption.REPLACE_EXISTING);

        Path overviewTarget = libDir.resolve("changelog-overview.md");
        OverviewGenerator.generate(overviewTarget, changelogTarget, entry, overviewMinLines);
    }

    /**
     * Generates a sources-index.md plus a sources.jar.link file holding the absolute path
     * of the sources jar in the local artifact cache. The jar is not copied — commands in
     * the index reference it via {@code $(cat sources.jar.link)}.
     *
     * @param outputDir the root output directory (e.g. build/ai-docs)
     * @param sourcesJar the resolved sources jar file
     * @param entry metadata about the dependency
     */
    public static void collectSources(Path outputDir, Path sourcesJar, DocEntry entry) throws IOException {
        Path libDir = outputDir.resolve(entry.group()).resolve(entry.name());
        Files.createDirectories(libDir);

        Files.writeString(libDir.resolve(SourcesIndexGenerator.LINK_FILE),
                sourcesJar.toAbsolutePath() + "\n");

        Path indexTarget = libDir.resolve("sources-index.md");
        SourcesIndexGenerator.generate(indexTarget, sourcesJar, entry);
    }

    /**
     * Generates both context.md and index.md. Context is generated first because
     * index.md references line ranges within context.md.
     *
     * @param outputDir the root output directory
     * @param entries all collected documentation entries (with descriptions)
     * @param contextMinLines sub-chapters shorter than this are omitted from context.md
     */
    public static void generateContextAndIndex(Path outputDir, List<DocEntry> entries, int contextMinLines) throws IOException {
        String contextContent = ContextGenerator.generate(outputDir, entries, contextMinLines);
        Files.writeString(outputDir.resolve("context.md"), contextContent);
        IndexGenerator.generate(outputDir.resolve("index.md"), entries, contextContent);
    }

    /**
     * Generates the SKILL.md file that tells AI agents about the documentation system.
     *
     * @param skillDir the directory to write SKILL.md into (e.g. .claude/skills/docs)
     * @param docsOutputDir the relative path to the docs output directory (e.g. "build/ai-docs")
     * @param buildTool the build tool whose collect command should be referenced
     * @return true if the file was written, false if a user-owned file was left untouched
     */
    public static boolean generateSkill(Path skillDir, String docsOutputDir, BuildTool buildTool) throws IOException {
        return SkillGenerator.generate(skillDir.resolve("SKILL.md"), docsOutputDir, buildTool);
    }

    /**
     * Recursively copies a directory tree.
     */
    public static void copyDirectory(Path source, Path target) throws IOException {
        Files.createDirectories(target);
        try (var entries = Files.list(source)) {
            for (Path entry : entries.toList()) {
                Path dest = target.resolve(entry.getFileName().toString());
                if (Files.isDirectory(entry)) {
                    copyDirectory(entry, dest);
                } else {
                    Files.copy(entry, dest, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    /**
     * Cleans the output directory.
     */
    public static void cleanOutputDir(Path outputDir) throws IOException {
        if (Files.exists(outputDir)) {
            deleteRecursively(outputDir);
        }
        Files.createDirectories(outputDir);
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (var entries = Files.list(path)) {
                for (Path entry : entries.toList()) {
                    deleteRecursively(entry);
                }
            }
        }
        Files.deleteIfExists(path);
    }
}
