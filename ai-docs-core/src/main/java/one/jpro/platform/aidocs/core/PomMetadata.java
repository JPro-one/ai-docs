package one.jpro.platform.aidocs.core;

/**
 * Metadata extracted from a Maven POM file. All fields are nullable
 * since POMs may omit any of these elements.
 */
public record PomMetadata(String name, String description, String url, String scmUrl, String license) {

    /**
     * Fills missing fields from a parent POM's metadata. The name is never inherited —
     * a parent's name (e.g. "Gson Parent") would misrepresent the module.
     */
    public PomMetadata withFallback(PomMetadata parent) {
        return new PomMetadata(
                name,
                description != null ? description : parent.description(),
                url != null ? url : parent.url(),
                scmUrl != null ? scmUrl : parent.scmUrl(),
                license != null ? license : parent.license());
    }
}
