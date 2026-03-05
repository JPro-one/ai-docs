package one.jpro.platform.aidocs.maven;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs both Gradle and Maven on the same dependency and verifies
 * they produce equivalent output.
 */
class CrossPluginComparisonTest {

    private static final String PLUGIN_VERSION = "0.1.0-SNAPSHOT";

    @TempDir
    Path tempDir;

    @BeforeAll
    static void checkToolsAvailable() {
        Assumptions.assumeTrue(isAvailable("mvn", "--version"), "mvn not found on PATH");
        Assumptions.assumeTrue(isAvailable("gradle", "--version"), "gradle not found on PATH");
    }

    @Test
    void gradleAndMavenProduceEquivalentOutput() throws Exception {
        Path gradleDir = tempDir.resolve("gradle-project");
        Path mavenDir = tempDir.resolve("maven-project");
        Files.createDirectories(gradleDir);
        Files.createDirectories(mavenDir);

        // --- Gradle project ---
        Files.writeString(gradleDir.resolve("settings.gradle"), "rootProject.name = 'test-project'\n");
        Files.writeString(gradleDir.resolve("build.gradle"), """
                buildscript {
                    repositories {
                        mavenLocal()
                        mavenCentral()
                        gradlePluginPortal()
                        maven { url "https://sandec.jfrog.io/artifactory/repo" }
                    }
                    dependencies {
                        classpath 'one.jpro.platform:ai-docs-gradle-plugin:%s'
                    }
                }
                apply plugin: 'java'
                apply plugin: 'one.jpro.platform.ai-docs'
                repositories {
                    mavenCentral()
                    maven { url "https://sandec.jfrog.io/artifactory/repo" }
                }
                dependencies {
                    implementation 'one.jpro.platform:jpro-routing-core:0.5.8'
                }
                """.formatted(PLUGIN_VERSION));

        RunResult gradleResult = run(gradleDir, "gradle", "collectDocs", "--no-daemon", "--stacktrace");
        assertThat(gradleResult.exitCode())
                .as("Gradle exit code (stdout: %s, stderr: %s)", gradleResult.stdout(), gradleResult.stderr())
                .isEqualTo(0);

        // --- Maven project ---
        Files.createDirectories(mavenDir.resolve("src/main/java"));
        Files.writeString(mavenDir.resolve("pom.xml"), """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>com.example</groupId>
                    <artifactId>test-project</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <repositories>
                        <repository>
                            <id>sandec</id>
                            <url>https://sandec.jfrog.io/artifactory/repo</url>
                        </repository>
                    </repositories>
                    <pluginRepositories>
                        <pluginRepository>
                            <id>local</id>
                            <url>file://${user.home}/.m2/repository</url>
                        </pluginRepository>
                    </pluginRepositories>
                    <dependencies>
                        <dependency>
                            <groupId>one.jpro.platform</groupId>
                            <artifactId>jpro-routing-core</artifactId>
                            <version>0.5.8</version>
                        </dependency>
                    </dependencies>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>one.jpro.platform</groupId>
                                <artifactId>ai-docs-maven-plugin</artifactId>
                                <version>%s</version>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """.formatted(PLUGIN_VERSION));

        RunResult mavenResult = run(mavenDir, "mvn", "ai-docs:collect-docs", "-B");
        assertThat(mavenResult.exitCode())
                .as("Maven exit code (stdout: %s, stderr: %s)", mavenResult.stdout(), mavenResult.stderr())
                .isEqualTo(0);

        // --- Compare outputs ---
        Path gradleOut = gradleDir.resolve("build/ai-docs");
        Path mavenOut = mavenDir.resolve("target/ai-docs");

        // Same set of library directories (exclude ai-docs-gradle-plugin which Gradle
        // picks up from its own buildscript classpath but Maven doesn't have)
        Set<String> gradleDirs = listRelativeDirs(gradleOut).stream()
                .filter(d -> !d.contains("ai-docs-gradle-plugin"))
                .collect(Collectors.toSet());
        Set<String> mavenDirs = listRelativeDirs(mavenOut);
        assertThat(mavenDirs).as("Library directories should match").isEqualTo(gradleDirs);

        // DOCUMENTATION.md files are byte-identical
        Path gradleDoc = gradleOut.resolve("one.jpro.platform/jpro-routing-core/DOCUMENTATION.md");
        Path mavenDoc = mavenOut.resolve("one.jpro.platform/jpro-routing-core/DOCUMENTATION.md");
        assertThat(Files.readAllBytes(mavenDoc))
                .as("DOCUMENTATION.md should be byte-identical")
                .isEqualTo(Files.readAllBytes(gradleDoc));

        // sources.jar files are byte-identical
        Path gradleSources = gradleOut.resolve("one.jpro.platform/jpro-routing-core/sources.jar");
        Path mavenSources = mavenOut.resolve("one.jpro.platform/jpro-routing-core/sources.jar");
        assertThat(Files.readAllBytes(mavenSources))
                .as("sources.jar should be byte-identical")
                .isEqualTo(Files.readAllBytes(gradleSources));

        // overview.md files are identical (same core generator)
        Path gradleOverview = gradleOut.resolve("one.jpro.platform/jpro-routing-core/overview.md");
        Path mavenOverview = mavenOut.resolve("one.jpro.platform/jpro-routing-core/overview.md");
        assertThat(Files.readString(mavenOverview))
                .as("overview.md should be identical")
                .isEqualTo(Files.readString(gradleOverview));

        // sources-index.md files are identical
        Path gradleSrcIndex = gradleOut.resolve("one.jpro.platform/jpro-routing-core/sources-index.md");
        Path mavenSrcIndex = mavenOut.resolve("one.jpro.platform/jpro-routing-core/sources-index.md");
        assertThat(Files.readString(mavenSrcIndex))
                .as("sources-index.md should be identical")
                .isEqualTo(Files.readString(gradleSrcIndex));

        // index.md — both should contain jpro-routing-core
        String gradleIndex = Files.readString(gradleOut.resolve("index.md"));
        String mavenIndex = Files.readString(mavenOut.resolve("index.md"));
        assertThat(mavenIndex).contains("jpro-routing-core");
        assertThat(gradleIndex).contains("jpro-routing-core");

        // context.md — both should contain jpro-routing-core
        String gradleContext = Files.readString(gradleOut.resolve("context.md"));
        String mavenContext = Files.readString(mavenOut.resolve("context.md"));
        assertThat(mavenContext).contains("jpro-routing-core");
        assertThat(gradleContext).contains("jpro-routing-core");
    }

    // --- helpers ---

    /**
     * Lists directories two levels deep under root (group/artifact paths).
     */
    private Set<String> listRelativeDirs(Path root) throws IOException {
        try (Stream<Path> stream = Files.walk(root, 2)) {
            return stream
                    .filter(Files::isDirectory)
                    .filter(p -> p.getNameCount() - root.getNameCount() == 2) // exactly 2 levels deep
                    .map(p -> root.relativize(p).toString())
                    .collect(Collectors.toSet());
        }
    }

    private RunResult run(Path workDir, String... command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command)
                .directory(workDir.toFile())
                .redirectErrorStream(false);
        Process process = pb.start();

        String stdout = new String(process.getInputStream().readAllBytes());
        String stderr = new String(process.getErrorStream().readAllBytes());
        int exitCode = process.waitFor();

        return new RunResult(exitCode, stdout, stderr);
    }

    private static boolean isAvailable(String... command) {
        try {
            Process p = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();
            p.getInputStream().readAllBytes();
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    record RunResult(int exitCode, String stdout, String stderr) {}
}
