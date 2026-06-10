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
 * (.java, .scala, .kt, .groovy) organized by package. The jar itself stays in the
 * local artifact cache; its absolute path is stored in the sibling sources.jar.link
 * file, keeping the index machine-independent. AI agents read single files via
 * {@code unzip -p "$(cat sources.jar.link)" <path>} or extract everything for
 * method-level lookup.
 */
public class SourcesIndexGenerator {

    public static final String LINK_FILE = "sources.jar.link";

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
        sb.append("Source jar: in the local artifact cache, path stored in `").append(LINK_FILE)
                .append("` (next to this file). Run the commands below from this directory.\n");
        sb.append("Read one file: `unzip -p \"$(cat ").append(LINK_FILE).append(")\" <directory><file>`");
        // Prefer a packaged file as the example — it demonstrates the directory prefix
        var examplePkg = packageToFiles.entrySet().stream()
                .filter(e -> !e.getKey().equals(ROOT_LABEL)).findFirst()
                .or(() -> packageToFiles.entrySet().stream().findFirst());
        if (examplePkg.isPresent()) {
            String dir = examplePkg.get().getKey();
            String examplePath = (dir.equals(ROOT_LABEL) ? "" : dir) + examplePkg.get().getValue().get(0).name();
            sb.append(", e.g. `unzip -p \"$(cat ").append(LINK_FILE).append(")\" ").append(examplePath).append("`");
        }
        sb.append("\n");
        sb.append("Extract all (best for finding methods and their javadoc): `unzip -q \"$(cat ")
                .append(LINK_FILE).append(")\" -d sources`\n\n");
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

    static final String ROOT_LABEL = "(root)";

    /**
     * Lists all source files in a jar, grouped by directory path (e.g. "com/example/"),
     * so entries can be concatenated directly into unzip paths.
     * Returns a sorted map of directory path to sorted list of file names.
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
                    pkg = name.substring(0, lastSlash + 1);
                    fileName = name.substring(lastSlash + 1);
                } else {
                    pkg = ROOT_LABEL;
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
