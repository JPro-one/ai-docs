package one.jpro.platform.aidocs.gradle;

import org.gradle.api.provider.Property;

/**
 * Configuration for the AI Docs plugin, available as {@code aiDocs { ... }}.
 */
public abstract class AiDocsExtension {

    public static final String NAME = "aiDocs";

    /**
     * Whether to also collect documentation for buildscript (plugin classpath)
     * dependencies. Defaults to {@code false} — application dependencies only.
     */
    public abstract Property<Boolean> getIncludeBuildscript();
}
