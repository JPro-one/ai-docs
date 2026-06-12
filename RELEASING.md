# Releasing

## Targets

| Registry | What | Why |
|---|---|---|
| Sandec Artifactory | all modules | internal use; CI publishes every main push automatically |
| Maven Central | `ai-docs-core`, `ai-docs-maven-plugin`, `ai-docs-gradle-plugin` + plugin marker | public Maven consumption |
| Gradle Plugin Portal | `ai-docs-gradle-plugin` | makes `plugins { id 'one.jpro.aidocs' version 'X' }` work without repo config |

## Required environment / secrets

- `SANDEC_ARTIFACTORY_USERNAME` / `SANDEC_ARTIFACTORY_PASSWORD` — Artifactory
- `SANDEC_SIGNING_KEY_ID` / `SANDEC_SIGNING_SECRET_KEY` / `SANDEC_SIGNING_PASSWORD` — GPG signing (signing is skipped when unset, so snapshot CI stays unsigned)
- `MAVEN_CENTRAL_AUTH_TOKEN` — Sonatype Central Portal token (same as jpro-platform)
- Gradle Plugin Portal API keys — `gradle.publish.key` / `gradle.publish.secret` in `~/.gradle/gradle.properties`, or `./gradlew publishPlugins -Pgradle.publish.key=... -Pgradle.publish.secret=...`

## Steps

The version defaults to a `-SNAPSHOT`; pass `-PreleaseVersion=X.Y.Z` to every command below
(Central and the Plugin Portal both reject `-SNAPSHOT`). Alternatively change the default in
the root `build.gradle`.

1. Verify: `./gradlew clean build -PreleaseVersion=X.Y.Z`
2. Publish to Artifactory and assemble the signed Central bundle:
   `./gradlew publish -PreleaseVersion=X.Y.Z`
3. Upload the bundle to Maven Central:
   `./gradlew publishToMavenCentralPortal -PreleaseVersion=X.Y.Z`
   (`MAVEN_CENTRAL_PUBLISHING_TYPE=USER_MANAGED` to review in the portal before release; default is `AUTOMATIC`)
4. Publish the Gradle plugin to the Plugin Portal:
   `./gradlew :ai-docs-gradle-plugin:publishPlugins -PreleaseVersion=X.Y.Z`
5. Tag the release (`git tag vX.Y.Z && git push --tags`).
