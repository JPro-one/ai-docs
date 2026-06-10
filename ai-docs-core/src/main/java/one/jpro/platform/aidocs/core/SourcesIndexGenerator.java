package one.jpro.platform.aidocs.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Generates a sources-index.md file from a sources jar, listing all source files
 * (.java, .scala, .kt, .groovy) organized by package. This allows AI agents to discover
 * and selectively read source files using {@code unzip -p sources.jar <path>}.
 */
public class SourcesIndexGenerator {

    private static final List<String> SOURCE_EXTENSIONS = List.of(".java", ".scala", ".kt", ".groovy");

    record FileEntry(String name, int lineCount) implements Comparable<FileEntry> {
        @Override
        public int compareTo(FileEntry other) {
            return this.name.compareTo(other.name);
        }
    }

    /**
     * Generates a sources-index.md file from a sources jar.
     *
     * @param sourcesIndexPath path to write the sources-index.md
     * @param sourcesJar path to the sources jar
     * @param entry the documentation entry metadata
     */
    public static void generate(Path sourcesIndexPath, Path sourcesJar, DocEntry entry) throws IOException {
        String content = generate(sourcesJar, entry);
        Files.writeString(sourcesIndexPath, content);
    }

    /**
     * Generates sources-index.md content from a sources jar.
     */
    static String generate(Path sourcesJar, DocEntry entry) throws IOException {
        Map<String, List<FileEntry>> packageToFiles = listSourceFiles(sourcesJar);

        var sb = new StringBuilder();
        sb.append("# ").append(entry.displayName()).append(" (").append(entry.version()).append(") — Source Index\n");
        sb.append("Source jar: sources.jar\n");
        sb.append("To read a source file: `unzip -p sources.jar <path>`\n\n");
        sb.append("## Packages\n");

        for (var pkgEntry : packageToFiles.entrySet()) {
            String pkg = pkgEntry.getKey();
            List<FileEntry> files = pkgEntry.getValue();
            sb.append("- ").append(pkg).append(" (").append(files.size())
                    .append(files.size() == 1 ? " file)" : " files)").append("\n");
            for (FileEntry file : files) {
                sb.append("  - ").append(file.name())
                        .append(" (").append(file.lineCount()).append(file.lineCount() == 1 ? " line)" : " lines)")
                        .append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * Lists all source files in a jar, grouped by package (derived from directory path).
     * Returns a sorted map of package name to sorted list of file names.
     */
    static Map<String, List<FileEntry>> listSourceFiles(Path jarPath) throws IOException {
        Map<String, List<FileEntry>> result = new TreeMap<>();

        try (var zipFile = new ZipFile(jarPath.toFile())) {
            var entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry ze = entries.nextElement();
                String name = ze.getName();
                if (ze.isDirectory() || SOURCE_EXTENSIONS.stream().noneMatch(name::endsWith)) continue;

                int lineCount;
                try (var reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(ze), StandardCharsets.UTF_8))) {
                    lineCount = (int) reader.lines().count();
                }

                int lastSlash = name.lastIndexOf('/');
                String pkg;
                String fileName;
                if (lastSlash >= 0) {
                    pkg = name.substring(0, lastSlash).replace('/', '.');
                    fileName = name.substring(lastSlash + 1);
                } else {
                    pkg = "(default package)";
                    fileName = name;
                }

                result.computeIfAbsent(pkg, k -> new java.util.ArrayList<>()).add(new FileEntry(fileName, lineCount));
            }
        }

        // Sort file entries within each package
        for (List<FileEntry> files : result.values()) {
            files.sort(Comparator.naturalOrder());
        }

        return result;
    }
}
