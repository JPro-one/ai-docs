package one.jpro.platform.aidocs.gradle;

import one.jpro.platform.aidocs.core.BuildTool;
import one.jpro.platform.aidocs.core.DocEntry;
import one.jpro.platform.aidocs.core.DocsCollector;
import one.jpro.platform.aidocs.core.EntriesFile;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Aggregates the partial outputs of all {@link CollectDocsPartialTask}s (this project
 * and its subprojects) into the final AI-navigable structure: per-library directories,
 * context.md, index.md, and the generated skill file.
 */
public abstract class CollectDocsTask extends DefaultTask {

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @Input
    public abstract Property<Integer> getContextMinLines();

    /** Partial output directories produced by the per-project collectDocsPartial tasks. */
    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getPartialDirs();

    /** Absolute path of the directory the skill file is written into. */
    @Internal
    public abstract Property<String> getSkillDirectory();

    /** Path of the docs output directory relative to the root project, used in the skill text. */
    @Internal
    public abstract Property<String> getRelativeDocsDir();

    @TaskAction
    public void collectDocs() throws IOException {
        Path outputDir = getOutputDirectory().get().getAsFile().toPath();
        DocsCollector.cleanOutputDir(outputDir);

        Map<String, DocEntry> byCoordinate = new LinkedHashMap<>();
        for (File partialDir : getPartialDirs()) {
            Path entriesFile = partialDir.toPath().resolve(EntriesFile.FILE_NAME);
            if (!Files.exists(entriesFile)) continue;
            for (DocEntry entry : EntriesFile.read(entriesFile)) {
                if (byCoordinate.putIfAbsent(entry.coordinate(), entry) == null) {
                    Path libDir = partialDir.toPath().resolve(entry.relativePath());
                    if (Files.isDirectory(libDir)) {
                        DocsCollector.copyDirectory(libDir, outputDir.resolve(entry.relativePath()));
                    }
                }
            }
        }

        var entries = new ArrayList<>(byCoordinate.values());
        DocsCollector.generateContextAndIndex(outputDir, entries, getContextMinLines().get());

        Path skillDir = Path.of(getSkillDirectory().get());
        if (DocsCollector.generateSkill(skillDir, getRelativeDocsDir().get(), BuildTool.GRADLE)) {
            getLogger().lifecycle("Generated AI skill at .claude/skills/docs/SKILL.md");
        } else {
            getLogger().lifecycle("Skipped .claude/skills/docs/SKILL.md (user-modified)");
        }

        getLogger().lifecycle("Collected documentation for {} libraries into {}", entries.size(), outputDir);
    }
}
