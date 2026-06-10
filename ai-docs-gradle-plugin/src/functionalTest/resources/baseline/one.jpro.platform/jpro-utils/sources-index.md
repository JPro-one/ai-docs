# jpro-utils (0.5.8) — Source Index
Source jar: in the local artifact cache, path stored in `sources.jar.link` (next to this file). Run the commands below from this directory.
Read one file: `unzip -p "$(cat sources.jar.link)" <directory><file>`, e.g. `unzip -p "$(cat sources.jar.link)" one/jpro/platform/utils/CommandRunner.java`
Extract all (best for finding methods and their javadoc): `unzip -q "$(cat sources.jar.link)" -d sources`

## Packages
- (root) (1 file)
  - module-info.java (14 lines)
- one/jpro/platform/utils/ (7 files)
  - CommandRunner.java (339 lines)
  - CopyUtil.java (120 lines)
  - FreezeDetector.java (74 lines)
  - OpenLink.java (54 lines)
  - PlatformUtils.java (109 lines)
  - TreeShowing.java (152 lines)
  - UserPlatform.java (196 lines)
