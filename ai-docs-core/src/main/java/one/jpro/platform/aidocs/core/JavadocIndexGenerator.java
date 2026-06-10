package one.jpro.platform.aidocs.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Generates a javadoc-index.md listing the hand-written guide documents shipped in a
 * library's javadoc jar ({@code doc-files/*.html}, e.g. the JavaFX CSS reference),
 * each with a chapter overview parsed from its HTML headings. Generated per-class HTML
 * is skipped — the sources cover that better. The jar stays in the local artifact
 * cache, referenced via the sibling javadoc.jar.link file.
 */
public class JavadocIndexGenerator {

    public static final String LINK_FILE = "javadoc.jar.link";

    private static final Pattern GUIDE_PATH = Pattern.compile(".*/doc-files/[^/]+\\.html?");
    private static final Pattern HTML_HEADING = Pattern.compile("<h([1-4])[^>]*>(.*?)</h\\1>", Pattern.CASE_INSENSITIVE);
    private static final Pattern HTML_TITLE = Pattern.compile("<title[^>]*>(.*?)</title>", Pattern.CASE_INSENSITIVE);
    private static final Pattern TAGS = Pattern.compile("<[^>]+>");

    record Guide(String path, String title, int lineCount, List<OverviewGenerator.ChapterInfo> chapters) {}

    /**
     * Finds and analyzes the hand-written guide documents (doc-files HTML) inside a javadoc jar:
     * extracts each guide's title and chapter structure from its HTML headings.
     */
    static List<Guide> analyzeGuides(Path javadocJar) throws IOException {
        List<Guide> guides = new ArrayList<>();
        try (var zipFile = new ZipFile(javadocJar.toFile())) {
            var entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry ze = entries.nextElement();
                String name = ze.getName();
                if (ze.isDirectory() || !GUIDE_PATH.matcher(name).matches()) continue;

                List<String> lines;
                try (var reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(ze), StandardCharsets.UTF_8))) {
                    lines = reader.lines().toList();
                }
                guides.add(analyzeGuide(name, lines));
            }
        }
        guides.sort((a, b) -> a.path().compareTo(b.path()));
        return guides;
    }

    static Guide analyzeGuide(String path, List<String> lines) {
        record Heading(String text, int depth, int startLine) {}
        List<Heading> headings = new ArrayList<>();
        String titleTag = null;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (titleTag == null) {
                Matcher t = HTML_TITLE.matcher(line);
                if (t.find()) {
                    titleTag = stripTags(t.group(1));
                }
            }
            Matcher m = HTML_HEADING.matcher(line);
            while (m.find()) {
                String text = stripTags(m.group(2));
                if (!text.isBlank()) {
                    headings.add(new Heading(text, Integer.parseInt(m.group(1)), i + 1));
                }
            }
        }

        // Prefer the first h1 — <title> tags often carry site suffixes ("... | JavaFX 21")
        String title = headings.stream().filter(h -> h.depth() == 1).findFirst().map(Heading::text)
                .orElse(titleTag != null && !titleTag.isBlank() ? titleTag
                        : !headings.isEmpty() ? headings.get(0).text()
                        : path.substring(path.lastIndexOf('/') + 1));

        List<OverviewGenerator.ChapterInfo> chapters = new ArrayList<>();
        for (int i = 0; i < headings.size(); i++) {
            Heading h = headings.get(i);
            int endLine = lines.size();
            for (int j = i + 1; j < headings.size(); j++) {
                if (headings.get(j).depth() <= h.depth()) {
                    endLine = headings.get(j).startLine() - 1;
                    break;
                }
            }
            chapters.add(new OverviewGenerator.ChapterInfo(h.text(), h.depth(), h.startLine(), endLine, null));
        }
        return new Guide(path, title, lines.size(), chapters);
    }

    private static String stripTags(String html) {
        return TAGS.matcher(html).replaceAll("")
                .replace("&lt;", "<").replace("&gt;", ">")
                .replace("&quot;", "\"").replace("&#39;", "'").replace("&apos;", "'")
                .replace("&nbsp;", " ").replace("&amp;", "&")
                .strip();
    }

    /**
     * Generates a javadoc-index.md file.
     */
    public static void generate(Path indexPath, DocEntry entry, List<Guide> guides, int minLineCount) throws IOException {
        Files.writeString(indexPath, generate(entry, guides, minLineCount));
    }

    static String generate(DocEntry entry, List<Guide> guides, int minLineCount) {
        var sb = new StringBuilder();
        sb.append("# ").append(entry.displayName()).append(" (").append(entry.version()).append(") — Javadoc Guides\n");
        sb.append("Hand-written guide documents (HTML) from the library's javadoc jar, ")
                .append("in the local artifact cache with its path stored in `").append(LINK_FILE)
                .append("` (next to this file). Run the commands below from this directory.\n");
        sb.append("Read a guide (or a chapter's line range): `unzip -p \"$(cat ").append(LINK_FILE).append(")\" <path>`");
        if (!guides.isEmpty()) {
            sb.append(", e.g. `unzip -p \"$(cat ").append(LINK_FILE).append(")\" ").append(guides.get(0).path()).append("`");
        }
        sb.append("\n\n");
        sb.append("## Guides\n");
        for (Guide guide : guides) {
            sb.append("- ").append(guide.path())
                    .append(" — ").append(guide.title())
                    .append(" (").append(guide.lineCount()).append(" lines)\n");
            if (!guide.chapters().isEmpty()) {
                int minDepth = guide.chapters().stream()
                        .mapToInt(OverviewGenerator.ChapterInfo::depth).min().orElse(1);
                for (var ch : OverviewGenerator.filterChapters(guide.chapters(), minLineCount)) {
                    // shift minDepth so chapters indent one level below the guide entry
                    OverviewGenerator.appendChapter(sb, ch.title(), ch.depth(), minDepth - 1,
                            ch.startLine(), ch.endLine(), ch.summary());
                }
            }
        }
        return sb.toString();
    }
}
