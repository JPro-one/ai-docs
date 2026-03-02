package one.jpro.platform.aidocs.core;

/**
 * Represents a collected documentation artifact.
 */
public record DocEntry(String group, String name, String version, String description) {

    public DocEntry(String group, String name, String version) {
        this(group, name, version, null);
    }

    public DocEntry withDescription(String description) {
        return new DocEntry(group, name, version, description);
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
