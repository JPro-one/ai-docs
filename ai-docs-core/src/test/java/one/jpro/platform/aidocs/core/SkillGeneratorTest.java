package one.jpro.platform.aidocs.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SkillGeneratorTest {

    @Test
    void containsKeyElements() throws IOException {
        String result = SkillGenerator.generate("build/ai-docs", BuildTool.GRADLE);

        assertThat(result).contains("build/ai-docs/");
        assertThat(result).contains("context.md");
        assertThat(result).contains("index.md");
        assertThat(result).contains("overview.md");
        assertThat(result).contains("DOCUMENTATION.md");
        assertThat(result).contains("gradle collectDocs");
        assertThat(result).contains("Reading Strategies");
    }

    @Test
    void usesProvidedOutputDir() throws IOException {
        String result = SkillGenerator.generate("custom/output/path", BuildTool.GRADLE);

        assertThat(result).contains("custom/output/path/");
        assertThat(result).contains("custom/output/path/index.md");
        assertThat(result).doesNotContain("build/ai-docs");
    }

    @Test
    void mavenVariantReferencesMaven() throws IOException {
        String result = SkillGenerator.generate("target/ai-docs", BuildTool.MAVEN);

        assertThat(result).contains("Maven plugin");
        assertThat(result).contains("mvn one.jpro.aidocs:ai-docs-maven-plugin:collect-docs");
        assertThat(result).contains("target/ai-docs/");
        assertThat(result).doesNotContainIgnoringCase("gradle");
    }

    @Test
    void startsWithFrontmatter() throws IOException {
        String result = SkillGenerator.generate("build/ai-docs", BuildTool.GRADLE);

        assertThat(result).startsWith("---\nname: docs\ndescription:");
        assertThat(result.indexOf("---", 4)).isGreaterThan(0);
    }

    @Test
    void writeToFile(@TempDir Path tempDir) throws IOException {
        Path skillFile = tempDir.resolve("SKILL.md");
        SkillGenerator.generate(skillFile, "build/ai-docs", BuildTool.GRADLE);

        assertThat(skillFile).exists();
        String content = Files.readString(skillFile);
        assertThat(content).contains("build/ai-docs/");
        assertThat(content).contains("index.md");
    }

    @Test
    void createsParentDirectories(@TempDir Path tempDir) throws IOException {
        Path skillFile = tempDir.resolve("deep/nested/dir/SKILL.md");
        SkillGenerator.generate(skillFile, "build/ai-docs", BuildTool.GRADLE);

        assertThat(skillFile).exists();
    }
}
