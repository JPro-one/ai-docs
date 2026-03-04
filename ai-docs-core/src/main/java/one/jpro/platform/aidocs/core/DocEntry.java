package one.jpro.platform.aidocs.core;

/**
 * Represents a collected documentation artifact.
 */
public record DocEntry(String group, String name, String version, String description, boolean hasSources, PomMetadata pomMetadata) {

    public DocEntry(String group, String name, String version) {
        this(group, name, version, null, false, null);
    }

    public DocEntry(String group, String name, String version, String description) {
        this(group, name, version, description, false, null);
    }

    public DocEntry(String group, String name, String version, String description, boolean hasSources) {
        this(group, name, version, description, hasSources, null);
    }

    public DocEntry withDescription(String description) {
        return new DocEntry(group, name, version, description, hasSources, pomMetadata);
    }

    public DocEntry withHasSources(boolean hasSources) {
        return new DocEntry(group, name, version, description, hasSources, pomMetadata);
    }

    public DocEntry withPomMetadata(PomMetadata pomMetadata) {
        return new DocEntry(group, name, version, description, hasSources, pomMetadata);
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
     * Returns the description from DOCUMENTATION.md if available, otherwise the POM description.
     */
    public String effectiveDescription() {
        if (description != null) {
            return description;
        }
        if (pomMetadata != null) {
            return pomMetadata.description();
        }
        return null;
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
