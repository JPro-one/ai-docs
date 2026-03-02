package one.jpro.platform.aidocs.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Generates a SKILL.md file that describes the AI docs file structure
 * so AI coding agents know the documentation system exists and how to use it.
 *
 * The template is loaded from the classpath resource {@code SKILL.md}, which is
 * copied from the project root during the build. The placeholder path
 * {@code build/ai-docs} in the template is replaced with the actual output directory.
 */
public class SkillGenerator {

    private static final String DEFAULT_DOCS_DIR = "build/ai-docs";
    private static final String TEMPLATE_RESOURCE = "SKILL.md";

    /**
     * Generates a SKILL.md file at the given path.
     *
     * @param skillPath path to write the SKILL.md
     * @param docsOutputDir the relative path to the docs output directory (e.g. "build/ai-docs")
     */
    public static void generate(Path skillPath, String docsOutputDir) throws IOException {
        String content = generate(docsOutputDir);
        Files.createDirectories(skillPath.getParent());
        Files.writeString(skillPath, content);
    }

    /**
     * Generates SKILL.md content by loading the template and substituting the output directory.
     */
    static String generate(String docsOutputDir) throws IOException {
        String template = loadTemplate();
        return template.replace(DEFAULT_DOCS_DIR, docsOutputDir);
    }

    private static String loadTemplate() throws IOException {
        try (InputStream is = SkillGenerator.class.getResourceAsStream(TEMPLATE_RESOURCE)) {
            if (is == null) {
                throw new IOException("SKILL.md template not found on classpath");
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
