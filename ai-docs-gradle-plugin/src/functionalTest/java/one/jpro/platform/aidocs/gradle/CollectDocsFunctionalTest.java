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
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE;

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
                    id 'one.jpro.aidocs'
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
                    id 'one.jpro.aidocs'
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
        assertThat(aiDocs.resolve("context.md")).exists();

        String index = Files.readString(aiDocs.resolve("index.md"));
        assertThat(index).contains("# AI Documentation Index");
        // No library entries
        assertThat(index.lines().filter(l -> l.startsWith("- ")).count()).isZero();

        String context = Files.readString(aiDocs.resolve("context.md"));
        assertThat(context).contains("# Project Documentation Context");

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
                    id 'one.jpro.aidocs'
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

        // index.md should list the library exactly once with line range into context.md
        String index = Files.readString(aiDocs.resolve("index.md"));
        assertThat(index).contains("one.jpro.platform:jpro-routing-core");
        assertThat(index).contains("lines");
        long indexCount = index.lines().filter(l -> l.contains("jpro-routing-core")).count();
        assertThat(indexCount).isEqualTo(1);

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
        // Should have summaries (at least one line with " — ")
        assertThat(overview).contains(" — ");

        // context.md should exist and contain the library
        Path contextFile = aiDocs.resolve("context.md");
        assertThat(contextFile).exists();
        String context = Files.readString(contextFile);
        assertThat(context).contains("one.jpro.platform:jpro-routing-core");
        assertThat(context).contains("Chapters");

        // sources-index.md should be generated, referencing the jar in the artifact cache
        Path sourcesIndex = aiDocs.resolve("one.jpro.platform/jpro-routing-core/sources-index.md");
        assertThat(sourcesIndex).exists();
        String srcIndex = Files.readString(sourcesIndex);
        assertThat(srcIndex).contains("Source Index");
        assertThat(srcIndex).contains(".java");
        // The jar is referenced via sources.jar.link, not copied — and the path must exist
        assertThat(aiDocs.resolve("one.jpro.platform/jpro-routing-core/sources.jar")).doesNotExist();
        String link = Files.readString(aiDocs.resolve("one.jpro.platform/jpro-routing-core/sources.jar.link"));
        assertThat(Path.of(link.strip())).exists();
        assertThat(srcIndex).contains("sources.jar.link");

        // CHANGELOG.md — graceful absence: if the artifact doesn't publish one, no file should exist
        Path changelogFile = aiDocs.resolve("one.jpro.platform/jpro-routing-core/CHANGELOG.md");
        Path changelogOverview = aiDocs.resolve("one.jpro.platform/jpro-routing-core/changelog-overview.md");
        if (changelogFile.toFile().exists()) {
            // If a changelog is present, the overview should also exist
            assertThat(changelogOverview).exists();
            String changelogContent = Files.readString(changelogOverview);
            assertThat(changelogContent).contains("Chapters");
            // context.md should reference the changelog
            assertThat(context).contains("changelog-overview.md");
        } else {
            // No changelog artifact — overview should not exist either
            assertThat(changelogOverview).doesNotExist();
        }

    }

    @Test
    void collectDocsFromMultipleJproDependencies() throws IOException {
        Files.writeString(projectDir.resolve("build.gradle"), """
                plugins {
                    id 'java'
                    id 'one.jpro.aidocs'
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
                    id 'one.jpro.aidocs'
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

        Path aiDocs = projectDir.resolve("build/ai-docs");
        String index = Files.readString(aiDocs.resolve("index.md"));
        // jpro-routing-core should be there
        assertThat(index).contains("jpro-routing-core");

        // slf4j has sources but no DOCUMENTATION.md — it should still appear in the index with sources
        assertThat(index).contains("slf4j");
        Path slf4jSourcesIndex = aiDocs.resolve("org.slf4j/slf4j-api/sources-index.md");
        assertThat(slf4jSourcesIndex).exists();

        // slf4j POM has name/description/url/license — verify POM metadata is parsed
        // The SLF4J POM contains a human-readable name
        assertThat(index).contains("SLF4J");
    }

    @Test
    void buildscriptDependenciesSkippedByDefault() throws IOException {
        Files.writeString(projectDir.resolve("build.gradle"), """
                buildscript {
                """ + JPRO_REPOS + """
                    dependencies {
                        classpath 'one.jpro.platform:jpro-routing-core:0.5.8'
                    }
                }
                plugins {
                    id 'java'
                    id 'one.jpro.aidocs'
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

        // Buildscript-only dependencies are not documented by default
        String index = Files.readString(projectDir.resolve("build/ai-docs/index.md"));
        assertThat(index).doesNotContain("jpro-routing-core");
    }

    @Test
    void collectDocsFromBuildscriptDependencyWhenEnabled() throws IOException {
        Files.writeString(projectDir.resolve("build.gradle"), """
                buildscript {
                """ + JPRO_REPOS + """
                    dependencies {
                        classpath 'one.jpro.platform:jpro-routing-core:0.5.8'
                    }
                }
                plugins {
                    id 'java'
                    id 'one.jpro.aidocs'
                }
                repositories {
                    mavenCentral()
                }
                aiDocs {
                    includeBuildscript = true
                }
                """);

        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments("collectDocs")
                .withPluginClasspath()
                .build();

        assertThat(result.task(":collectDocs").getOutcome()).isEqualTo(SUCCESS);

        Path aiDocs = projectDir.resolve("build/ai-docs");

        // jpro-routing-core should appear even though it's only in the buildscript classpath
        String index = Files.readString(aiDocs.resolve("index.md"));
        assertThat(index).contains("jpro-routing-core");

        // DOCUMENTATION.md should be collected
        Path docFile = aiDocs.resolve("one.jpro.platform/jpro-routing-core/DOCUMENTATION.md");
        assertThat(docFile).exists();
        String doc = Files.readString(docFile);
        assertThat(doc.length()).isGreaterThan(100);
        assertThat(doc).containsIgnoringCase("routing");
    }

    @Test
    void collectDocsFromSubprojectDependencies() throws IOException {
        // Root project applies the plugin but has no dependencies of its own
        Files.writeString(projectDir.resolve("build.gradle"), """
                plugins {
                    id 'one.jpro.aidocs'
                }
                repositories {
                    mavenCentral()
                }
                """);

        // Settings: include a subproject
        Files.writeString(projectDir.resolve("settings.gradle"), """
                rootProject.name = 'test-multiproject'
                include 'sub'
                """);

        // Subproject has jpro-routing-core as a dependency
        Path subDir = projectDir.resolve("sub");
        Files.createDirectories(subDir);
        Files.writeString(subDir.resolve("build.gradle"), """
                plugins {
                    id 'java'
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

        // jpro-routing-core comes from the subproject — should be collected
        String index = Files.readString(aiDocs.resolve("index.md"));
        assertThat(index).contains("jpro-routing-core");

        Path docFile = aiDocs.resolve("one.jpro.platform/jpro-routing-core/DOCUMENTATION.md");
        assertThat(docFile).exists();
        String doc = Files.readString(docFile);
        assertThat(doc).containsIgnoringCase("routing");
    }

    @Test
    void collectDocsPicksUpDependencyChanges() throws IOException {
        Files.writeString(projectDir.resolve("build.gradle"), """
                plugins {
                    id 'java'
                    id 'one.jpro.aidocs'
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

        runner.build();
        // With the dependency graph tracked as input, an unchanged second run may skip
        BuildResult second = runner.build();
        assertThat(second.task(":collectDocs").getOutcome()).isIn(SUCCESS, UP_TO_DATE);

        // Adding a dependency (one not already in the transitive graph) must
        // re-execute the task and be reflected in the regenerated index
        Files.writeString(projectDir.resolve("build.gradle"), """
                plugins {
                    id 'java'
                    id 'one.jpro.aidocs'
                }
                """ + JPRO_REPOS + """
                dependencies {
                    implementation 'one.jpro.platform:jpro-routing-core:0.5.8'
                    implementation 'com.google.code.gson:gson:2.11.0'
                }
                """);
        BuildResult third = runner.build();
        assertThat(third.task(":collectDocs").getOutcome()).isEqualTo(SUCCESS);

        String index = Files.readString(projectDir.resolve("build/ai-docs/index.md"));
        assertThat(index).contains("jpro-routing-core");
        assertThat(index).contains("gson");
    }

    @Test
    void collectsJavadocGuides() throws IOException {
        Files.writeString(projectDir.resolve("build.gradle"), """
                plugins {
                    id 'java'
                    id 'one.jpro.aidocs'
                }
                repositories {
                    mavenCentral()
                }
                dependencies {
                    implementation 'org.openjfx:javafx-graphics:21.0.5'
                }
                """);

        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments("collectDocs")
                .withPluginClasspath()
                .build();

        assertThat(result.task(":collectDocs").getOutcome()).isEqualTo(SUCCESS);

        Path aiDocs = projectDir.resolve("build/ai-docs");

        // The JavaFX CSS reference guide ships as doc-files HTML in the javadoc jar
        Path guideIndex = aiDocs.resolve("org.openjfx/javafx-graphics/javadoc-index.md");
        assertThat(guideIndex).exists();
        String guides = Files.readString(guideIndex);
        assertThat(guides).contains("cssref.html");
        assertThat(guides).containsIgnoringCase("CSS Reference Guide");
        // Chapter overview parsed from the guide's HTML headings
        assertThat(guides).contains("(lines ");

        String linkContent = Files.readString(aiDocs.resolve("org.openjfx/javafx-graphics/javadoc.jar.link"));
        assertThat(Path.of(linkContent.strip())).exists();

        // The guides are highlighted in context.md with their titles
        String context = Files.readString(aiDocs.resolve("context.md"));
        assertThat(context).contains("Guides (");
        assertThat(context).containsIgnoringCase("CSS Reference Guide");
    }

    @Test
    void collectsJavadocGuidesWithJavafxPlugin() throws IOException {
        // The javafx plugin makes JavaFX resolve via Gradle Module Metadata variants —
        // classifier artifacts must still be resolvable (variant matching must be bypassed)
        Files.writeString(projectDir.resolve("settings.gradle"), """
                pluginManagement { repositories { gradlePluginPortal() } }
                rootProject.name = 'test-project'
                """);
        Files.writeString(projectDir.resolve("build.gradle"), """
                plugins {
                    id 'java'
                    id 'org.openjfx.javafxplugin' version '0.1.0'
                    id 'one.jpro.aidocs'
                }
                javafx {
                    version = '21.0.5'
                    modules = ['javafx.controls']
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

        Path aiDocs = projectDir.resolve("build/ai-docs");
        String index = Files.readString(aiDocs.resolve("index.md"));
        assertThat(index).contains("org.openjfx:javafx-graphics");
        String guides = Files.readString(aiDocs.resolve("org.openjfx/javafx-graphics/javadoc-index.md"));
        assertThat(guides).contains("cssref.html");
        assertThat(aiDocs.resolve("org.openjfx/javafx-graphics/sources-index.md")).exists();
    }

    @Test
    void warnsWhenMavenLocalShadowsDocumentation() throws IOException {
        // Simulate the mavenLocal trap: ~/.m2 contains a partial copy (jar+pom, as cached
        // by any Maven run) of a library that publishes docs remotely. Gradle then serves
        // the metadata from mavenLocal and never finds the DOCUMENTATION/sources artifacts.
        Path fakeM2 = projectDir.resolve("fake-m2");
        Path moduleDir = fakeM2.resolve("one/jpro/platform/jpro-routing-core/0.5.8");
        Files.createDirectories(moduleDir);
        Files.writeString(moduleDir.resolve("jpro-routing-core-0.5.8.pom"), """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>one.jpro.platform</groupId>
                    <artifactId>jpro-routing-core</artifactId>
                    <version>0.5.8</version>
                </project>
                """);
        Files.write(moduleDir.resolve("jpro-routing-core-0.5.8.jar"), new byte[]{0x50, 0x4b, 0x05, 0x06,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});

        Files.writeString(projectDir.resolve("build.gradle"), """
                plugins {
                    id 'java'
                    id 'one.jpro.aidocs'
                }
                repositories {
                    mavenLocal()
                    mavenCentral()
                    maven { url = "https://sandec.jfrog.io/artifactory/repo" }
                }
                dependencies {
                    implementation 'one.jpro.platform:jpro-routing-core:0.5.8'
                }
                """);

        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments("collectDocs", "-Dmaven.repo.local=" + fakeM2.toAbsolutePath())
                .withPluginClasspath()
                .build();

        assertThat(result.task(":collectDocs").getOutcome()).isEqualTo(SUCCESS);

        // The trap silently hides the docs...
        String index = Files.readString(projectDir.resolve("build/ai-docs/index.md"));
        assertThat(index).doesNotContain("jpro-routing-core");
        // ...but the plugin must warn about it
        assertThat(result.getOutput()).contains("mavenLocal");
        assertThat(result.getOutput()).contains("one.jpro.platform:jpro-routing-core:0.5.8");
    }

    @Test
    void worksWithConfigurationCache() throws IOException {
        Files.writeString(projectDir.resolve("build.gradle"), """
                plugins {
                    id 'java'
                    id 'one.jpro.aidocs'
                }
                """ + JPRO_REPOS + """
                dependencies {
                    implementation 'one.jpro.platform:jpro-routing-core:0.5.8'
                }
                """);

        GradleRunner runner = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments("collectDocs", "--configuration-cache")
                .withPluginClasspath();

        // First run: stores the configuration cache; any CC problem fails the build
        BuildResult first = runner.build();
        assertThat(first.task(":collectDocs").getOutcome()).isEqualTo(SUCCESS);

        String index = Files.readString(projectDir.resolve("build/ai-docs/index.md"));
        assertThat(index).contains("jpro-routing-core");

        // Second run: must reuse the cache entry without problems
        BuildResult second = runner.build();
        assertThat(second.getOutput()).contains("Reusing configuration cache");

        // A dependency change must invalidate the cache and refresh the docs
        Files.writeString(projectDir.resolve("build.gradle"), """
                plugins {
                    id 'java'
                    id 'one.jpro.aidocs'
                }
                """ + JPRO_REPOS + """
                dependencies {
                    implementation 'one.jpro.platform:jpro-routing-core:0.5.8'
                    implementation 'com.google.code.gson:gson:2.11.0'
                }
                """);
        runner.build();
        String updated = Files.readString(projectDir.resolve("build/ai-docs/index.md"));
        assertThat(updated).contains("gson");
    }
}
