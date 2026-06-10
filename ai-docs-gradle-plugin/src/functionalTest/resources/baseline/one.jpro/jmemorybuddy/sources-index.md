# jmemorybuddy (0.5.6) — Source Index
Source jar: in the local artifact cache, path stored in `sources.jar.link` (next to this file). Run the commands below from this directory.
Read one file: `unzip -p "$(cat sources.jar.link)" <directory><file>`, e.g. `unzip -p "$(cat sources.jar.link)" one/jpro/jmemorybuddy/CleanupDetector.java`
Extract all (best for finding methods and their javadoc): `unzip -q "$(cat sources.jar.link)" -d sources`

## Packages
- (root) (1 file)
  - module-info.java (9 lines)
- one/jpro/jmemorybuddy/ (3 files)
  - CleanupDetector.java (55 lines)
  - JMemoryBuddy.java (294 lines)
  - JMemoryBuddyLive.java (84 lines)
