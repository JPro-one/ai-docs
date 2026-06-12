package one.jpro.platform.aidocs.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PomParserTest {

    @Test
    void parseFullPom(@TempDir Path tempDir) throws IOException {
        Path pom = tempDir.resolve("pom.xml");
        Files.writeString(pom, """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                    <groupId>org.example</groupId>
                    <artifactId>my-lib</artifactId>
                    <version>1.0.0</version>
                    <name>My Library</name>
                    <description>A great utility library for Java projects.</description>
                    <url>https://example.com/my-lib</url>
                    <scm>
                        <url>https://github.com/example/my-lib</url>
                        <connection>scm:git:git://github.com/example/my-lib.git</connection>
                    </scm>
                    <licenses>
                        <license>
                            <name>Apache-2.0</name>
                            <url>https://www.apache.org/licenses/LICENSE-2.0</url>
                        </license>
                    </licenses>
                </project>
                """);

        PomMetadata metadata = PomParser.parse(pom);

        assertThat(metadata.name()).isEqualTo("My Library");
        assertThat(metadata.description()).isEqualTo("A great utility library for Java projects.");
        assertThat(metadata.url()).isEqualTo("https://example.com/my-lib");
        assertThat(metadata.scmUrl()).isEqualTo("https://github.com/example/my-lib");
        assertThat(metadata.license()).isEqualTo("Apache-2.0");
    }

    @Test
    void parsePomWithMissingFields(@TempDir Path tempDir) throws IOException {
        Path pom = tempDir.resolve("pom.xml");
        Files.writeString(pom, """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                    <groupId>org.example</groupId>
                    <artifactId>minimal-lib</artifactId>
                    <version>1.0.0</version>
                </project>
                """);

        PomMetadata metadata = PomParser.parse(pom);

        assertThat(metadata.name()).isNull();
        assertThat(metadata.description()).isNull();
        assertThat(metadata.url()).isNull();
        assertThat(metadata.scmUrl()).isNull();
        assertThat(metadata.license()).isNull();
    }

    @Test
    void parsePomWithPartialFields(@TempDir Path tempDir) throws IOException {
        Path pom = tempDir.resolve("pom.xml");
        Files.writeString(pom, """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                    <groupId>org.example</groupId>
                    <artifactId>partial-lib</artifactId>
                    <version>1.0.0</version>
                    <name>Partial Library</name>
                    <url>https://example.com</url>
                </project>
                """);

        PomMetadata metadata = PomParser.parse(pom);

        assertThat(metadata.name()).isEqualTo("Partial Library");
        assertThat(metadata.description()).isNull();
        assertThat(metadata.url()).isEqualTo("https://example.com");
        assertThat(metadata.license()).isNull();
    }

    @Test
    void parseMalformedXmlReturnsEmptyMetadata(@TempDir Path tempDir) throws IOException {
        Path pom = tempDir.resolve("pom.xml");
        Files.writeString(pom, "this is not XML at all");

        PomMetadata metadata = PomParser.parse(pom);

        assertThat(metadata.name()).isNull();
        assertThat(metadata.description()).isNull();
        assertThat(metadata.url()).isNull();
        assertThat(metadata.license()).isNull();
    }

    @Test
    void parseNonExistentFileReturnsEmptyMetadata(@TempDir Path tempDir) {
        Path pom = tempDir.resolve("nonexistent.xml");

        PomMetadata metadata = PomParser.parse(pom);

        assertThat(metadata.name()).isNull();
        assertThat(metadata.description()).isNull();
    }

    @Test
    void parseMultipleLicensesTakesFirst(@TempDir Path tempDir) throws IOException {
        Path pom = tempDir.resolve("pom.xml");
        Files.writeString(pom, """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                    <groupId>org.example</groupId>
                    <artifactId>dual-license</artifactId>
                    <version>1.0.0</version>
                    <licenses>
                        <license>
                            <name>MIT</name>
                        </license>
                        <license>
                            <name>Apache-2.0</name>
                        </license>
                    </licenses>
                </project>
                """);

        PomMetadata metadata = PomParser.parse(pom);

        assertThat(metadata.license()).isEqualTo("MIT");
    }

    @Test
    void parseParentFromPom(@TempDir Path tempDir) throws IOException {
        Path pom = tempDir.resolve("pom.xml");
        Files.writeString(pom, """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                    <parent>
                        <groupId>org.example</groupId>
                        <artifactId>parent-lib</artifactId>
                        <version>2.0.0</version>
                    </parent>
                    <artifactId>child-lib</artifactId>
                </project>
                """);

        String[] parent = PomParser.parseParent(pom);

        assertThat(parent).isNotNull();
        assertThat(parent[0]).isEqualTo("org.example");
        assertThat(parent[1]).isEqualTo("parent-lib");
        assertThat(parent[2]).isEqualTo("2.0.0");
    }

    @Test
    void parseParentReturnsNullWhenNoParent(@TempDir Path tempDir) throws IOException {
        Path pom = tempDir.resolve("pom.xml");
        Files.writeString(pom, """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                    <groupId>org.example</groupId>
                    <artifactId>root-lib</artifactId>
                    <version>1.0.0</version>
                </project>
                """);

        String[] parent = PomParser.parseParent(pom);

        assertThat(parent).isNull();
    }

    @Test
    void parseParentReturnsNullWhenPartialParent(@TempDir Path tempDir) throws IOException {
        Path pom = tempDir.resolve("pom.xml");
        Files.writeString(pom, """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                    <parent>
                        <groupId>org.example</groupId>
                        <artifactId>parent-lib</artifactId>
                    </parent>
                    <artifactId>child-lib</artifactId>
                </project>
                """);

        String[] parent = PomParser.parseParent(pom);

        assertThat(parent).isNull();
    }

    @Test
    void parseEmptyElementsReturnNull(@TempDir Path tempDir) throws IOException {
        Path pom = tempDir.resolve("pom.xml");
        Files.writeString(pom, """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                    <groupId>org.example</groupId>
                    <artifactId>empty-fields</artifactId>
                    <version>1.0.0</version>
                    <name></name>
                    <description>  </description>
                </project>
                """);

        PomMetadata metadata = PomParser.parse(pom);

        assertThat(metadata.name()).isNull();
        assertThat(metadata.description()).isNull();
    }

    @Test
    void multiLineDescriptionIsFlattened(@TempDir Path tempDir) throws IOException {
        Path pom = tempDir.resolve("pom.xml");
        Files.writeString(pom, """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                    <groupId>net.bytebuddy</groupId>
                    <artifactId>byte-buddy</artifactId>
                    <version>1.0</version>
                    <description>Byte Buddy is a library.
                        This artifact is repackaged.</description>
                </project>
                """);

        PomMetadata metadata = PomParser.parse(pom);

        assertThat(metadata.description()).isEqualTo("Byte Buddy is a library. This artifact is repackaged.");
    }

    @Test
    void withFallbackFillsMissingFieldsButNotName() {
        var own = new PomMetadata("Gson", null, null, "https://github.com/google/gson/scm", null);
        var parent = new PomMetadata("Gson Parent", "Gson JSON library", "https://github.com/google/gson",
                "https://parent/scm", "Apache-2.0");

        PomMetadata merged = own.withFallback(parent);

        assertThat(merged.name()).isEqualTo("Gson");
        assertThat(merged.description()).isEqualTo("Gson JSON library");
        assertThat(merged.url()).isEqualTo("https://github.com/google/gson");
        assertThat(merged.scmUrl()).isEqualTo("https://github.com/google/gson/scm");
        assertThat(merged.license()).isEqualTo("Apache-2.0");
    }
}
