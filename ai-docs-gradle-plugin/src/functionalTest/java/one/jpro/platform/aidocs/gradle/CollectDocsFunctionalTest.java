package one.jpro.platform.aidocs.gradle;

import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;

class CollectDocsFunctionalTest {

    private static final String JPRO_REPOS = """
            repositories {
                mavenCentral()
                maven {
                    url "https://sandec.jfrog.io/artifactory/repo"
                }
            }
            """;

    @TempDir
    Path projectDir;

    @BeforeEach
    void setUp() throws IOException {
        Files.writeString(projectDir.resolve("settings.gradle"), "rootProject.name = 'test-project'\n");
    }

    @Test
    void taskIsRegistered() throws IOException {
        Files.writeString(projectDir.resolve("build.gradle"), """
                plugins {
                    id 'java'
                    id 'one.jpro.platform.ai-docs'
                }
                repositories {
                    mavenCentral()
                }
                """);

        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments("tasks", "--group=documentation")
                .withPluginClasspath()
                .build();

        assertThat(result.getOutput()).contains("collectDocs");
    }

    @Test
    void collectDocsWithNoDependencies() throws IOException {
        Files.writeString(projectDir.resolve("build.gradle"), """
                plugins {
                    id 'java'
                    id 'one.jpro.platform.ai-docs'
                }
                repositories {
                    mavenCentral()
                }
                """);

        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments("collectDocs")
                .withPluginClasspath()
                .build();

        assertThat(result.task(":collectDocs").getOutcome()).isEqualTo(SUCCESS);
        assertThat(result.getOutput()).contains("Collected documentation for 0 libraries");

        Path aiDocs = projectDir.resolve("build/ai-docs");
        assertThat(aiDocs.resolve("index.md")).exists();

        String index = Files.readString(aiDocs.resolve("index.md"));
        assertThat(index).contains("# AI Documentation Index");
        // No library entries
        assertThat(index.lines().filter(l -> l.startsWith("- ")).count()).isZero();

        // SKILL.md should be generated
        Path skillFile = projectDir.resolve(".claude/skills/docs/SKILL.md");
        assertThat(skillFile).exists();
        String skill = Files.readString(skillFile);
        assertThat(skill).contains("build/ai-docs/");
        assertThat(skill).contains("index.md");
        assertThat(skill).contains("overview.md");
        assertThat(skill).contains("DOCUMENTATION.md");
        assertThat(result.getOutput()).contains("Generated AI skill at .claude/skills/docs/SKILL.md");
    }

    @Test
    void collectDocsFromJproRoutingCore() throws IOException {
        Files.writeString(projectDir.resolve("build.gradle"), """
                plugins {
                    id 'java'
                    id 'one.jpro.platform.ai-docs'
                }
                """ + JPRO_REPOS + """
                dependencies {
                    implementation 'one.jpro.platform:jpro-routing-core:0.5.8'
                }
                """);

        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments("collectDocs")
                .withPluginClasspath()
                .build();

        assertThat(result.task(":collectDocs").getOutcome()).isEqualTo(SUCCESS);

        Path aiDocs = projectDir.resolve("build/ai-docs");

        // index.md should list the library
        String index = Files.readString(aiDocs.resolve("index.md"));
        assertThat(index).contains("one.jpro.platform:jpro-routing-core");
        assertThat(index).contains("[overview]");

        // DOCUMENTATION.md should be collected with real content
        Path docFile = aiDocs.resolve("one.jpro.platform/jpro-routing-core/DOCUMENTATION.md");
        assertThat(docFile).exists();
        String doc = Files.readString(docFile);
        assertThat(doc.length()).isGreaterThan(100);
        // Should contain actual routing documentation
        assertThat(doc).containsIgnoringCase("routing");

        // overview.md should have chapters parsed from the documentation
        Path overviewFile = aiDocs.resolve("one.jpro.platform/jpro-routing-core/overview.md");
        assertThat(overviewFile).exists();
        String overview = Files.readString(overviewFile);
        assertThat(overview).contains("jpro-routing-core");
        assertThat(overview).contains("## Chapters");
        assertThat(overview).contains("lines");
    }

