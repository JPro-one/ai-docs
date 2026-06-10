package one.jpro.platform.aidocs.core;

/**
 * The build tool generating the docs, used to tailor generated instructions.
 */
public enum BuildTool {
    GRADLE("Gradle", "gradle collectDocs"),
    MAVEN("Maven", "mvn one.jpro.aidocs:ai-docs-maven-plugin:collect-docs");

    private final String displayName;
    private final String collectCommand;

    BuildTool(String displayName, String collectCommand) {
        this.displayName = displayName;
        this.collectCommand = collectCommand;
    }

    public String displayName() {
        return displayName;
    }

    public String collectCommand() {
        return collectCommand;
    }
}
