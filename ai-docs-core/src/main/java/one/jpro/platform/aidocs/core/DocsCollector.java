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
     *
     * @param outputDir the root output directory (e.g. build/ai-docs)
     * @param sourceFile the resolved DOCUMENTATION.md file
     * @param entry metadata about the dependency
     */
    public static void collectDoc(Path outputDir, Path sourceFile, DocEntry entry) throws IOException {
        Path libDir = outputDir.resolve(entry.group()).resolve(entry.name());
        Files.createDirectories(libDir);

        Path docTarget = libDir.resolve("DOCUMENTATION.md");
        Files.copy(sourceFile, docTarget, StandardCopyOption.REPLACE_EXISTING);

        Path overviewTarget = libDir.resolve("overview.md");
        OverviewGenerator.generate(overviewTarget, docTarget, entry);
    }

    /**
     * Generates the index.md file from all collected entries.
     *
     * @param outputDir the root output directory
     * @param entries all collected documentation entries
     */
    public static void generateIndex(Path outputDir, List<DocEntry> entries) throws IOException {
        IndexGenerator.generate(outputDir.resolve("index.md"), entries);
    }

    /**
     * Generates the SKILL.md file that tells AI agents about the documentation system.
     *
     * @param skillDir the directory to write SKILL.md into (e.g. .claude/skills/docs)
     * @param docsOutputDir the relative path to the docs output directory (e.g. "build/ai-docs")
     */
    public static void generateSkill(Path skillDir, String docsOutputDir) throws IOException {
        SkillGenerator.generate(skillDir.resolve("SKILL.md"), docsOutputDir);
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
