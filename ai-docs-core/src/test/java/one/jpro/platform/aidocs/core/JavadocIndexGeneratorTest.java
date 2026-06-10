package one.jpro.platform.aidocs.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class JavadocIndexGeneratorTest {

    private static final String CSS_GUIDE = """
            <html><head><title>JavaFX CSS Reference Guide</title></head>
            <body>
            <h1>JavaFX CSS Reference Guide</h1>
            <p>intro text</p>
            <h2>Introduction</h2>
            <p>...</p>
            <p>...</p>
            <h2>Types</h2>
            <h3><a id="typecolor">Color</a></h3>
            <p>...</p>
            </body></html>
            """;

    @Test
    void analyzesOnlyDocFilesGuides(@TempDir Path tempDir) throws IOException {
        Path jar = createTestJar(tempDir, Map.of(
                "javafx.graphics/javafx/scene/doc-files/cssref.html", CSS_GUIDE,
                "javafx.graphics/javafx/scene/doc-files/cssexample1.png", "binary",
                "javafx.graphics/javafx/scene/Node.html", "<html>generated class doc</html>",
                "index.html", "<html>overview</html>"
        ));

        var guides = JavadocIndexGenerator.analyzeGuides(jar);

        assertThat(guides).hasSize(1);
        var guide = guides.get(0);
        assertThat(guide.path()).isEqualTo("javafx.graphics/javafx/scene/doc-files/cssref.html");
        assertThat(guide.title()).isEqualTo("JavaFX CSS Reference Guide");
        assertThat(guide.chapters()).isNotEmpty();
    }

    @Test
    void generatesIndexWithChapters(@TempDir Path tempDir) throws IOException {
        Path jar = createTestJar(tempDir, Map.of(
                "javafx.graphics/javafx/scene/doc-files/cssref.html", CSS_GUIDE
        ));
        var entry = DocEntry.of("org.openjfx", "javafx-graphics", "21.0.5");

        var guides = JavadocIndexGenerator.analyzeGuides(jar);
        String result = JavadocIndexGenerator.generate(entry, guides, 1);

        assertThat(result).contains("# javafx-graphics (21.0.5) — Javadoc Guides");
        assertThat(result).contains("javadoc.jar.link");
        assertThat(result).contains("- javafx.graphics/javafx/scene/doc-files/cssref.html — JavaFX CSS Reference Guide");
        assertThat(result).contains("e.g. `unzip -p \"$(cat javadoc.jar.link)\" javafx.graphics/javafx/scene/doc-files/cssref.html`");
        // Chapter overview parsed from HTML headings, with line ranges
        assertThat(result).contains("  - Introduction (lines ");
        assertThat(result).contains("  - Types (lines ");
        assertThat(result).contains("    - Color (lines ");
        assertThat(result).doesNotContain("Node.html");
    }

    @Test
    void titlePrefersH1OverNoisyTitleTag() {
        var guide = JavadocIndexGenerator.analyzeGuide("a/doc-files/guide.html", java.util.List.of(
                "<title>Introduction to FXML | JavaFX 21.0.5</title>",
                "<h1>Introduction to FXML</h1>"));
        assertThat(guide.title()).isEqualTo("Introduction to FXML");

        var titleOnly = JavadocIndexGenerator.analyzeGuide("a/doc-files/t.html",
                java.util.List.of("<title>Title Only</title>"));
        assertThat(titleOnly.title()).isEqualTo("Title Only");

        var bare = JavadocIndexGenerator.analyzeGuide("a/doc-files/bare.html",
                java.util.List.of("<p>no structure at all</p>"));
        assertThat(bare.title()).isEqualTo("bare.html");
    }

    @Test
    void decodesHtmlEntitiesInHeadings() {
        var guide = JavadocIndexGenerator.analyzeGuide("a/doc-files/guide.html", java.util.List.of(
                "<h1>Guide</h1>",
                "<h2><a id=\"include\">&lt;fx:include&gt;</a></h2>",
                "<h2>Fish &amp; Chips</h2>"));
        assertThat(guide.chapters()).extracting(c -> c.title())
                .containsExactly("Guide", "<fx:include>", "Fish & Chips");
    }

    @Test
    void collectJavadocWritesIndexAndLinkOnlyWhenGuidesExist(@TempDir Path tempDir) throws IOException {
        Path outputDir = tempDir.resolve("ai-docs");
        DocsCollector.cleanOutputDir(outputDir);

        Path guideJar = createTestJar(tempDir, Map.of(
                "javafx.graphics/javafx/scene/doc-files/cssref.html", CSS_GUIDE
        ));
        var entry = DocEntry.of("org.openjfx", "javafx-graphics", "21.0.5");
        assertThat(DocsCollector.collectJavadoc(outputDir, guideJar, entry, 1))
                .containsExactly("JavaFX CSS Reference Guide");
        assertThat(outputDir.resolve("org.openjfx/javafx-graphics/javadoc-index.md")).exists();
        String link = Files.readString(outputDir.resolve("org.openjfx/javafx-graphics/javadoc.jar.link"));
        assertThat(link.strip()).isEqualTo(guideJar.toAbsolutePath().toString());

        Path emptyJar = createTestJar(tempDir.resolve("sub"), Map.of(
                "com/example/MyClass.html", "<html>generated</html>"
        ));
        var other = DocEntry.of("com.example", "no-guides", "1.0");
        assertThat(DocsCollector.collectJavadoc(outputDir, emptyJar, other, 1)).isEmpty();
        assertThat(outputDir.resolve("com.example/no-guides/javadoc-index.md")).doesNotExist();
        assertThat(outputDir.resolve("com.example/no-guides/javadoc.jar.link")).doesNotExist();
    }

    private Path createTestJar(Path dir, Map<String, String> entries) throws IOException {
        Files.createDirectories(dir);
        Path jarFile = dir.resolve("test-javadoc.jar");
        try (var jos = new JarOutputStream(Files.newOutputStream(jarFile))) {
            for (var e : entries.entrySet()) {
                jos.putNextEntry(new JarEntry(e.getKey()));
                jos.write(e.getValue().getBytes());
                jos.closeEntry();
            }
        }
        return jarFile;
    }
}
