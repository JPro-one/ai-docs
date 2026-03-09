package one.jpro.platform.aidocs.core;

/**
 * Metadata extracted from a Maven POM file. All fields are nullable
 * since POMs may omit any of these elements.
 */
public record PomMetadata(String name, String description, String url, String scmUrl, String license) {}
