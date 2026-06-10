package one.jpro.platform.aidocs.gradle;

import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Compares the generated output for pinned release dependencies against a committed
 * baseline, catching unintended changes to the generators.
 *
 * To regenerate the baseline after an intended format change, run:
 * {@code ./gradlew :ai-docs-gradle-plugin:functionalTest --tests '*BaselineRegressionTest*' -DupdateBaseline=true}
 */
class BaselineRegressionTest {

    /** Only generated files are baselined — DOCUMENTATION.md/CHANGELOG.md are verbatim upstream copies. */
    private static final List<String> GENERATED_FILES =
            List.of("index.md", "context.md", "overview.md", "sources-index.md", "changelog-overview.md");

    private static final Path BASELINE_DIR = Path.of("src/functionalTest/resources/baseline");

    @TempDir
    Path projectDir;

    @Test
    void outputMatchesBaseline() throws IOException {
        Files.writeString(projectDir.resolve("settings.gradle"), "rootProject.name = 'baseline-project'\n");
        Files.writeString(projectDir.resolve("build.gradle"), """
                plugins {
                    id 'java'
                    id 'one.jpro.aidocs'
                }
                repositories {
                    mavenCentral()
                    maven {
                        url = "https://sandec.jfrog.io/artifactory/repo"
                    }
                }
                dependencies {
                    implementation 'one.jpro.platform:jpro-routing-core:0.5.8'
                    implementation 'org.slf4j:slf4j-api:2.0.17'
                }
                """);

        GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments("collectDocs")
                .withPluginClasspath()
                .build();

        Path aiDocs = projectDir.resolve("build/ai-docs");
        TreeMap<String, String> generated = collectGeneratedFiles(aiDocs);
        assertThat(generated).isNotEmpty();

        if (Boolean.getBoolean("updateBaseline")) {
            updateBaseline(generated);
            return;
        }

        assertThat(BASELINE_DIR)
                .as("Baseline missing — regenerate with -DupdateBaseline=true")
                .exists();
        TreeMap<String, String> baseline = collectGeneratedFiles(BASELINE_DIR);

        assertThat(generated.keySet())
                .as("Generated file set should match baseline (regenerate with -DupdateBaseline=true if intended)")
                .isEqualTo(baseline.keySet());
        for (var entry : baseline.entrySet()) {
            assertThat(generated.get(entry.getKey()))
                    .as("Content of %s should match baseline (regenerate with -DupdateBaseline=true if intended)",
                            entry.getKey())
                    .isEqualTo(entry.getValue());
        }
    }

    private TreeMap<String, String> collectGeneratedFiles(Path root) throws IOException {
        var result = new TreeMap<String, String>();
        try (Stream<Path> files = Files.walk(root)) {
            for (Path file : files.filter(Files::isRegularFile).toList()) {
                if (GENERATED_FILES.contains(file.getFileName().toString())) {
                    result.put(root.relativize(file).toString(), Files.readString(file));
                }
            }
        }
        return result;
    }

    private void updateBaseline(TreeMap<String, String> generated) throws IOException {
        if (Files.exists(BASELINE_DIR)) {
            try (Stream<Path> files = Files.walk(BASELINE_DIR).sorted((a, b) -> b.compareTo(a))) {
                for (Path p : files.toList()) Files.delete(p);
            }
        }
        for (var entry : generated.entrySet()) {
            Path target = BASELINE_DIR.resolve(entry.getKey());
            Files.createDirectories(target.getParent());
            Files.writeString(target, entry.getValue());
        }
    }
}
