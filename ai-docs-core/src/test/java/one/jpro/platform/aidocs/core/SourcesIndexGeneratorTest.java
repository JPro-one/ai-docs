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

class SourcesIndexGeneratorTest {

    @Test
    void generateFromJar(@TempDir Path tempDir) throws IOException {
        Path jar = createTestJar(tempDir, Map.of(
                "com/example/mylib/MyClass.java", "public class MyClass {}",
                "com/example/mylib/MyService.java", "public class MyService {}",
                "com/example/mylib/model/MyModel.java", "public class MyModel {}"
        ));

        var entry = DocEntry.of("com.example", "my-lib", "1.0.0");
        String result = SourcesIndexGenerator.generate(jar, entry);

        assertThat(result).contains("# my-lib (1.0.0) — Source Index");
        // The jar stays in the local artifact cache; its path is stored in sources.jar.link,
        // so the index itself is machine-independent
        assertThat(result).contains("sources.jar.link");
        assertThat(result).doesNotContain(jar.toAbsolutePath().toString());
        // Header shows copyable commands using the link file, with a real-file example
        assertThat(result).contains("e.g. `unzip -p \"$(cat sources.jar.link)\" com/example/mylib/MyClass.java`");
        assertThat(result).contains("`unzip -q \"$(cat sources.jar.link)\" -d sources`");
        assertThat(result).contains("## Packages");
        // Package headers are directory paths so unzip paths can be assembled directly
        assertThat(result).contains("- com/example/mylib/ (2 files)");
        assertThat(result).contains("  - MyClass.java (1 line)");
        assertThat(result).contains("  - MyService.java (1 line)");
        assertThat(result).contains("- com/example/mylib/model/ (1 file)");
        assertThat(result).contains("  - MyModel.java (1 line)");
    }

    @Test
    void generateWritesToFile(@TempDir Path tempDir) throws IOException {
        Path jar = createTestJar(tempDir, Map.of(
                "com/example/Foo.java", "public class Foo {}"
        ));

        var entry = DocEntry.of("com.example", "foo-lib", "2.0.0");
        Path indexFile = tempDir.resolve("sources-index.md");
        SourcesIndexGenerator.generate(indexFile, jar, entry);

        assertThat(indexFile).exists();
        String content = Files.readString(indexFile);
        assertThat(content).contains("foo-lib (2.0.0)");
        assertThat(content).contains("com/example/ (1 file)");
        assertThat(content).contains("  - Foo.java (1 line)");
    }

    @Test
    void emptyJarProducesEmptyPackages(@TempDir Path tempDir) throws IOException {
        Path jar = createTestJar(tempDir, Map.of());

        var entry = DocEntry.of("com.example", "empty-lib", "1.0.0");
        String result = SourcesIndexGenerator.generate(jar, entry);

        assertThat(result).contains("## Packages");
        // No package entries
        assertThat(result.lines().filter(l -> l.startsWith("- ")).count()).isZero();
    }

    @Test
    void nonSourceFilesAreIgnored(@TempDir Path tempDir) throws IOException {
        Path jar = createTestJar(tempDir, Map.of(
                "com/example/MyClass.java", "public class MyClass {}",
                "com/example/README.md", "# Readme",
                "META-INF/MANIFEST.MF", "Manifest-Version: 1.0"
        ));

        var entry = DocEntry.of("com.example", "mixed-lib", "1.0.0");
        String result = SourcesIndexGenerator.generate(jar, entry);

        assertThat(result).contains("MyClass.java (1 line)");
        assertThat(result).doesNotContain("README.md");
        assertThat(result).doesNotContain("MANIFEST.MF");
    }

    @Test
    void scalaKotlinGroovySourcesAreIncluded(@TempDir Path tempDir) throws IOException {
        Path jar = createTestJar(tempDir, Map.of(
                "scala/collection/List.scala", "class List {}",
                "com/example/App.kt", "class App",
                "com/example/Script.groovy", "class Script {}",
                "com/example/MyClass.java", "public class MyClass {}"
        ));

        var entry = DocEntry.of("com.example", "poly-lib", "1.0.0");
        String result = SourcesIndexGenerator.generate(jar, entry);

        assertThat(result).contains("List.scala (1 line)");
        assertThat(result).contains("App.kt (1 line)");
        assertThat(result).contains("Script.groovy (1 line)");
        assertThat(result).contains("MyClass.java (1 line)");
    }

    @Test
    void defaultPackageFilesGrouped(@TempDir Path tempDir) throws IOException {
        Path jar = createTestJar(tempDir, Map.of(
                "Main.java", "public class Main {}"
        ));

        var entry = DocEntry.of("com.example", "nopackage", "1.0.0");
        String result = SourcesIndexGenerator.generate(jar, entry);

        assertThat(result).contains("(root) (1 file)");
        assertThat(result).contains("  - Main.java (1 line)");
        assertThat(result).contains("\" Main.java`");
    }

    @Test
    void listSourceFiles(@TempDir Path tempDir) throws IOException {
        Path jar = createTestJar(tempDir, Map.of(
                "com/example/A.java", "class A {}",
                "com/example/B.java", "class B {}",
                "com/other/C.java", "class C {}"
        ));

        var result = SourcesIndexGenerator.listSourceFiles(jar);

        assertThat(result).containsKeys("com/example/", "com/other/");
        assertThat(result.get("com/example/")).extracting(SourcesIndexGenerator.FileEntry::name)
                .containsExactly("A.java", "B.java");
        assertThat(result.get("com/other/")).extracting(SourcesIndexGenerator.FileEntry::name)
                .containsExactly("C.java");
        // Verify line counts
        assertThat(result.get("com/example/")).extracting(SourcesIndexGenerator.FileEntry::lineCount)
                .containsExactly(1, 1);
    }

    @Test
    void lineCountsReflectActualContent(@TempDir Path tempDir) throws IOException {
        Path jar = createTestJar(tempDir, Map.of(
                "com/example/Small.java", "class Small {}",
                "com/example/Large.java", "package com.example;\n\npublic class Large {\n    int x;\n    int y;\n}\n"
        ));

        var entry = DocEntry.of("com.example", "counted-lib", "1.0.0");
        String result = SourcesIndexGenerator.generate(jar, entry);

        assertThat(result).contains("  - Large.java (6 lines)");
        assertThat(result).contains("  - Small.java (1 line)");
    }

    private Path createTestJar(Path dir, Map<String, String> entries) throws IOException {
        Path jarFile = dir.resolve("test-sources.jar");
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
