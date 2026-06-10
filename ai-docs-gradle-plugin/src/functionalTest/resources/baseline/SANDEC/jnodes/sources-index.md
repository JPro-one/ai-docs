# jnodes (0.8.3) — Source Index
Source jar: in the local artifact cache, path stored in `sources.jar.link` (next to this file). Run the commands below from this directory.
Read one file: `unzip -p "$(cat sources.jar.link)" <directory><file>`, e.g. `unzip -p "$(cat sources.jar.link)" de/sandec/jnodes/context/ContextManager.scala`
Extract all (best for finding methods and their javadoc): `unzip -q "$(cat sources.jar.link)" -d sources`

## Packages
- (root) (1 file)
  - module-info.java (14 lines)
- de/sandec/jnodes/context/ (5 files)
  - ContextManager.scala (41 lines)
  - IgnoreMe.java (7 lines)
  - PageContext.scala (6 lines)
  - PopupContext.scala (6 lines)
  - Util.scala (18 lines)
- de/sandec/jnodes/css/ (2 files)
  - DynamicCSS.scala (117 lines)
  - IgnoreMe.java (7 lines)
- de/sandec/jnodes/elements/ (7 files)
  - AdaptiveImageHorizontal.scala (24 lines)
  - AdaptiveNode.scala (42 lines)
  - Carousel.scala (89 lines)
  - FontResizingText.scala (90 lines)
  - IgnoreMe.java (7 lines)
  - Resizable.scala (27 lines)
  - ScalingElement.scala (22 lines)
- de/sandec/jnodes/fork/ (1 file)
  - UtilsPublic.java (844 lines)
