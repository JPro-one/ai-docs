package one.jpro.platform.aidocs.maven;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class CollectDocsMojoFunctionalTest {

    private static final String PLUGIN_VERSION = "0.1.0-SNAPSHOT";

    private static final String POM_HEADER = """
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
                <build>
                    <plugins>
                        <plugin>
                            <groupId>one.jpro.aidocs</groupId>
                            <artifactId>ai-docs-maven-plugin</artifactId>
                            <version>%s</version>
                        </plugin>
                    </plugins>
                </build>
            """.formatted(PLUGIN_VERSION);

    private static final String POM_FOOTER = "</project>\n";

    @TempDir
    Path projectDir;

    @BeforeAll
    static void checkMvnAvailable() {
        Assumptions.assumeTrue(isMvnAvailable(), "mvn not found on PATH");
    }

    @BeforeEach
    void setUp() throws IOException {
        // Create a minimal src/main/java directory so Maven doesn't complain
        Files.createDirectories(projectDir.resolve("src/main/java"));
    }

    @Test
    void collectDocsWithNoDependencies() throws Exception {
        writePom("");

        RunResult result = runMaven();
        assertThat(result.exitCode()).as("Maven exit code (stdout: %s, stderr: %s)", result.stdout(), result.stderr()).isEqualTo(0);

        Path aiDocs = projectDir.resolve("target/ai-docs");
        assertThat(aiDocs.resolve("index.md")).exists();
        assertThat(aiDocs.resolve("context.md")).exists();

        String index = Files.readString(aiDocs.resolve("index.md"));
        assertThat(index).contains("# AI Documentation Index");
        assertThat(index.lines().filter(l -> l.startsWith("- ")).count()).isZero();

        String context = Files.readString(aiDocs.resolve("context.md"));
        assertThat(context).contains("# Project Documentation Context");

        // SKILL.md should be generated
        Path skillFile = projectDir.resolve(".claude/skills/docs/SKILL.md");
        assertThat(skillFile).exists();
        String skill = Files.readString(skillFile);
        assertThat(skill).contains("ai-docs/");
        assertThat(skill).contains("index.md");
        assertThat(skill).contains("overview.md");
        assertThat(skill).contains("DOCUMENTATION.md");

        assertThat(result.stdout()).contains("Generated AI skill at .claude/skills/docs/SKILL.md");
    }

    @Test
    void collectDocsFromJproRoutingCore() throws Exception {
        writePom("""
                <dependencies>
                    <dependency>
                        <groupId>one.jpro.platform</groupId>
                        <artifactId>jpro-routing-core</artifactId>
                        <version>0.5.8</version>
                    </dependency>
                </dependencies>
                """);

        RunResult result = runMaven();
        assertThat(result.exitCode()).as("Maven exit code (stdout: %s, stderr: %s)", result.stdout(), result.stderr()).isEqualTo(0);

        Path aiDocs = projectDir.resolve("target/ai-docs");

        // index.md should list the library
        String index = Files.readString(aiDocs.resolve("index.md"));
        assertThat(index).contains("one.jpro.platform:jpro-routing-core");
        assertThat(index).contains("lines");
        long indexCount = index.lines().filter(l -> l.contains("jpro-routing-core")).count();
        assertThat(indexCount).isEqualTo(1);

        // DOCUMENTATION.md
        Path docFile = aiDocs.resolve("one.jpro.platform/jpro-routing-core/DOCUMENTATION.md");
        assertThat(docFile).exists();
        String doc = Files.readString(docFile);
        assertThat(doc.length()).isGreaterThan(100);
        assertThat(doc).containsIgnoringCase("routing");

        // overview.md
        Path overviewFile = aiDocs.resolve("one.jpro.platform/jpro-routing-core/overview.md");
        assertThat(overviewFile).exists();
        String overview = Files.readString(overviewFile);
        assertThat(overview).contains("jpro-routing-core");
        assertThat(overview).contains("## Chapters");
        assertThat(overview).contains("lines");
        assertThat(overview).contains(" — ");

        // context.md
        Path contextFile = aiDocs.resolve("context.md");
        assertThat(contextFile).exists();
        String context = Files.readString(contextFile);
        assertThat(context).contains("one.jpro.platform:jpro-routing-core");
        assertThat(context).contains("Chapters");

        // sources.jar
        Path sourcesJar = aiDocs.resolve("one.jpro.platform/jpro-routing-core/sources.jar");
        assertThat(sourcesJar).exists();
        assertThat(Files.size(sourcesJar)).isGreaterThan(0);

        // sources-index.md
        Path sourcesIndex = aiDocs.resolve("one.jpro.platform/jpro-routing-core/sources-index.md");
        assertThat(sourcesIndex).exists();
        String srcIndex = Files.readString(sourcesIndex);
        assertThat(srcIndex).contains("Source Index");
        assertThat(srcIndex).contains(".java");
    }

    @Test
    void collectDocsFromMultipleDependencies() throws Exception {
        writePom("""
                <dependencies>
                    <dependency>
                        <groupId>one.jpro.platform</groupId>
                        <artifactId>jpro-routing-core</artifactId>
                        <version>0.5.8</version>
                    </dependency>
                    <dependency>
                        <groupId>one.jpro</groupId>
                        <artifactId>jpro-webapi</artifactId>
                        <version>2025.3.1</version>
                    </dependency>
                </dependencies>
                """);

        RunResult result = runMaven();
        assertThat(result.exitCode()).as("Maven exit code (stdout: %s, stderr: %s)", result.stdout(), result.stderr()).isEqualTo(0);

        Path aiDocs = projectDir.resolve("target/ai-docs");

        String index = Files.readString(aiDocs.resolve("index.md"));
        assertThat(index).contains("jpro-routing-core");
        assertThat(index).contains("jpro-webapi");

        assertThat(aiDocs.resolve("one.jpro.platform/jpro-routing-core/DOCUMENTATION.md")).exists();
        assertThat(aiDocs.resolve("one.jpro/jpro-webapi/DOCUMENTATION.md")).exists();

        assertThat(aiDocs.resolve("one.jpro.platform/jpro-routing-core/overview.md")).exists();
        assertThat(aiDocs.resolve("one.jpro/jpro-webapi/overview.md")).exists();

        String routingDoc = Files.readString(aiDocs.resolve("one.jpro.platform/jpro-routing-core/DOCUMENTATION.md"));
        String webapiDoc = Files.readString(aiDocs.resolve("one.jpro/jpro-webapi/DOCUMENTATION.md"));
        assertThat(routingDoc.length()).isGreaterThan(100);
        assertThat(webapiDoc.length()).isGreaterThan(100);
    }

    @Test
    void collectDocsSkipsDependenciesWithoutDocumentation() throws Exception {
        writePom("""
                <dependencies>
                    <dependency>
                        <groupId>org.slf4j</groupId>
                        <artifactId>slf4j-api</artifactId>
                        <version>2.0.17</version>
                    </dependency>
                    <dependency>
                        <groupId>one.jpro.platform</groupId>
                        <artifactId>jpro-routing-core</artifactId>
                        <version>0.5.8</version>
                    </dependency>
                </dependencies>
                """);

        RunResult result = runMaven();
        assertThat(result.exitCode()).as("Maven exit code (stdout: %s, stderr: %s)", result.stdout(), result.stderr()).isEqualTo(0);

        Path aiDocs = projectDir.resolve("target/ai-docs");
        String index = Files.readString(aiDocs.resolve("index.md"));
        assertThat(index).contains("jpro-routing-core");

        // slf4j has sources but no DOCUMENTATION.md — should still appear with sources
        assertThat(index).contains("slf4j");
        Path slf4jSourcesJar = aiDocs.resolve("org.slf4j/slf4j-api/sources.jar");
        assertThat(slf4jSourcesJar).exists();
        Path slf4jSourcesIndex = aiDocs.resolve("org.slf4j/slf4j-api/sources-index.md");
        assertThat(slf4jSourcesIndex).exists();

        // slf4j POM has name — verify POM metadata is parsed
        assertThat(index).contains("SLF4J");
    }

    // --- helpers ---

    private void writePom(String extraContent) throws IOException {
        Files.writeString(projectDir.resolve("pom.xml"), POM_HEADER + extraContent + POM_FOOTER);
    }

    private RunResult runMaven() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("mvn", "ai-docs:collect-docs", "-B")
                .directory(projectDir.toFile())
                .redirectErrorStream(false);
        // Ensure mavenLocal is available
        pb.environment().put("MAVEN_OPTS", "");
        Process process = pb.start();

        String stdout = new String(process.getInputStream().readAllBytes());
        String stderr = new String(process.getErrorStream().readAllBytes());
        int exitCode = process.waitFor();

        return new RunResult(exitCode, stdout, stderr);
    }

    private static boolean isMvnAvailable() {
        try {
            Process p = new ProcessBuilder("mvn", "--version")
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