    @Test
    void collectDocsFromMultipleJproDependencies() throws IOException {
        Files.writeString(projectDir.resolve("build.gradle"), """
                plugins {
                    id 'java'
                    id 'one.jpro.platform.ai-docs'
                }
                """ + JPRO_REPOS + """
                dependencies {
                    implementation 'one.jpro.platform:jpro-routing-core:0.5.8'
                    implementation 'one.jpro:jpro-webapi:2025.3.1'
                }
                """);

        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments("collectDocs")
                .withPluginClasspath()
                .build();

        assertThat(result.task(":collectDocs").getOutcome()).isEqualTo(SUCCESS);

        Path aiDocs = projectDir.resolve("build/ai-docs");

        // Both libraries should appear in the index
        String index = Files.readString(aiDocs.resolve("index.md"));
        assertThat(index).contains("jpro-routing-core");
        assertThat(index).contains("jpro-webapi");

        // Both should have documentation files
        assertThat(aiDocs.resolve("one.jpro.platform/jpro-routing-core/DOCUMENTATION.md")).exists();
        assertThat(aiDocs.resolve("one.jpro/jpro-webapi/DOCUMENTATION.md")).exists();

        // Both should have overview files
        assertThat(aiDocs.resolve("one.jpro.platform/jpro-routing-core/overview.md")).exists();
        assertThat(aiDocs.resolve("one.jpro/jpro-webapi/overview.md")).exists();

        // Both should contain real content
        String routingDoc = Files.readString(aiDocs.resolve("one.jpro.platform/jpro-routing-core/DOCUMENTATION.md"));
        String webapiDoc = Files.readString(aiDocs.resolve("one.jpro/jpro-webapi/DOCUMENTATION.md"));
        assertThat(routingDoc.length()).isGreaterThan(100);
        assertThat(webapiDoc.length()).isGreaterThan(100);
    }

    @Test
    void collectDocsSkipsDependenciesWithoutDocumentation() throws IOException {
        Files.writeString(projectDir.resolve("build.gradle"), """
                plugins {
                    id 'java'
                    id 'one.jpro.platform.ai-docs'
                }
                """ + JPRO_REPOS + """
                dependencies {
                    implementation 'org.slf4j:slf4j-api:2.0.17'
                    implementation 'one.jpro.platform:jpro-routing-core:0.5.8'
                }
                """);

        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments("collectDocs")
                .withPluginClasspath()
                .build();

        assertThat(result.task(":collectDocs").getOutcome()).isEqualTo(SUCCESS);

        String index = Files.readString(projectDir.resolve("build/ai-docs/index.md"));
        // slf4j should not appear (no DOCUMENTATION.md)
        assertThat(index).doesNotContain("slf4j");
        // jpro-routing-core should still be there
        assertThat(index).contains("jpro-routing-core");
    }

    @Test
    void collectDocsIsIdempotent() throws IOException {
        Files.writeString(projectDir.resolve("build.gradle"), """
                plugins {
                    id 'java'
                    id 'one.jpro.platform.ai-docs'
                }
                """ + JPRO_REPOS + """
                dependencies {
                    implementation 'one.jpro.platform:jpro-routing-core:0.5.8'
                }
                """);

        GradleRunner runner = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments("collectDocs")
                .withPluginClasspath();

        // Run twice — both should succeed, second run should produce same output
        runner.build();
        BuildResult second = runner.build();
        assertThat(second.task(":collectDocs")).isNotNull();

        // Output should still be valid after second run
        String index = Files.readString(projectDir.resolve("build/ai-docs/index.md"));
        assertThat(index).contains("jpro-routing-core");
    }
}
