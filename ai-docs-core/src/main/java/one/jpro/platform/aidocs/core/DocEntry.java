package one.jpro.platform.aidocs.core;

/**
 * Represents a collected documentation artifact.
 */
public record DocEntry(String group, String name, String version) {

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
