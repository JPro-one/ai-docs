# Releasing

The version is **derived from git tags** — it exists nowhere in the repository:

- HEAD exactly on tag `vX.Y.Z` → version `X.Y.Z`
- commits after tag `vX.Y.Z` → version `X.Y.(Z+1)-SNAPSHOT`
- no tag → `0.1.0-SNAPSHOT`

## Releasing a version

```bash
./tagRelease.sh 0.1.0
```

The script only tags: it verifies a clean tree on an up-to-date `main`, then tags and
pushes `v0.1.0` — no publishing happens locally. The tag push triggers
`.github/workflows/release.yml`, which builds, tests, and runs the publish scripts:

| Script | Registry | Versions |
|---|---|---|
| `publishSandecArtifactory.sh` | Sandec Artifactory | snapshots (every main push, via CI) and releases |
| `publishMavenCentral.sh` | Maven Central | releases only (refuses snapshots) |
| `publishGradlePluginPortal.sh` | Gradle Plugin Portal | releases only (refuses snapshots) |

The scripts are the shared entry points for CI and manual use; each prints the derived
version before publishing. There is no version bump and no bump-back — after the release,
builds automatically become `X.Y.(Z+1)-SNAPSHOT`.

## Required repository secrets

- `SANDEC_ARTIFACTORY_USERNAME` / `SANDEC_ARTIFACTORY_PASSWORD`
- `SANDEC_SIGNING_KEY_ID` / `SANDEC_SIGNING_SECRET_KEY` / `SANDEC_SIGNING_PASSWORD` — GPG (signing is skipped when unset, so snapshot CI stays unsigned)
- `MAVEN_CENTRAL_AUTH_TOKEN` — Sonatype Central Portal token (same as jpro-platform)
- `GRADLE_PUBLISH_KEY` / `GRADLE_PUBLISH_SECRET` — Gradle Plugin Portal API keys

## Notes

- CI checkouts need `fetch-depth: 0` (configured) — a shallow clone can't see tags and would fall back to `0.1.0-SNAPSHOT`.
- `Central publishing type`: set `MAVEN_CENTRAL_PUBLISHING_TYPE=USER_MANAGED` to review the deployment in the portal before it goes live; default is `AUTOMATIC`.
- The `example/` and `example-maven/` projects pin the current snapshot version of the plugin; after a release moves the snapshot (e.g. to `0.1.1-SNAPSHOT`), bump them — or switch them to the released version.
