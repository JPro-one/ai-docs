package one.jpro.platform.aidocs.core;

/**
 * Represents a collected documentation artifact.
 */
public record DocEntry(String group, String name, String version, String description, boolean hasSources, boolean hasChangelog, PomMetadata pomMetadata) {

    /**
     * Creates a DocEntry with the required fields. Use {@code with*()} methods to set optional fields.
     */
    public static DocEntry of(String group, String name, String version) {
        return new DocEntry(group, name, version, null, false, false, null);
    }

    public DocEntry withDescription(String description) {
        return new DocEntry(group, name, version, description, hasSources, hasChangelog, pomMetadata);
    }

    public DocEntry withHasSources(boolean hasSources) {
        return new DocEntry(group, name, version, description, hasSources, hasChangelog, pomMetadata);
    }

    public DocEntry withHasChangelog(boolean hasChangelog) {
        return new DocEntry(group, name, version, description, hasSources, hasChangelog, pomMetadata);
    }

    public DocEntry withPomMetadata(PomMetadata pomMetadata) {
        return new DocEntry(group, name, version, description, hasSources, hasChangelog, pomMetadata);
    }

    /**
     * Returns the human-readable display name from POM metadata, or the artifact name as fallback.
     */
    public String displayName() {
        if (pomMetadata != null && pomMetadata.name() != null) {
            return pomMetadata.name();
        }
        return name;
    }

    /**
     * Returns the POM description if available, otherwise falls back to the
     * first line extracted from DOCUMENTATION.md.
     */
    public String effectiveDescription() {
        if (pomMetadata != null && pomMetadata.description() != null) {
            return pomMetadata.description();
        }
        return description;
    }

    public String coordinate() {
        return group + ":" + name + ":" + version;
    }

    /**
     * Returns the relative path for this library's directory (e.g. "one.jpro.platform/jpro-file").
     */
    public String relativePath() {
        return group + "/" + name;
    }
}
