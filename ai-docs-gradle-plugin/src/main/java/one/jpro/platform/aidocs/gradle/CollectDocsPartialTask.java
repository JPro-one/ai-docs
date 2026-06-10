package one.jpro.platform.aidocs.gradle;

import one.jpro.platform.aidocs.core.DocEntry;
import one.jpro.platform.aidocs.core.DocsCollector;
import one.jpro.platform.aidocs.core.EntriesFile;
import one.jpro.platform.aidocs.core.PomParser;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Collects the documentation artifacts of a single project's dependencies into a
 * partial output directory. The artifacts are resolved at configuration time
 * (see {@link DocsResolver}); this task only processes the resolved files, which
 * keeps it compatible with the configuration cache.
 */
public abstract class CollectDocsPartialTask extends DefaultTask {

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @Input
    public abstract Property<Integer> getOverviewMinLines();

    /** Encoded {@link ModuleSpec}s discovered at configuration time. */
    @Input
    public abstract ListProperty<String> getModuleSpecs();

    /** The resolved artifact files, tracked so content changes re-trigger the task. */
    @InputFiles
    @PathSensitive(PathSensitivity.NONE)
    public abstract ConfigurableFileCollection getResolvedFiles();

    @TaskAction
    public void collectPartial() throws IOException {
        Path outputDir = getOutputDirectory().get().getAsFile().toPath();
        DocsCollector.cleanOutputDir(outputDir);

        List<DocEntry> entries = new ArrayList<>();
        for (String encoded : getModuleSpecs().get()) {
            ModuleSpec spec = ModuleSpec.decode(encoded);
            var entry = DocEntry.of(spec.group(), spec.name(), spec.version());
            DocEntry enriched = entry;

            if (spec.doc() != null) {
                enriched = DocsCollector.collectDoc(outputDir, spec.doc().toPath(), entry, getOverviewMinLines().get());
                getLogger().lifecycle("Collected docs: {}:{}", spec.group(), spec.name());
            }
            if (spec.sources() != null) {
                DocsCollector.collectSources(outputDir, spec.sources().toPath(), enriched);
                enriched = enriched.withHasSources(true);
                getLogger().lifecycle("Collected sources: {}:{}", spec.group(), spec.name());
            }
            if (spec.changelog() != null) {
                DocsCollector.collectChangelog(outputDir, spec.changelog().toPath(), enriched, getOverviewMinLines().get());
                enriched = enriched.withHasChangelog(true);
                getLogger().lifecycle("Collected changelog: {}:{}", spec.group(), spec.name());
            }
            if (!spec.pomChain().isEmpty()) {
                var metadata = PomParser.parse(spec.pomChain().get(0).toPath());
                for (var parentPom : spec.pomChain().subList(1, spec.pomChain().size())) {
                    metadata = metadata.withFallback(PomParser.parse(parentPom.toPath()));
                }
                enriched = enriched.withPomMetadata(metadata);
            }
            entries.add(enriched);
        }

        entries.sort(Comparator.comparing(DocEntry::coordinate));
        EntriesFile.write(outputDir.resolve(EntriesFile.FILE_NAME), entries);
    }
}
