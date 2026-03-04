# Changelog

## 2026.1.x

### 2026.1.1 (February 20, 2026)
#### Improvements
* Added `jpro.debug.delayWS` configuration property to introduce an artificial delay (in milliseconds) before sending WebSocket messages. Useful for simulating slow network conditions during development.
* Improved startup logging: fixed startTime reporting (was previously showing the stop time) and added duration to the log output.

#### Bugfixes
* Fixed a rare deadlock during startup of JPro.
* Fixed a regression regarding showing virtual images.
* Fixed an issue where uploaded filenames were incorrect due to encoding issues. Files with special characters or spaces in their names are now handled correctly.
* Fixed an issue where uploaded files were written to the server's working directory instead of the temporary directory.
* Fixed an issue where the `vmoptions` file in the release zip contained Windows newlines when built on Windows, causing startup failures on Linux/macOS.
* Fixed issue regarding cursor detection for transparent Regions where pickOnBounds is false.
* Fixed text-input for nested Stages.

### 2026.1.0 (January 19, 2026)

#### Security
* Fixed an important security issue regarding file uploads via the WebAPI. It is recommended to update.

#### Improvements
* Added the possibility to extend the live check that is performed when calling `/status/alive`.
  You can now define a custom class with a main method. This class is called during the alive check.
  It can be configured with the property `jpro.liveCheck` in the `jpro.conf` file.
* Fixed OS detection in several places.
  In particular, default action key detection is now correct.
  On macOS, pressing Enter no longer triggers the default action.

#### Bugfixes
* Fixed an issue with Safari. There was an issue opening links created with `HTMLView` in Safari.

## 2025.3.x

### 2025.3.3 (December 22, 2025)
#### Improvements
* Added Canvas PixelWriter support, accessed using `GraphicsContext.getPixelWriter()`.
* InstanceID is now longer and guaranteed to be unguessable.
* Added support for remote testing with QF-Test.
* It's now possible to set the "image-rendering" attribute in the jpro-app tag.
  Its value is forwarded to image elements when running JPro.

#### Changes
* The experimental 3D support flag `jpro.beta.enable3D` is now disabled by default.
  Enabling only when needed keeps the default javascript file size to a minimum.
* Provide a new bundled JPro artifact that includes:
    - project wide CHANGELOG.md
    - project wide DOCUMENTATION.md
    - openapi specification for the jpro Server
    - a jpro-utils.qft library to help write tests against jpro using QF-test

#### Bugfixes
* Improved handling of Modal Dialogs. Fixed an issue with multiple modal dialogs sometimes not blocking input correctly.
* Fixed a shadow rendering issue where an unwanted shadow could appear in the top right corner.
* Also corrected an invalid optimization for regions with transparent backgrounds that caused incorrect shadow rendering.
* Fixed an issue where the cursor of a scene was incorrectly prioritized over the cursor of a node.
* Fixed an issue regarding CanvasRendering. If the content wasn't cleared but rendered across multiple frames, 
  sometimes settings like fill or stroke were not applied correctly.
* Fixed a regression for `2025.3.0` where `WebAPI.getInstanceInfo().dataSentWSProperty()` was always `0`. It now works correctly again.

### 2025.3.2 (November 18, 2025)

#### Major
* Reworked the entire Documentation. It's now much more structured, and easier to read.
* Added ability to set custom HTTP-Headers for responses using the ServerAPI.

#### Bugfixes
* Fixed issue regarding download of files with special characters in the filename.
This is a regression introduced in 2025.3.0.
* Fixed that, when using a TextField or TextArea with the setTextFormatter API, on every change the whole text was
formatted. Now it only gets formatted when the user finished the input.
This aligns the behavior with Desktop JavaFX. This may also fix unknown issues regarding text input.
* Fixed mouse cursor behavior in Regions with transparent backgrounds.
Regions don't influence the cursor.
* Fixed bug in ServerAPI. The Request returned a broken value for the Request.getURI() method.
* Fixed a rare exception when using Drag & Drop with multiple windows.
* Fixed issue regarding Modal Dialogs, where it was possible to send KeyEvents to the underlying Stage.
* Fixed that when a modal dialog was shown, it was possible to lose focus by clicking on the blocked area.
* Fixed a regression where remoteAddress was not set. This regression was introduced in 2025.3.0.
* The JPro-Loadbalancer now correctly uses the X-Forwarded-For and X-Forwarded-Host headers.
* Fixed an issue regarding rendering Text elements with stroke.
* Security improvement: restricted access to images that were not currently in use by the JVM.
* Fixed the JPro-Loadbalancer sometimes not starting properly on some non-english linux versions.

### 2025.3.1 (September 20, 2025)
#### Major
* Added support for JavaFX 25.

#### Bugfixes
* Fixed an issue accessing resources when the JPro Loadbalancer is used.
  This happened when `WebAPI.openLocalURL(url)` was used.
* Dropping a file outside a drop area no longer opens the file in the browser.
  This caused problems by accidentally closing the JPro application.
* Fixed a rare race condition, regarding Drag and Drop.
  It didn't have any known visible effect, but could log an exception.

### 2025.3.0 (September 10, 2025)

#### Major
* Changed the web framework used internally from Play to Helidon.
  This allows us to react more quickly to the latest security updates, 
  and won't affect how you use JPro.
* The JPro Loadbalancer is now released together with JPro.
* Added support for Gradle9.
* JPro now requires Java 21 or later.

#### Features
* Every build is now automatically checked against the NVD (National Vulnerability Database) for security issues.
* We now also release a SBOM (Software Bill of Materials) for every JPro release.
It can be found in the `jpro-server` artifact as a file named `jpro-server_2.12-<version>-cyclonedx-bom.json`.

#### Bugfixes
* Small bugfix for the Maven Plugin. When JAVA_HOME is an empty String, we now fall back to the default java command.
This is the same behaviour JPro has in the Gradle Plugin and the start scripts.
* Fixed an issue regarding indexing by Google and other crawlers that don't support websocket.
* ImageIO is now explicitly initialized in the ClassLoader of the App.
  Previously, it was sometimes initialized in the JPro Classloader - leading to subtle differences.
* Fixed an issue where calling `WebAPI.closeInstance()` would sometimes result in an exception.
* Removed the redirect of the log files in the start-background.sh script on linux and mac.
This could lead to an endlessly growing log file. Now all the log files are created by the internal logger of JPro, 
which doesn't have such issues.
* Fixed a rare issue when rendering non-uniform corner radii on Regions.

## 2025.2.x

### 2025.2.1 (June 3, 2025)

#### Features
* Improved how shortcut key combinations are detected. 
Combinations are now correctly based on the user's device rather than the server OS.
This can be configured with the property `jpro.shortcutSource` in the `jpro.conf` file.
* The `ServerAPI.addRequestHandler` API has been improved. The request now contains the headers and body. 
The request can now be any HTTP method (GET, POST, PUT, PATCH, DELETE).
The response can now set the status code.

#### Bugfixes
* Added a fix for context menus and modality (`Stage.initModality`) where it was previously 
possible to open context menus in the blocked stage.
* Fixed an exception which happened when a ComboBox was opened twice in a Popup.
In this case, it was only possible, to open the ComboBox once.
* Fixed an issue when using Canvas. In some cases the
  `Canvas.setClip()` method was not working as expected.
* Removed the accidental rounded borders at the bottom on undecorated Stages.
* Fixed an issue regarding rendering & minWidth/minHeight for `Window`.
The bounds are now respected correctly. 
When the size of the `Scene` and `Window` do not match, 
the Scene size is now used as in JavaFX.
* Fixed remaining issue regarding dropping folders as files.
* Fixed an issue where resources were not properly released after image creation.
* When using the Gradle commands `jproStart` and `jproStop` at the same time, stop was always called first. 
Now, the order is respected. This makes it easier to start and stop 
servers for test environments.


### 2025.2.0 (April 2, 2025)

#### Features
* Added support for JavaFX 24.
* In `Canvas`, clipping with arbitrary shapes is now supported.
* Improved instance startup timeout logging. The timeout can now be configured with `jpro.instanceStartupTimeoutSeconds`
  in the `jpro.conf` file.
* Added `WebAPI.getInstanceInfo().getRemoteAddress()` method to retrieve client IP addresses.

#### Bugfixes
* Fixed a regression with Text which happened in some cases when multiple `\n` or `\r` characters were used.
* Folders can no longer be dropped as files. They can't be uploaded and are now properly filtered out.
* Texts with gradients are now rendered correctly.
* The initial font for Canvas is now applied correctly.
* Fixed issue in the `Canvas` where in some rare situations, the identity matrix was used incorrectly.
* Fixed issue with WebAPI access when using `jpro.linkUnownedWindowsToFirstInstance = true`.

#### Changes
* Updated core dependencies that create the javascript files of JPro.

## 2025.1.x

### 2025.1.0 (January 31, 2025)

#### Features
 * Added experimental 3D support. It can be activated in the jpro.conf with `jpro.beta.enable3D=true`.
 * Rewrote Focus Management. If multiple Stages are shown, now only one window is "visible focused".
 * Updated the JavaFX 17,21 and 23 versions, which are used by JPro.
 * Added new methods to the WebAPI to detect the current platform: `WebAPI.getPlatformOld()` and `WebAPI.getPlatform()`.
   The method `WebAPI.getPlatformOld()` maps to `navigator.platform`, which is well-supported but deprecated.
   The method `WebAPI.getPlatform()` maps to `navigator.userAgent.platform`, which is not well-supported but is not deprecated.
   We provide an easier cross-platform API in the jpro-platform that also supports Desktop.

#### Bugfixes
 * When `Stage.title` and `Stage.icon` are null, the title and icon of the html page are no longer overwritten.
 * Added support for BorderStrokeStyle.NONE for Region.
 * Fixed an issue with the Stage Border. When browser zoom was used, sometimes a "transparent line" was shown.
 * Fixed Issue with JAVA_HOME containing a space. This happened on Mac and Linux, when the start script from 
   the JPro release was used.
 * When a ComboBox opened, it wasn't possible to select the next node with Tab. This is fixed now.
 * When a Node was focused, and a Tooltip appeared - than the key events were received by the popup of the Tooltip, 
   instead of the focused Node. This is fixed now.
 * Fixed an Exception, which happened when a stage has an icons, and it's manually shown/hidden. [#192](https://github.com/JPro-one/JPro/issues/192)
 * Fixed and simplified text implementation, to fix very rare issues with text rendering.
 * `JSFile.getUploadedFileFuture()` can now be called outside the JavaFX Thread.
 * Fixed rare render bug in ImageView. Certain property combinations involving Viewport were not rendered correctly.
 * Fixed an exception, when `Text.text` is set to null.

## 2024.4.x

### 2024.4.1 (November 8, 2024)

#### Changes
* Published the `jpro-webapi` artifact to the Maven Central Repository. The artifact is now available at the following
  coordinates:
    - Maven POM:
      ```
      ...
      <dependency> 
          <groupId>one.jpro</groupId>
          <artifactId>jpro-webapi</artifactId>
          <version>2024.4.1</version>
      </dependency>
      ...
      ```
    - Gradle Build:
      ```
      implementation 'one.jpro:jpro-webapi:2024.4.1'
      ```
  For more information, refer to the [JPro Documentation](https://www.jpro.one/docs/current/1.3/CREATE_A_PROJECT).
* The `syncStageAttributes` attribute is now enabled by default.

### 2024.4.0 (November 5, 2024)

#### Feature
* JavaFX 23 (version 23.0.1) is now supported and set as the default version.
  Note that JavaFX 23 requires JDK 21 or later.
* JavaFX 17 LTS has been updated to version 17.0.12.
* JavaFX 21 LTS has been updated to version 21.0.4.
* You can now start JPro with the argument `-Djpro.parent.pid=<PID>`. If the specified parent process stops, the JPro
  server will also stop automatically.
* When the BuildTool Process of the command `mvn jpro:run` or `gradle jproRun` is stopped forcefully, then the JPro
  server is now also stopped.
* The browser tab now accurately reflects the JavaFX stage's title and favicon. The tab title is set based on the 
  stage's `title` property, and the favicon is sourced from the first icon in the stage's `getIcons` list. To enable
  this feature, use the `syncStageAttributes` attribute in the `jpro-app` tag. For more details, refer to the updated
  documentation or [JPro Issue #185](https://github.com/JPro-one/JPro/issues/185).

#### Improvements
* The implementation JSFile is no longer an anonymous class - making debugging easier.

#### Bugfixes
* Fixed Stages, when the StageStyle `Transparent`, `Unified`, or `Utility` is used.
* Fixed various issues, regarding special characters, and the start script of the JPro Release.

## 2024.3.x

### 2024.3.3 (September 17, 2024)

#### Features
* Added a new `WebAPI.openStageAsTab(Stage stage, String target)` method to the WebAPI. This allows a stage to be opened
  as a new tab in a specified browsing context via the `target` parameter.
* The CursorImage is now shown when using a DragBoard, even if its size exceeds 100x100 pixels.

#### Changes
* The `WebAPI.registerWindow(Window)` method now returns the instance ID if called multiple times, instead of throwing
  an exception.
* Removed unused, experimental and legacy media implementation.

#### Improvements
* Improved overall `WebAPI` javadoc documentation.

#### Bugfixes
* Fixed an issue during the creation of the release zip file by preventing duplicate entries.
* Fixed an issue where the application instance was being disposed of, despite being in singleton mode.
* Fixed an issue, when using a CursorImage. Sometimes the image wasn't shown.
* Fixed a regression in `2024.3.2` that affected file uploads within a Window.
* Fixed a regression in `2024.3.2` where the mouse position in a child of a decorated Stage was sometimes inaccurate.
* Fixed a regression in `2024.3.2` where the resize cursor of a decorated Stage occasionally leaked to its child Stage.

### 2024.3.2 (August 29, 2024)

#### Features
* Windows now have Stage decoration and are resizable and movable.
* Added the properties browserX, browserY and instanceID to the properties of every Window.
  This is used by QF-Test to make tests more stable. This is also useful for other tools.

#### Changes
* The root of the JPro Rendering is now the starting stage, instead of its scene. This is only an implementation change
  only and should not affect functionality.
* The Main Window is now centered at the same place, new Windows are opened. 
  Before it was shown in the top left corner of the "Virtual Screen" of the JPro Server.

#### Bugfixes
* Fixed an issue with changing the Scene of an additional Stage or Popup.
* Fixed an issue where some application resources failed to load with the `jproRun` or `jproStart` Gradle tasks when the
  module system is activated via `useModuleSystem`.
* Fixed an issue where the `jproRun` or `jproStart` Gradle tasks, as well as `jpro:run` and `jpro:start` Maven goals,
  failed to start the JPro server when directory names contained spaces.
* Fixed an issue where `jproReleaseFiles` specified via the plugins failed to overwrite existing files on Windows
  platform.

### 2024.3.1 (August 5, 2024)

#### Features
* It's now possible to copy out text of **non-editable** TextFields with the keyboard.

#### Improvements
* Removed an unnecessary layout pass when the application is started.

#### Bugfixes
* Fixed issue [#182](https://github.com/JPro-one/JPro-Tickets/issues/182) about the SVG shapes and Path elements not rendering correctly in the browser by ensuring that
  the fill rules (such as `EVEN_ODD` and `NON_ZERO`) are properly applied and rendered in accordance with JavaFX’s
  standard behavior.
* Fixed a bug when a Stage was shown as a Popup, then the size provided with `setWidth`/`setHeight` was ignored in favor
  of the preferred size of the node.
* Fixed errors caused by long messages sent between JS-Client and JPro-Server.
* Fixed an issue in the `Dockerfile` provided in the JPro-Release zip that caused an error during the Docker image build
process.
* Fixed replacing port number variables in the `docker-compose.yml` file to ensure dynamic port assignment during the
  creation of the JPro-Release zip.
* Fixed an issue where the content of additional Stages and Popups were not clipped.

### 2024.3.0 (July 11, 2024)

#### Release Notes
**If you update to this release, please make sure to update the [JPro Loadbalancer](https://sandec.jfrog.io/ui/native/repo/one/jpro/jpro-loadbalancer/0.13.0/jpro-loadbalancer-0.13.0.jar)
at least to version 0.13.0.**

This release introduces modularization of `jpro.webapi` to comply with JPMS (Java Platform Module System).
It improves the KeyEvents generated by JPro, ensuring that the KeyCodes are now correct. The WebSocket (WS) connection
syntax now uses query parameters. The plugin property `releasePlatforms` has been reworked so the JPro-Release zip now
includes only the current platform binaries and no longer adds `linux` by default. Additionally, an important issue has
been fixed regarding the transmission of large messages between the JPro Server and the JPro JS Client.

#### Features
* Modularize `jpro.webapi` to be in compliance with the Java Platform Module System (JPMS).
* Added the method `ServerAPI.getJProVersion()` to the ServerAPI.
* Rewrote how KeyInputs are interpreted by JPro.
  KeyCombinations work now reliable.
  It also works reliable with various KeyboardLayouts.
  We no longer use old Browser API, which are now deprecated.

#### Improvements
* Rewrote the syntax for the ws connection. Previously it was using the character "!" in some cases,
  which is not a valid character in a URL. Now it uses query parameters instead.
* The JPro-Release zip now includes by default only the binaries for the current platform. Please refer to the updated
  documentation for more details on how to include additional platforms, by configuring `releasePlatforms` property under
  `CONFIGURING JPRO` section.
* The WebAPI can now be used directly after calling `WebAPI.openStageAsPopup`, `WebAPI.openStageAsTab`, or similar methods.

#### Bugfixes
* Fixed a bug, when the js-client sends very big messages and the JPro Loadbalancer is used.
  In this case the message was not received correctly.
* Stages outside the primary stage are now rendered in the visible area of the browser.
* Stages with an owner which is not shown, are now handled like an "unlinked" stage.
* Fixed a bug with the scroll events, having wrong screenX and screenY values in Popups.
* Fixed a bug with the mouse events when visible is set to true for debugging. In this case, sometimes the screenX and
  screenY values were wrong for the mouse events. In rare cases also the x and y values were wrong.
* Fixed a rare issue related to Popups. In a rare case, the Windows was "unfocusable".
* Fixed a rare issue. When a Popup was clicked, closed, and the event handler throw an exception, sometimes the underlying
  application did also receive the click event.

## 2024.2.x

### 2024.2.1 (June 3, 2024)

#### Features
* Added the class `JProTextInputControl`. This class can be used when you have written a custom class similar to 
`TextInputControl` without extending the JavaFX class. JPro will treat this class as a `TextInputControl`, utilizing a 
hidden `<input>` element in the browser.
* The working directory of a JPro application can now be changed when started from the **start** scripts included in a 
JPro-Release zip. Refer to the documentation for more details.

#### Changes
* When using Gradle, the sandec repository is now added at the end of the repositories. This allows the user to
overwrite it.

#### Improvements
* The start scripts in the JPro-Release zip now check if a JavaFX build for the current platform is available. If not,
they will print an error message and exit.

#### Bugfixes
* In Gradle, when an application used the same dependency as JPro, it could sometimes affect the version used by JPro.
This issue has been fixed.

### 2024.2.0 (May 6, 2024)

#### Release Notes

In this release, JPro introduces significant enhancements, including official support for **ARM64** architecture on 
Linux, ensuring broader platform compatibility.

Additionally, JavaFX compatibility is confirmed across major Linux distributions, with **JavaFX 22** now set as the 
default version. 

Developers will appreciate the introduction of a new **JavaScript API** for seamless interaction with the JavaScript
environment, offering better developer experience and improved error reporting.

#### Features
* ARM64 (aarch64) on Linux is now officially supported for JPro. You can include the Linux ARM64 platform in the release
zip by using the string `linux-aarch64` in the `releasePlatforms` property of the Gradle/Maven plugin.
* Our JavaFX Fork was rebuilt for all versions using our new `JPro Launchpad` build pipeline.
We have confirmed compatibility with the following Linux distributions:
  - Debian Bookworm
  - Fedora 39
  - Ubuntu 20.04
  - Ubuntu 22.04
  - Ubuntu 24.04
  - Some very old Linux distributions might not work anymore.
* JavaFX 22 is now supported and set as the default.
* Introduced a new API for interacting with the JS environment, deprecating many old methods. The new API, centered 
around the class `JSVariable`, is more powerful and developer-friendly. Key methods include `WebAPI.js().evalCode(code)`
, `WebAPI.js().evalFuture(code)` and `WebAPI.js().evalPromise(code)`.
* If an error happens through the evaluation of an `JSVariable`, the error is now propagated to the Java side.
* If an `JSVariable` is used after it has been collected, an informative error message is now thrown.
* `JSVariable` now features methods indicating completion and success status.
* `WebAPI.getElement` and `WebAPI.getHTMLViewElement` are no longer experimental.
* The `useZGC` parameter is now set to `false` by default. This is because of ZGC issues on some systems.
* Introduced a new experimental ServerAPI allowing definition of HTTP handlers for the JPro server.

#### Improvements
* The Gradle plugin now enforces versions of the JPro server dependencies to prevent potential overrides by Maven 
plugins/BOMs.
* Images are now created in a background thread, enhancing responsiveness, particularly for WriteableImages and Images 
loaded from InputStreams.
* Increased maximum upload file size to 2GB from the previous 500MB.
* Added a workaround for the issue reported in **2023.3.3** and [JDK-8321737](https://bugs.openjdk.org/browse/JDK-8321737) fixing the `LC_CTYPE` environment 
variable on Linux inherited from Mac terminals if it has an invalid value.
* The maximum size of the **WebSocket** is now configurable, with a default value of **64KB**, resolving issues with the
JPro Loadbalancer and other intermediary services.
* Reduced JPro server dependencies, resulting in approximately 10% size reduction.
* Improved overall `WebAPI` javadoc and Docker configuration documentation.

#### Bugfixes
* Activation of the module system via the `useModuleSystem` property on the Gradle/Maven plugin and generation of the 
release zip with `jproRelease`/`jpro:release`, now correctly utilizes the module system.
* Resolved a bug preventing TextInput when switching focus directly from one ComboBox to another.
* Fixed a rare issue causing file upload failure when `user.dir` was set, introducing a more robust solution.
* Addressed a corner case where replacement images were displayed during image loading in some scenarios.

## 2024.1.x

### 2024.1.0 (January 10, 2024)
This release contains an important Fix for the JavaFX Performance Regression in JavaFX21 [JDK-8322964](https://bugs.openjdk.org/browse/JDK-8322964)!

#### Features
* Added the method `WebAPI.getLocale()` and `WebAPI.getLocale()` to the WebAPI.
* Added the method `JSVariable.isPromise()` for an easy way to check if a JSVariable is a promise.
* The Z Garbage Collector (ZGC) is now enabled by default. It is designed to minimize pause times, which is crucial for
  frontend applications where responsiveness and user experience are paramount. By keeping garbage collection pauses 
  consistently low (typically under 10ms), ZGC ensures smoother application performance. Also, efficient handling of 
  memory allocation and deallocation can lead to better overall memory utilization. This flag can be disabled by 
  setting the property `useZGC` to `false` on both Gradle and Maven plugin.

#### Changes
* Updated the Dockerfiles in the JPro-Release zip and in the Documentation.
* The method `WebAPI.loadJSFile` now loads the file from the global context. This way it matches the normal way of 
  loading the library with a script tag.

#### Bugfixes
* Fixed the behaviour of virtual images. URLs containing query parameters work now correctly.
  Also, the parameter `jproServerAsProxy` is now interpreted correctly.

## 2023.x.x

### 2023.3.3 (December 13, 2023)

#### Improvements
* Improved the performing when evaluating JS through the WebAPI.
* Improved all shell scripts distributed in the JPro release archive.

#### Features
* Added new methods to the WebAPI to interact with JavaScript - especially with promises and asynchronous code.
  The following methods were added:
  - `CompletableFuture<String> executeScriptWithFuture(String code)`
  - `JSVariable registerJavaFunctionWithVariable(Consumer<JSVariable> callback)`
  - `CompletableFuture<JSVariable> WebAPI.executeJSAsync(String)`
  - `JSVariable WebAPI.executeJSAsyncPromise(String)`
  - `static JSVariable JSVariable.futureToPromise(CompletableFuture<JSVariable> future)`
  - `static CompletableFuture<JSVariable> JSVariable.promiseToFuture(JSVariable promise)`
* To ensure file uploads and downloads function correctly, we check Java's native encoding for umlaut support. 
  This step is particularly crucial on Linux systems to avoid infrequent yet unstable configurations. We resolve this 
  by setting a specific environment variable. We also reported it as a Java bug ticket
  [JDK-8321737](https://bugs.openjdk.org/browse/JDK-8321737).
* The Drag and Drop API now works with Touch events.

#### Bugfixes
* Fixed Maven release artifacts path for ARM architectures when `mvn jpro:release` is called.
* Fixed a bug with the Gradle plugin. I could when another Plugin is used after the JPro plugin. When the other plugin
  added dependencies with "afterEvaluate" to the project, then Gradle failed with an exception.
* Removed the property `processArgs` from the GradlePlugin, which wasn't supposed to be public.
* Fixed download (and other operations with WebAPI) of files and resources with special characters like `'`.
* The method `WebAPI.executeScriptWithVariable(code);` now also works with block statements. Before, it only worked with
  expressions. This makes it behave similar to the other methods in the `WebAPI` to execute JS.
* Fixed filename normalization when uploading files with special characters on some Linux systems.
* Region Border, with different widths per side and gradients, are now rendered correctly.
* The console log file now mentions the correct thread name.

### 2023.3.2 (December 8, 2023)

* Don't use this version. Instead, update to version 2023.3.3.

### 2023.3.1 (October 26, 2023)

#### Features
* It's now possible access the MimeTypes of a dragged file with the method `FileHandler.getFilesDragOverTypes()`.
  Accessing the filenames/extensions is not possible due to browser limitations.
* Added the attribute `jpro-hidden` to Window. When set to true, then the window is not rendered as part of its owners
  instance.
  It can be set with `window.getProperties().put("jpro-hidden",true)`.

#### Bugfixes
* When a "Snapshot" of a page is created for indexing, then an exception is sometimes thrown since `2023.3.0`.
  In certain configurations, this could trigger a restart with systemd.
  This if fixed now. Also, the close reason in this case is now always `snapshot-finished` for both the Instance and the
  View.
* `WebAPI.runAfterUpdate(f)` no longer blocks other `runAfterUpdate` functions from executing if the provided
  function `f` throws an exception.
* Fixed the native rendering of fonts on macOS. This is only relevant when creating a snapshot.
* The method makeMultiFileUploadNodeStatic(Node) had the wrong return type. It was changed from `FileUploader`
  to `MultiFileUploader`.

### 2023.3.0 (October 4, 2023)

#### Features
* JavaFX21 is now supported and used by default.
* Reworked how instances are closed. It can now be configured with more details in the jpro.conf.
  It's now also possible to configure that an instance is closed when the user is afk, or the tab is in the background -
  which is not the default behavior.
  Check out the `JPro Close Instance Strategy Configuration` section for more details.
* Enhanced WebAPI.FileHandler with a new property, FileHandler.selectionMode, to enable configurable multiple file
  selection.
* Added a new version of `WebAPI.executeScriptWithVariable(f)` which returns a JSVariable.
* Added a new configuration parameter `jpro.linkUnownedWindowsToFirstInstance` to the `jpro.conf` file.
  When set to `true`, all unowned windows are linked to the first instance.
  This is useful for applications not designed to run multiple Instances in the same JVM.
* Text translation in the browser for a specific node can now be enabled or disabled by
  setting the attribute **translate** in the Node properties to `true` or `false`.
  The translation rule is inherited from the parent node.

  > As an example, disabling translation on a specific text field can be done by calling:
  textField.getProperties().put("translate", false);
* Added missing Documentation on how to customize the keyboard with the `vkType` property.
* Added new properties to `InstanceInfo`. The properties are **nodesCreated**, **nodesSynchronized** and *
  *nodesCollected** for the instance.
* Reworked the new experimental `InstanceInfo` API in the WebAPI. Added the properties afk, background and
  lastActionTime.
  The original methods are now changed to be properties.
* The JPro Gradle plugin now works with JavaFX Gradle plugin version `0.1.0`.

#### Changes
* Reduced the CPU usage of an idle JPro instance.

#### Bugfixes
* Removed the accidental `throws Exception` from the method `WebAPI.executeScriptWithVariable`
  and `WebAPI.executeScriptWithReturn`.
* Always the correct **WebAPI** version matching the JPro server is now used. This ensures that the server starts
  properly.
* Fixed a bug when setting a custom JavaFX Version in the Gradle/Maven Plugin. The version was not set correctly.
* Fixed the method `InstanceInfo.getInitialHostName`. Before, it always threw an exception.
* Fixed the structured log messages when closing an instance. Some values were set to be "Unknown".
* Fixed `WebAPI.closeInstance()`. It now works as expected.

### 2023.2.2 (August 16, 2023)

#### Features
* Renamed the newly added method `WebAPI.getHTMLElement` to `WebAPI.getElement`.
* Added the new experimental method `WebAPI.getHTMLViewElement`, `WebAPI.wrapNode` and `WebAPI.loadNode` to the WebAPI.
    * WebAPI.wrapNode: Useful for implementing links, copy-to-clipboard functionality, and more
    * WebAPI.getHTMLViewElement: For enhanced integration with external HTML libraries.
    * WebAPI.loadNode: Serialize a node before using it in the browser. This is useful for large nodes, which are not
      used immediately.
* Use `ISO-8601` format based representation for `Time running` information in the JPro stats page.

#### Bugfixes
* Resolved an issue where, if two or more Canvas nodes were utilized and the same image was rendered in the same frame,
  only one image would appear. This has now been corrected.
* Previously, when the Canvas attempted to render an unloaded font, it defaulted to a generic font. This behavior has
  been improved to ensure the intended font is loaded and correctly rendered.
* Addressed a rare translation operation error during the Canvas rendering process.
* Implemented canvas filling attribute `FILL_RULE`, an algorithm by which is determined if a point is inside or outside
  the filling region.
* Added support for rendering dashed lines in the Canvas node.
* Disable automatic translation in the browsers for text input controls.
* When using in the Gradle plugin `jproRun` or `jprStart` tasks, or in the Maven plugin `jpro:run` or `jpro:start`
  phases,
  the plugin will first verify if an exising JPro server is already running on the designated port before initiating a
  new server.

### 2023.2.1 (July 3, 2023)

#### Features
* Added a new method `WebAPI.getHTMLElement` to retrieve the HTML element of a given node.
  One use case is to implement a "copy to clipboard" functionality for a given node - which can only be done on a click
  event in the browser.
* Deprecated old WebAPI methods `WebAPI.registerValue` because they are not used.

#### Bugfixes
* Fixed a bug with the new, improved `start.bat` script. After stopping the JPro server, a process was hanging in the
  background.
* Fixed a bug with the improved Gradle plugin. In some cases, not all dependencies were compiled.

### 2023.2.0 (June 29, 2023)

#### Features
* We now support **JavaFX20** and use it by default!
* We have updated the JPro Maven plugin. We now guarantee with automated tests, that the MavenPlugin
  works for `3.6.x`, `3.8.x`, `3.9.x` and the latest preview `4.0.0-alpha-5`.
  It is now possible to configure the JPro server by passing arguments to a JPro related goal via
  `-Djpro.param=value`, where `param` is the name of the JPro related parameter and `value` is its value.

  *Please note that if the parameter itself has already been configured via the JPro Maven Plugin inside the POM file,
  the given `value` will **NOT** be taken in consideration.*

  > As an example, to configure the `port` parameter before starting the server,
  we can call the following command `mvn jpro:run -Djpro.port=9000`.

* The `WebAPI.getInstanceInfo()` and `WebAPI.getServerInfo()` methods have been introduced to provide access to instance
  and server information, enabling users to retrieve and monitor the current status. These classes are marked
  as `@Experiemntal`
  since they are still subject to change in the future.
* Added a new logging section on the Documentation page.
* It's now possible to override the default `JPro` logging configuration by providing a new `logback.xml` file
  accessed by defining its path via the following options inside `jpro.conf`.
    1. `jpro.logger.resource` - to access the file as a resource in the classpath
    2. `jpro.logger.file` - to access the file as an external file
    3. `jpro.logger.url` - to access the file as a URL
* Added the `jpro.logToJUL` configuration parameter to the `jpro.conf` file. When set to `true`, the logging will be
  redirected to the Java Util Logging (JUL).
* Added the attribute `disableVirtualKeyboard` to the jpro tag. When set to true, the virtual keyboard is disabled.
* Added the configuration option `jpro.onJVMStartup` to the `jpro.conf` file. This option allows to specify a class,
  which
  is executed on JVM startup. This is useful to initialize the JVM with some code, before the JPro server has started.
* Added the configuration option `jpro.onJVMShutdown` to the `jpro.conf
* Added the plugin property `workingDir` for both Maven and Gradle. This option allows to specify the working
  directory of the JPro server before it is started.
* A new type of logging messages called `structured` logging has been introduced and used internally by JPro. Added the
  `jpro.logToJsonFormat` configuration parameter to the `jpro.conf` file. When set to `true`, the structured logging
  messages will be converted in JSON format, making them easier to analyze and work with. Furthermore, the documentation
  section has been updated to include information about this new feature and all the parameters that are currently being
  used.
* Updated the JavaFX17 version to `17.0.7`.

#### Changes
* Improved the startup time of the JPro server. Several parts are now loaded in parallel.
* Improved the performance of the Gradle plugin. The `jpro` tasks now are no more dependent on gradle assemble task.
  This will speed up the build process.
* The internally used URLClassLoader is now an own class JProServerURLClassLoader, so we get better error messages
  related to classloaders.
* When using the WebAPI, changes to the cookies with `WebAPI.setCookie(key,value)` and `WebAPI.removeCookie(key)` are
  now
  immediately visible in the ObservableMap `WebAPI.getCookies()`.
* Added documentation to the attribute "snapshot". Changed its default value from "false" to "auto".
  When set to true, the JPro app is rendered as a static image. On "auto" this only happens when it's indexed and
  WebSocket is not available.
  This is useful to properly index the webpage.
* Deprecated `WebAPI.getServerName()` method. Use `WebAPI.getBrowserURL()` instead.
* In `jpro.conf` configuration file, the `jpro.onStartup` option was renamed to `jpro.onFXStartup`. In addition, the
  documentation section was updated to reflect the new changes.
* The default release platforms contained inside the release zip file are now `linux` and `current` platform of the
  user.
* Added a useful error message, if for some reason JavaFX is not found on the classpath.
* Improved the start performance of the start.bat file in the JPro release.

#### Bugfixes
* Fixed the JavaFX warning message `WARNING: Unsupported JavaFX configuration`
* Fixed the download/upload of files when using the JPro Loadbalancer.
* Fixed a race condition, which happened when a snapshot (for indexing) is created before the first normal instance is
  started.
  This caused an Exception and general instability of the JPro server.

### 2023.1.0 (January 16, 2023)

#### UPGRADE INSTRUCTIONS
We have completely rewritten the Gradle plugin and Gradle Kotlin DSL is now supported.
The Gradle plugin was renamed from `com.sandec.jpro` to `jpro-gradle-plugin`. Also,
Gradle and Maven plugins group id were renamed from `com.sandec.jpro` to `one.jpro`.

Examples of configuration changes for both Gradle and Maven from previous versions.

Previous gradle plugin configuration:

```
buildscript {
  ...
  dependencies {
    classpath 'com.sandec.jpro:jpro-plugin-gradle:2022.1.8'
  }
  ...
}

apply plugin: 'com.sandec.jpro'
```

New gradle plugin configuration:

```
buildscript {
  ...
  dependencies {
    classpath 'one.jpro:jpro-gradle-plugin:2023.1.0'
  }
  ...
}

apply plugin: 'jpro-gradle-plugin'
```

Previous Maven plugin configuration:

```
<plugin>
      <groupId>com.sandec.jpro</groupId>
      <artifactId>jpro-maven-plugin</artifactId>
      <version>2022.1.8</version>
      <configuration>
           ...
      </configuration>
</plugin>
```

New Maven plugin configuration:

```
<plugin>
      <groupId>one.jpro</groupId>
      <artifactId>jpro-maven-plugin</artifactId>
      <version>2023.1.0</version>
      <configuration>
           ...
      </configuration>
</plugin>
```

#### Features
* Added `JSFile WebAPI.createJSFile(String objectURL, String filename, long size)`.
* Added the method `JSVariable JSFile.getObjectURL()`.
* Added support for Data URLs in Images.
* It's now also possible to check whether JPro is used in a browser or not via `Boolean.getBoolean("jpro.isbrowser")` as
  an alternative to `WebAPI.isBrowser()`.
* We now also publish the JPro CHANGELOG and JPro DOCUMENTATION as an additional artifact of the webapi dependency.
* Add new `releaseName` property on both Gradle and Maven plugins to configure the name of the zip file generated during
  `jproRelease` task.
* Added information about the License/Copyright into the jar of the WebAPI.

#### Bugfixes:
* Fixed a bug, which caused JPro to not work, when the domain ended with `.app`.
  A workaround for older versions is to provide the whole domain in the href of the JPro Tag. (for example: 
  <jpro href="https://myapp.app/app/default"/>)
* On Mac platform, when `openURLOnStartup` property is `true`, the duke icon is no longer shown after opening the
  browser.
  
## 2022.x.x

### 2022.1.8 (November 22, 2022)

#### Features
* Added support for the property `Node.viewOrder`. Nodes with viewOrder are now rendered in the correct order. This
  property was added in JavaFX 9.
* Added `Instances active` and `Instance afk` to the page `/status`.
* The OS related information in `/status` is now computed in a background thread. This excludes any potential OS/JVM
  related performance issues.
* Introduced a new configuration parameter `verbose` for the Maven and Gradle plugins. When set to true, the 
  start-arguments are logged, making it easier to debug when the application isn't starting properly.
* It's now possible to change the directory of the logfiles and other temporary files by setting `-Djpro.logdir` as a VM
  argument.

#### Bug Fixes
* Fixed a very old bug, that when the Gradle task `jproRun` was stopped, the JPro server was not stopped correctly.
* Fixed a bug, when JPro tried to reconnect to the server, the reconnecting animation was not shown correctly.
* Fixed a regression in 2022.1.6 for `WebAPI.darkModeProperty()` and `WebAPI.devicePixelRatioProperty`.
  The properties wrongly stopped sending updates. This is now fixed.
* Fixed a regression in the file upload in `2022.1.5` until `2022.1.7`. When a file was uploaded, in some cases the
  encoding was changed resulting in changed content.
* When `WebAPI.addInstanceCloseListener(InstanceCloseListener)` was called after the page was closed, the listener was
  not called. Now it is called immediately. As a negative side effect, a memory leak was created by keeping the 
  reference to the listener. This is now fixed.
* Fixed a small leak related to the JSFiles created by the WebAPI.

### 2022.1.7 (November 6, 2022)

#### Features
* Added experimental support for the module system. When setting the property `useModuleSystem` on the Gradle/Maven
  Plugin, then all dependencies are added as modules instead of added to the classpath.
* It's now possible to download all logfiles as a zip by invoking `<server>/info/log/all.zip`

#### Changes
* Doubled the default size of the logfiles to 2MB.

#### Bugfixes
* Fixed regressions relating to the reconnect behaviour of the js client.
* Fixed an issue with the MavenPlugin. Sometimes the JavaFX artifacts of the wrong operating system were added to the
  classpath. Specifically, the `mac` artifacts were added sometimes to the `mac-aarch64` artifacts.

### 2022.1.6 (November 2, 2022)

#### Features
* Added an API to upload multiple files at once. Check out the method `WebAPI.makeMultiFileUploadNode(Node)`.
  A sample can be found in the `multifilehandler` sample, in
  our [JPro-Samples GitHub project](https://github.com/JPro-one/JPro-Samples).
* Added a new method `WebAPI.registerWindow(Window)`. It can be used to create a new JPro instance based on an existing
  Window. This was used to implement browser native scrolling, which is now used in 
  [jfx-central.com](https://www.jfx-central.com/).
  It's also available as a library in [jpro-utils](https://github.com/JPro-one/jpro-utils.git).
* When a touch event is generated, and it was used for scrolling in the browser, then isStillSincePressed is set to
  false for the created MouseEvent.

#### Improvements
* Improved logging. When an instance/view is closed, always the reason is reported, and the instanceID.
* If an instance is created with the WebAPI, but no user connects to it, then the corresponding instance is closed after
  1 minute. It was possible to create an instance with the methods 
  `WebAPIregisterWindow(Window)`, `WebAPI.openStageAsTab(Stage)` `WebAPI.openStageAsPopup(Stage)`.

#### Bug Fixes
* Instances, which are not opened once, are now closed after 1 minute. This could happen with the method 
  `WebAPI.registerWindow(Window)`, `WebAPI.openStageAsPopup(Stage)`, or `WebAPI.openStageAsTab)`.
* Fixed a bug with the JPro tag `<jpro-app>`.
  When the HTMLElement was removed and readded to the DOM, then an additional session was started. This is now fixed.
* Various improvements to error handling.

### 2022.1.5 (September 27, 2022)

#### Features
* Support For Java19
* Support for JavaFX19
* The cookies in the WebAPI are now automatically updated when they are changed from
  another tab. The update happens also when the cookies are changed from outside of JPro.

#### Bugfixes
* Now a simple message is logged when the user closes a tab.
  It used to produce a warning message and sometimes also an exception.
  They both didn't have side effects, but they were superfluous.
* Fixed a rare issue when rendering. In some corner cases, render fragments were visible for 1 frame.

### 2022.1.4 (August 24, 2022)

#### Bugfixes
* Fixed a memory leak related to the recently introduced class named "JSVariable" in the WebAPI.
* Fixed an issue, when using the WebAPI for downloading a file.
  Files containing "/1" in its URL were not downloaded properly.
  This was a regression in `2022.1.2`.
* Fixed an important bug related to event handling. Under some conditions, an event was evaluated later than it should,
  which generated an impression of bad performance execution of the event (the handling of the event was deferred until
  a next event arrived).
  This happened mainly on mobile.

### 2022.1.3 (August 3, 2022)

Added support for Java18.

#### Changes
* When using Gradle, by default the same java command is now used, as when running with the application plugin.
  This also has the effect, that it's now possible to configure the JVM used in IntelliJ.
* The default value for `jpro.preventSystemExit` is now false. This was done to support Java18.
  It has the consequence, though, that, either you need to explicitly configure `jpro.preventSystemExit` to true, or
  you now have to make sure, your application does NOT call `System.exit`, when the stage is closed.
* Added the properties `objectURLProperty` and `shouldCreateObjectURL` to the FileHandler.
* Added the methods `createUniqueJSName()` and `createUniqueJSName(String prefix)`to the WebAPI.

#### Bugfixes
* Updated internal libraries and removed no longer needed JVM argument.
* Fixed an issue with Maven on Mac Arm.
* Fixed an issue when loading externally hosted images. This bug was introduced in `2ß22.1.2`
* Fixed some rare exceptions, related to Popup and mouse input. It didn't have any side effect.

### 2022.1.2 (May 30, 2022)

JPro now also supports **ARM** for Mac and Linux!

#### Improvements
* Fixed a performance issue. This was especially harmful when running the JPro Loadbalancer with multiple JPro servers
  under MS-Windows.

#### Features
* JPro supports **ARM** for Mac and Linux!
* Added the field `jpro.statusUpdateTime` to the jpro.conf.
  When set to -1, no statistics are tracked for the status page.
* Removed the OSDetector plugin from the JPro Gradle Plugin.

#### Bugfixes
* Fixed an issue with Apache as a Reverse Proxy. In some cases the URLs generated by JPro were manipulated in a way,
  which broke loading images. this is now fixed.
* Improved the error message, when `WebAPI.openStageAsTab` or `WebAPI.openStageAsPopup` is called with null.

### 2022.1.1 (April 8, 2022)

Added support for JavaFX 18!
Jpro now uses JavaFX18 by default.

#### Features
* Added the method `WebAPI.closeInstance()`.
  This closes the current session and triggers a reconnecting in the client.
  This is especially useful with the LoadBalancer, when a fresh session is wanted.
* Added attribute "rememberInstanceIDInCookie" to the JProTag. When set to true, only one instance of the app is created
  per browser.
* Various resources, which are accessed by the browser, now have simplified URLs.
  This makes the link independent of the current folder of the server and makes it easier to create a sitemap and
  supporting indexing for Google.
* Added more detailed information to `/jpro/api/instances` about the opens views, and how long the instances will be
  kept open after closing all views.
* In the JProTag, the attribute `timeUntilReconnect` is removed. We have improved the reconnect behavior of the JPro
  Server.

#### Bugfixes
* Fixed a bug related to TextInput, when switching the focus between different windows.
* Fixed a bug, when the for the case that the `blurType` property of an effect was null. Before this fix, the instance
  wouldn't render properly. Now it uses the correct fallback `BlurType.THREE_PASS_BOX`.
* Fixed the script in the zip generated by JProRelease for MS-Windows. Before this fix, it failed when the path of the
  current folder contained Unicode Characters.
* Fixed a performance issue for the JPro Release when using MS-Windows. The start script `start.bat` now starts much
  faster and opens fewer filehandlers.
* `WebAPI.createVirtualImage` can now be called from outside the JavaFX Thread.
* Images created with `WebAPI.createVirtualImage` now return the correct URL, when calling `Image.getUrl()`.
* Fixed an error, which occurred when reconnecting to a JPro instance(“JPro session”) containing non-digit characters.
  This is only relevant for the JPro Loadbalancer.

### 2022.1.0 (March 1, 2022)

This version provides a new and efficient way/API for loading and rendering what we call “virtual images” in JPro. When
using this API the virtual images don't require any RAM space in the JPro Server, but instead enables the browser to
access the files directly through pure referencing, either to an HTTP location or to a file located on some server. The
API also provides static methods, which are useful when using the API independent of a specific JPro session.
This release drops the support for the previous JavaFX versions 14, 15, and 16 in favor of the current LTS versions 11
and 17.

#### Features
* Added support for real time updates of changes taking place in WritableImages.
* Added support for virtual images, they can be created with the
  methods `WebAPI.createVirtualImage(String url, int w, int h, boolean jproServerAsProxy)`
  and `WebAPI.createVirtualImage(String url, int w, int h, boolean jproServerAsProxy)`.
* Added static versions of the methods `setLossless` and `makeFileUploaderNode` to the WebAPI.
* Added darkMode property to the WebAPI.
* The user input is now processed in an runLater instead of an animation.
* Added the attribute `setThemeColor` to the JProTag.
  When set to true, a meta element is created which binds a webpage’s theme-color to a scene’s color.
* In the page `jpro/api/instances` users’ addresses were added and are now shown.
* LinearGradient and RadialGradient are now supported in the Canvas.
  RadialGradient has the limitation, that only non-distorted (scale in x or y) gradients are supported.
  Non-distorted scaling usually happens when the proportional property is set to true.

#### Bugfixes
* The Gradle task `jproRun` is now part of the task group `jpro`.
* Fixed a regression, when the `!<appname>` instance was used, to make sure an app is only created once.
* Fixed a regression to the ScreenSize provided by `javafx.stage.Screen`.

## 2021.x.x

### 2021.2.3 (February 9, 2022)

#### Summary
this update focuses on features for our new JPro loadbalancer. It also provides many minor improvements and bug fixes.

#### Features
* The new id can now be set as a query parameter when starting a new instance (`?newInstanceID=<newid>`). This is useful
  for load balancing.
* In the jpro.conf it's possible to set the value `jpro.addInstanceID`.
  When set to true, the requests from the client to the server will contain the current instance id.
  This is useful for load balancing.
* It's now possible to define a main method, which gets executed during the JPro Server’s startup.
  This can be used to speed up the handling of the initial session. It can be configured
  with `jpro.onStartup = <mainclass>`.
* Added support for `TextAlignment.JUSTIFY`, for all browsers supporting `text-align-last` which is the case for most
  common browsers, except Safari.
* Reworked the api-key. It is now provided with the key `jpro.apiKey`, as a query parameter `?apiKey=<key>`.
* Added the page `jpro/api/instances` which contains various information about the currently running instances. This is
  primarily used for the new JPro Loadbalancer.

#### Changes
* The websocket connection is now done on the url /app/ws/<appname> instead of /app/<appname>. This is only an internal
  change.
* A small performance improvement when using Text.
* Simplified `stop.bat` in the JProRelease file.
* In the default html page, the hostname is no longer part of the path to the application.
  This is useful when a load balancer is used, to avoid showing the internal address.
* Added "Instances Created" to the `/status` page.
* When a JavaScript exception is thrown, its stack trace is now printed.
* `WebAPI.getInstanceID` now returns a String instead of an Int.

#### Bugfixes
* The white background of Stages with transparent fill and without `StageStyle.TRANSPARENT`, is now rendered correctly.
* Fixed a memory leak in ScrollPane, related to touch events (JDK-8279228)
* Fixed an issue with Popups, being sometimes positioned wrongly.
* Fixed an issue with JPro, sometimes not shutting down properly.
* Added a more detailed error message, instead of an exception when the focus to a window cannot be set.
* Fixed a rare bug, which can cause the start method of the Application to be called twice.
* Fixed a minor memory leak in the js client.
* ScrollEvents now return the correct value for the method `getMultiplierX()` and `getMultiplierY()`.

### 2021.2.2 (December 1, 2021)

#### Features
* Merged our JavaFX17 fork with the latest JavaFX version 17.0.1
* JPro no longer uses the System font on Linux and Windows with JavaFX17, which is the default.
  This makes the behavior independent of the OS.

#### Bugfixes
* Fixed a regression with the JavaFX17 build for Linux. It works again for Ubuntu18.04.
* [#114](https://github.com/JPro-one/JPro-tickets/issues/114) Fixed a bug for atypical fonts.
  Texts with fonts with an unusual baseline were rendered on a wrong y position.
  This happened with icon fonts, and very rarely with normal fonts.
* [#88](https://github.com/JPro-one/JPro-tickets/issues/88) Fixed a bug which caused some Fonts on Mac to render with
  the wrong width.
  This happened with system fonts which were not supported by the browser. Please be aware, this bug will remain fixed
  with JavaFX17+, only. When using lower JavaFX versions, this bug will still remain.
* Fixed a regression in the rendering of TextFlow. In some situations, the text was shifted wrongly by one character.
* Fixed a rare exception related to Mobile and Popups.
* Fixed issues in the SVG fallback for RTL texts.

### 2021.2.1 (November 15, 2021)

#### Features
* Added support for the property `Node.nodeOrientation`.
  This is usually used for applications using a "right to left" language.
* JPro now correctly renders "right to left" text.
  This is required for languages like Hebrew or Arabic.
* JPro now correctly supports `Text.selectionFill`.
  This fixes the font color of the selected text in TextField and TextArea.
* Updated the logical fonts provided by JPro. As a sans serif font, now Lato is used by default.
  JPro now also provides an italic and bolditalic sans serif font.

#### Bugfixes
* Fixed the control accelerator memory
  leak [JDK-8274022](https://bugs.openjdk.java.net/browse/JDK-8274022) ([Pull request to JavaFX](https://github.com/openjdk/jfx/pull/659))
  in our JavaFX17 fork.
  This leak is a regression in JavaFX17 and happens quite frequently. It will probably be integrated into the next
  official JavaFX17 release.
* Fixed an issue with implicitly created stages, which were created due to an implementation detail.
  The `getWebAPI(Node, WebAPIConsumer)` wrongly executed the listener also for such stages.
  The listener executions for such stages were now eliminated.
  Now the lambda is only executed when its window is managed by JPro.
* When a download was started, the JPro-Session sometimes stopped too early.
  This is now fixed.
* Fixed an issue with the cookies in the WebAPI not being updated correctly.
* Fixed the `PointerCapture` mechanism for Firefox.
  When scrolling by dragging the bar of a scrollbar, no events were created when the mouse was outside the browser.
  This is now fixed.
* Fixed a bug in the scroll events. In rare case the browser scrolled in lines instead of pixels.
  This happened on Firefox running on Ubuntu 18.04.
* Fixed an issue that JPro didn't process `pen` events correctly. Ironically this caused problems with mouse input.

### 2021.2.0 (October 5, 2021)

#### Features
* JPro now uses JavaFX17 by default!
* Added support for Java17!
* Added support for Touch Events! When using JPro on mobile, now touch events are generated.
  Touch events are also generated on the desktop when using a touch device.
* JPro now generates scroll events, when using touch events. When released, the scrolling keeps the scroll inertia for a
  short time. This greatly improves the user experience on mobile.
* It's now possible to add a cleanup method to downloadURL and downloadResource,
  which can be used to clean up the file when it's no longer required.
* Added a new function `WebAPI.loadCSSFile` to the WebAPI. It works analogue to loadJSFile. This helps to integrate
  various tools for the browser.
* Added the properties `HTMLView.blockMouseInput` and `HTMLView.blockKeyboardInput` to HTMLView, to notify JPro, whether
  the HTMLView should prevent javafx events when it is the target.

#### Bugfixes
* Fixed corner cases for `WebAPI.getWebAPI(Node node, WebAPIConsumer consumer)`.
* Fixed a deadlock related to drag and drop.
* Fixed a rare rendering bug related to clip and HTMLView.

### 2021.1.4 (September 2, 2021)

#### Features
* Links to images which are available from the classpath, no longer contains information about the installation folder.

#### Bugfixes
* Fixed a memory leak that allocates memory outside the heap.
  Due to this bug, we recommend updating to this version.
* Fixed an exception during the shutdown. It didn't have any further side effects.
* Fixed an issue with the Gradle Plugin.
  When calling `jproRun`, and the JPro process was killed from the outside, the Gradle process wasn't stopped.
  This is now fixed.
* Fixed a regression in 2021.2.3. Sometimes opening Popups caused the browser to jump. This is now fixed.

### 2021.1.3 (August 2, 2021)

#### Bugfixes
* When a tab was closed, the closing of the WebSocket connection triggered a reconnect to the server.
  This could result in a small flickering and unnecessary resource usage. This is now fixed.
* Fixed an issue with fonts. Sometimes fonts were loaded multiple times,
  which could cause some flickering during font rendering.
* Fixed a very rare deadlock during startup.

### 2021.1.2 (July 5, 2021)

#### Features
* JPro now always uses UTF-8 as the default encoding for the JVM.
* Canvas is now rendered for snapshots when no window is set.
  In the `jpro.conf`, `canvasRenderOnServer` now has the default value `null`,
  to automatically detect whether it should be rendered in the server.
* Added `addInstanceID` to the JProTag. If this value is set, all http requests from the browser will add the current
  instanceID. This is useful for load balancers.
* It's now possible to add a file named `defaultpage` to the package `jpro/html`. This file is provided when no file
  extension is provided, or the extension `html` was provided.
  This is useful to develop web pages with a normal URL scheme.
* JPro now shows the path to the JavaFX jar for easier debugging of configuration errors.

#### Bugfixes
* Fixed the rendering of the padding for TextFlow. In some situations, the wrong padding was used.
* Fixed a bug for using JAVA_HOME, which was introduced in the previous update.
* Fixed a bug for the Gradle plugin, which is relevant for multi-project setups.
* Fixed an exception, when monocle is not set.
* Minor compatibility improvement for Java.

### 2021.1.1 (May 19, 2021)

#### Features
* Support for Java16.
* JPro now uses the JAVA_HOME variable everywhere when available, otherwise, it falls back to the normal Java command.
  This affects both the Gradle and the Maven Plugin.
  JAVA_HOME is now being used when starting JPro from the build tool and also when starting JPro with the JproRelease
  command.

#### Bugfixes
* Fixed an issue with Gradle 7.x. In the zip created by JProRelease.
  The artifact of the current project was missing. Rarely, this also
  happened in older Gradle versions.
* Fixed an issue when installing the JProServer as a Windows service.
  The issue had the effect, that the application wouldn't start properly.

### 2021.1.0 (May 11, 2021)

#### Features
* JPro now uses JavaFX16 by default.
* The JProRelease now contains support for *Windows*!
  It contains scripts to start/restart/stop the process, and to install it as a service.
* When resizing the window, now the Background of the Scene is resized immediately without waiting for the new
  scenegraph. This highly improves the experienced performance.
* JPro now uses high-resolution images with "@2x" at the end of the name, when using high-resolution displays.
  This can be deactivated in the jpro.conf with the statement `jpro.useHighResImages = false`.
* The WebAPI of an application is now set before calling `Application.init`. This can be accessed by the method
  getWebAPI of the class JProApplication.
* The WebAPI now provides the methods `devicePixelRatio` and `getDevicePixelRatio` to access the devicePixelRatio of the
  browser.
* Added support for the property `Shape.strokeLineCap`.
* Added the attributes `disableClip` and `disablePointerCapture` to the JProTag. When set to `true` JPro either ignores
  clip or doesn't use pointerCapture for the mouse-input.
* Added more detailed error messages, when a connection to the JPro Server could not be established.

#### Bugfixes
* Fixed the rendering of diacritical letters and other glyphs composed of multiple characters.
* Fixed a browser tab crash for Safari, related to pointerCapture.
* The file upload didn't work, when the Node was part of a Popup or a Stage with an owner. This is now fixed.
* Backported [JDK-8089589](https://github.com/openjdk/jfx/pull/398) for our JavaFX16 fork.
* Fixed an issue with the Focus of Substages and Popups. The Nodes can now get focused.
* Fixed an exception when switching scenes.
* Fixed an exception during mouse-input when visible is set true.
* Fixed the behavior when the font is set to null. Now `Font.defaultFont` is used in this case, just like it is for
  desktop JavaFX - instead of an internal Exception.
* Fixed a race condition happening on Safari, which sometimes caused the file upload to not work.
* Fixed an issue related to screen resizing. Sometimes on fullscreen a small white area was unused for the application.

## 2020.x.x

### 2020.1.6 (April 20, 2021)

* Moved to a new repository for hosting the artifacts because of
  the [Bintray shutdown](https://jfrog.com/blog/into-the-sunset-bintray-jcenter-gocenter-and-chartcenter/).
  The old Bintray repository will stop working at the 1. Mai. The new repository
  is `https://sandec.jfrog.io/artifactory/repo`.
  Checkout our commits to our HelloWorld projects, for the required
  changes: ([Maven](https://github.com/JPro-one/HelloJPro-Maven/commit/dfad6b6ace5f5d179dd1ccb26b3ede6fb3c53064),
  [Gradle](https://github.com/JPro-one/HelloJPro/commit/1c33028c033abf757d97d40ad53f0ed25782f325))

### 2020.1.5 (March 24, 2021)

#### Features
* Added an error message to be thrown when an unsupported JVM which bundles JavaFX is used. A JVM which bundles JavaFX
  is not supported by JPro, because JPro uses its own JavaFX Fork.
* To avoid confusing behavior the method `WebAPI.openStageAsTab` requires the Owner to be null. When the Owner is not
  null, we throw an exception.

#### Bugfixes
* Fixes for MouseEvents. Now, when a Session is closed, a MouseExited-Event with the last mouse position is sent. Before
  this fix, when the Mouse was pressed and released, the MousePressed-Event was suppressed and the MouseReleased-Event
  only was generated. This is now fixed.
* When the JavaFX-Thread was blocked, sometimes the webserver was not responding. This is now fixed.
* Fixed the rendering of the class SubScene.

### 2020.1.4 (February 16, 2021)

#### Features
* Added **Docker Compose** to the JProRelease.
  If you unzip the generated zip-file, you can run `docker-compose up` to run the application as a Docker service.

#### Bugfixes
* Fixed a startup error on Window. The Maven and Gradle Plugin now forwards the classpath for JPro Itself through a file
  instead of through the command line.
  This is very important to avoid the maximum length for the command line on Windows.
* Fixed a memory leak that happened after reconnecting to a running application.
  This could, for example, happen after being offline due to short connection problems.
* Fixed a bug in the Gradle plugin when JPro was used in a subproject.
  The server process is now started in the same folder as the folder of the subproject.
  This sometimes caused issues when starting the server related to the RUNNING.PID file.
* Fixed issue related to mouse input on mobile Firefox. Possible exceptions in the javascript-code are now properly
  logged.

### 2020.1.3 (January 27, 2021)

#### Bugfixes
* Fixed a regression in 2020.1.1, scrolling inside Popups wasn't working properly.
* Fixed an issue with the Gradle plugin. Sometimes the file RUNNING_PID was checked in the wrong folder.
* Fixed a bug in the zip created by JProRelease. This is a regression from 2020.1.0.
  The scripts `restart.sh`, `restart-background.sh` and `start-background.sh`
  weren't giving their arguments to the JVM, which was the previous behaviour.
* Fixed position of the Popup from the ComboBox. It was sometimes positioned outside the screen.

### 2020.1.2 (January 5, 2021)

This release uses a new version of our JavaFX Fork.

* Fixed a rare deadlock during the startup of JPro.
* Fixed a memory leak inside of JavaFX.
* JPro now uses JavaFX 15.0.1 instead of 15.0.0 as it’s default version.
* Added a new experimental feature based on (JMemoryBuddy)[https://github.com/Sandec/JMemoryBuddy].
  We now check every closed application, whether it gets properly collected.
  A list of uncollected applications can be found under `/info/minmemory`.
  All uncollected stages can be found in the HeapDump by searching for the class `AssertCollectableLive`.

### 2020.1.1 (December 7, 2020)

#### Features
* JPro now automatically renders Stages with an Owner, as part of the application.
  It works the same way as it previously worked with PopupWindow.
* The class FileHandler in the WebAPI now contains a List of String `filehandler.supportedExtensions`,
  to define the selectable file extension. This is limited to the file chooser and doesn't work with D&D due to
  limitations in the browser.
* Added SystemLoad and JVMLoad to the page `/status`.

#### Bugfixes
* Fixed a rare case where the FileHandler wasn't working with Safari.
* PopupWindow and Stages now receive events in the same order as they are rendered.
  Previously sometimes the Window which was rendered in the background got priority over events. This is now fixed.
* Fixed issue with some input events not correctly delivered to the content of the HTMLView.
* Fixed text-align property of the HTMLView. It is now no longer set for the outer div element of the HTMLView.
* Fixed a rare issue with the mouse input for HTMLView. The issue happened when the content of the HTMLView captured the
  mouse event.
* It's no longer possible, in rare situations, to "drag" parts of the application in the browser. This had the effect,
  that mouse events were slightly changed.

#### Regression
* With the current version, in some rare cases the mouse-cursor in Safari is wrong.

### 2020.1.0 (October 19, 2020)

JPro now supports **JavaFX14** and **JavaFX15**.
JPro now uses JavaFX15 by default.
We no longer support JavaFX8 in the standard versions.

#### Breaking Changes:
* The scripts in the bin folder of the JProReleaseZip now behave differently.
  The `start.sh` and `restart.sh` scripts now start JPro in the foreground.
  The newly added scripts `start-background.sh` and `restart-background.sh start JPro in the background.

#### Features
* JPro now always renders snapshot properly, without any configuration.
* Added the method `WebAPI.getWebsocketCookies()`. It returns the cookies of the WebSocket connection instead of the
  current browser tab. Their contents may differ, for example, due to different domains.
* JPro now properly loads the various logical fonts. In the previous version,
  most italic and bold italic versions were missing.
* The JavaFX version can now be configured in the plugins via the attribute `javafxVersion`in Maven and in Gradle.
  Possible values are `auto`, `latest`, `15`, `14` and `11`. The default value is `auto` which currently uses JavaFX15.

#### Bugfixes
* JPro now supports Gradle 6.6.x
* Fixed deprecations in Gradle to make sure the plugin will work with future releases of Gradle.
* Fixed an exception related to opaque images and mouse events in `QuantumToolkit.imageContains`
* Fixed the cursor in the PasswordField. It's now always positioned correctly.
* Fixed broken optimization for region border.
* Added new scheduling for updating the rendered Scene.
  This fixes some rare rendering of incomplete frames that sometimes felt like stuttering.

## 2019.x.x

### 2019.2.7 (August 13, 2020)

#### Features
* New Cookie API!
  We've added the methods `setCookie()` and `deleteCookie()` to easily save and delete cookies.
  `getCookies` now behaves differently. It's now an observable map and contains the cookies of the browser page instead
  of the websocket connection.
  The map gets updated after using `setCookie()` or `deleteCookie()`.
* Changed the default for `fxContextMenu` in the JPro Tag.
  By default, instead of the browser Context-Menu, the JavaFX Context-Menu is used.

#### Bugfixes
* The way the `WebAPI.downloadURL()` works was changed. It's now simpler and fixes a bug with the Firefox Version 78.
* We changed the way Cursors were set through Scene or Dragboard.
* We added an info message to the start script in JProRelease to inform the user,
  that the process is starting in the background.

### 2019.2.6 (July 21, 2020)

#### Bugfixes
* Fixed a bug with mouse input. In some situations the special-keys were not set in the MouseEvents.
* Fixed a memory leak in the javascript client.
* When using `loaderURL` in the JProTag the loader was not centered. This is now fixed.
* Firefox: Fixed a regression with the file upload.
  It was only possible to upload files in a domain+port after opening a popup on firefox. This is now fixed.
* Safari: The loading animation of JPro was stuttering in Safari. This is now fixed.
* Safari: Canvas can no now longer be selected with ctrl+a.

### 2019.2.5 (June 30, 2020)

#### Features
* BoxShadows! JPro now uses box-shadows when possible.
  This dramatically improves the performance of shadows.
* Added the method openURL, openURLAsTab and openURLAsPopup to the WebAPI.
* It's now possible to define an own loading animation as a gif. Just set the attribute `loaderURL` of the JProTag.

#### Bugfixes
* Fixed a bug in the Maven plugin.
  When executing the command `mvn jpro:run` followed by the command `mvn package`, the changes done between the two
  calls were not correctly registered.
* Fixed a bug related to cursor selection with Safari. In some situations the `select- `cursor was wrongly overlying
  text-elements.
* Fixed a bug related to a corner case using the clip property.
* Fixed a regression related to mouse-events. It broke the correct sorting of columns in the TableView.
* Fixed a bug in the attribute `disableShadows` for the JProTag.

### 2019.2.4 (June 2, 2020)

#### Features
* Added the Method `getHeaders` to the WebAPI.
  It makes all the HTTP-Headers provided to the WebSocket connection accessible.
* Reworked Shadows. The new implementation works reliably in all browsers.

#### Bugfixes
* The method `WebAPI.executeScript` no longer serializes the result of the provided javascript code.
  Only `WebAPI.executeScriptWithListener` and `WebAPI.executeScriptWithReturn` are now serializing the result.
  This avoids unnecessary exception due to unserializable results.
* Fixed a rendering bug happening with Chrome.
* Fixed a bug related to HTMLView. In some situations it was not possible to focus elements inside an HTMLView.
* In some situations, the HTMLView couldn't get MouseEvents. This is now fixed.
* Fixed an issue with MouseInput when JPro was embedded into another JProApplication using an HTMLView.
* Fixed a bug with Canvas when using Edge.

### 2019.2.3 (April 20, 2020)

#### Features
* MouseEvents are now also generated while dragging an element outside the Browser.
  This is especially important when dragging the ScrollBar of a ScrollPane. It no longer hangs when leaving the browser.

#### Minors
* Improved the support for JPro being embedded into an iframe.

### Bugfixes
* Dotted lines in Regions are now rendered correctly with the new rendering engine.
* Fixed a bug related to uploading files using the WebAPI.
  It only happened when the server was running on Windows and the client was Edge.
* Fixed TextInput for IPads with the latest version of IPadOS.
* ImageView is now rendered correctly when the image is `null`.
* Fixed a rendering regression in Chrome.
* Fixed a rendering bug in Chrome.
* Fixed a rendering bug related to clip in Safari.

### 2019.2.2 (February 4, 2020)

#### Features
* Gradients, Images and Effects now implemented for Canvas, as well.

#### Minors
* The WebAPI now supports a method (`WebAPI.setLossless(image,false)`) to mark an image which can transferred with a
  lossy compression.

#### Bugfixes
* Fixed a rare rendering bug. In some situations border/background of a region was rendered above its children.
* Fixed a rare rendering bug related to clips. In some situations some browsers didn't render the DOM correctly.
  These situations are now avoided.
* Fixed a rare rendering bug related to clips. In some situations the clip wasn't applied properly.
* Fixed a rendering bug which happened on Chrome when the users zoomed.
* Fixed `fxcontextmenu=true` for the JProTag. It was broken with the new rendering engine.
* Fixed an exception in the browser when rendering a `javafx.scene.text.Text` element with null as the value for the
  text.
* Fixed an exception in the browser when rendering a `javafx.scene.text.Rectangle` with an infinite expansion.
* Fixed the hover property in the FileHandler of the WebAPI.
* Fixed the "image drag problem" on Safari.
* Added a minor performance improvement.

### 2019.2.1 (December 23, 2019)

#### Minors
* Canvas now supports the methods ‘drawImage’ and ‘setGlobalAlpha’.
* We've backport the CSS-performance-improvements done to JavaFX to our JavaFXFork.
* The ByteBuffer of 2d Images are now deleted when no longer needed. It can be deactivated, though, by the following
  line in the jpro.conf: `jpro.deleteBufferOfImage=false`

#### Bugfixes
* Fixed a bug in the Canvas implementation. In some situations, Canvas elements were rendered without necessity.
* Fixed a bug in the gradle plugin.
  When using the command `gradle jproRun`, in some rare situations, two jars files with different javafx-versions were
  added to the classpath.
* Fixed a bug related to the rendering of glyphs with our new rendering engine. The bug occurred when using
  FontAwesomeFX.
* Fixed a behaviour bug which occurred when `userSelect` was set to active. The newline-character is now copied
  correctly with the rest of the text.
* Elements of type ImageView can no longer be selected in the browser. We disabled it because the selection caused an
  unwanted blue effect.

### 2019.2.0 (December 4, 2019)

#### Majors
* Canvas support!
  Canvas requires at least Java(FX) 11.
  Features not yet implemented for Canvas: Gradients, Images and Effects
* New generation rendering engine (with Canvas support)! Performance Improvements for new engine will follow soon!

#### Minors
* Google indexing is now working.
  The problem was, the Google crawler claimed to support WebSocket. But, it took a while until we realized, this was not
  the case. We have now solved it differently.

#### Bugfixes
* Fixed exception for the case that an application was opened without an initial Scene.
* Fixed exception for the case that a WebAPI.requestLayout(Scene) was called with a Window with no Scene attached to it.
* Fixed a bug in the Maven plugin.
  When using the command `mvn jpro:run`, in some rare situations, wrong jars were added to the classpath.

### 2019.1.3 (September 24, 2019)

#### Improvements:
* Major performance-improvement for the javascript-client.

#### Bug fixes:
* Fixed a bug with the SecurityManager in JPro, which prevents `System.exit` to shut down the whole server.
  The bug had the effect, that the new api for Java11 java.net.http` didn't work properly. It probably also affected
  other libraries.
* Fixed a bug with the [FileUploader](/api/2019.1.2/com/jpro/webapi/WebAPI.FileUploader.html).
  There was a problem related to multiple uploads with the same filename. It caused an Exception to be thrown. This is
  now fixed.

### 2019.1.2 (September 11, 2019)

#### Features:
* Added the property `selectedFileSize` to the [FileUploader](/api/2019.1.2/com/jpro/webapi/WebAPI.FileUploader.html).
* Added the attribute [timeUntilReconnect](/?page=docs/current/2.3/EMBEDDING_JPRO) to the JProTag. It specifies after
  how much time the client tries to reconnect when he didn't hear anything from the server.

#### Bugfixes:
* Fixed a performance regression that was introduced in 2019.1.0. It has a significant impact when many nodes are
  serialized in one and the same frame.
* Eliminated the throwing of a superfluous exception. The text of the exception was like the
  following: `Popup cannot be cast to JavaFX.stage.Stage`
* [#29](https://github.com/JPro-one/JPro-tickets/issues/29) Fixed a bug appearing when using JPro with Firefox. When the
  application was not in fullscreen, a node with effects was not rendered.
* Fixed a bug when using the MavenPlugin.
  Maven was downloading the artifacts which are required for the command `mvn jpro:release`,
  even when this command was not called.

### 2019.1.1 (June 17, 2019)

#### Features:
* Added the methods
  [openLocalResource](/api/2019.1.1/com/jpro/webapi/WebAPI.html#openLocalResource-java.lang.String-) and
  [openLocalURL ](/api/2019.1.1/com/jpro/webapi/WebAPI.html#openLocalURL-java.net.URL-) to the
  [WebAPI](/api/2019.1.1/com/jpro/webapi/WebAPI.html).
  They can be used to open files in a new tab, for example pdf-files.

#### Bugfixes
* In the JPro renderer, reduced CPU usage by about 10%. Before this fix JPro could allocate upto 10% of the local CPU at
  time when it should actually be idle.
  Now, no relevant processing power is used when no changes are happening in the scene-graph.
* Fixed a rare bug, when opening two applications at once, sometimes one instance did not start properly.
* Sometimes, after reconnecting, the width or height of the application was wrong. This is now fixed.
* Fixed a bug when changing the Scene of a Stage. Now, the Scene gets properly relayout to the size of the
  JProElement.
* Fixed a rare bug for JavaFX8 which in some rare occasions could cause the text input for a running session to stop
  working.
* Gradle: Fixed a bug in the Gradle plugin, the JavaFX-Fork was added to the compile dependencies.
* Maven: Fixed a bug, which prevented JPro from working when using JavaFX11 and Maven.
* Maven: Fixed the command `mvn jpro:release` for Maven. Now the proper files are added, which are required to run JPro
  on Linux.

### 2019.1.0 (May 28, 2019)

This release by default uses a forked version of JavaFX11.
JPro will still work with JavaFX8, but we recommend switching to Java(FX)11.
Some new JPro features are restricted to JavaFX11 and higher.

**We highly recommend switching to this new release `2019.1.0`,**
because previous versions were using an experimental version for Web Components,
which will not be supported by Chrome much longer.

#### Features for the forked JavaFX11

* JPro now supports the method **snapshot** from Node.
* Drag view and Drag events are now supported in JPro.
  For now, they are restricted to work inside the JavaFX application, they can not interface with other applications.
* When calling the method showAndWait, it no longer leads to a freeze of the JavaFX Thread. It does not mean JPro
  supports showAndWait calls, it just constraints their negative side effects. ShowAndWait calls now throw an exception
  when using JPro.

#### Big features
* For Linux, we now use different default fonts, because the Lucida fonts were removed from the OpenJDK, which resulted
  in various problems during deployment.
  Now they have been replaced by the fonts Roboto, Roboto Mono and Roboto Slab, which are licensed under the Apache
  License 2.0.
* Significant Performance improvements, especially when showing a lot of nodes for the first time. The time needed for
  rendering the initial scene graph was reduced by 30%.
* Updated the API version of the WebComponents.
  We highly recommend updating to this JPro version `2019.1.0`, because the old WebComponents API was experimental and
  will stop working on some browser soon.
* It's now possible to open **new Stages** as new tabs or popups. For this purpose, we've added the
  method `openStageAsPopup(Stage stage)` and `openStageAsTab(Stage stage)` to the WebAPI.
* Added the method `runAfterUpdate` to the WebAPI.
  This method enables for incremental loading of the scene graph, which can be used to improve the user experience.
* Added the method `getWebAPI(Node node, WebAPIConsumer consumer)` to the WebAPI.
  It can be used to access the WebAPI from a node, instead of a stage.
* JPro now supports Java12. New upcoming Java releases will now automatically work when there are no breaking change.

#### Small Features
* The JavaDoc and the source code of the WebAPI are now published as Maven artifacts. This should improve tooling for
  IDEs. The JavaDoc has now got richer content.
* Added the field `JPro build time`, containing a timestamp for the JPro build, to the info block returned by
  the `/status` command.
* Added a new favicon for the internal pages.

#### Bugfixes
* Fixed a rendering issue with Pie Chart. It happened to Regions with (a) width and height property equal zero and (b)
  with a shape.
* `<script>` tags in HTMLView are now being executed.
* Fixed the path for the heap dumps, which were temporarily created when downloading the current heap dump.

## 2018.x.x

### 2018.1.14 (May 15, 2019)

**JPro** fixes:
* JPro works now with the latest version of Java8 `1.8.0_212`.

### 2018.1.13 (April 10, 2019)

**JPro** features:
* The `/status` page as well as the JPro start and opening now lists the
  JavaFX version used.
* Improved the log information produced at JPro start.

**JPro** fixes:
* Mouse events are now generated while dragging a file from outside the
  browser into the JPro session inside the browser.
* Eliminated some rare-throwing of NullPointerExceptions.

**Gradle** and **Maven** Plugin fixes:
* When using `gradle jproRelease` or `mvn jpro:release` on Java11, now by
  default, the JavaFX version 11.0.2 is used.

### 2018.1.12 (March 17, 2019)

**JPro** features:
* The processing of user input, mainly relevant for slow event listeners, was performance optimized. The new behavior is
  closer to the behavior of JavaFX on the desktop.
* Changed the naming-pattern for logfiles. The new pattern is "logs/jpro.$level.log" instead of "
  logs/application.$level.log".

**JPro** fixes:
* The blinking behavior of the Caret in TextFields and TextAreas now behaves like on the desktop. It stops blinking for
  a short time, after typing.
* Key events for TextFields and TextAreas are now consumed as on the desktop. They are consumed when the objects are
  focused. Before this fix, when pressing SPACE in a focused TextField in a ScrollPane, a non-expected scroll down was
  executed.
* The `/status` page has gone through a cleanup regarding names and formatting.
* Fixed a bug on mobile chrome. On a touch, when the event target was removed shortly after touching down, then the
  touch release was not fired.
* Fixed a bug on mobile. On a long press, sometimes a release event was fired without actually releasing.
* Fixed a very rare bug, which could cause the text input to stop working.

### 2018.1.11 (February 11, 2019)

**JPro** features:
* The page `/status` now uses mB instead of kB to display memory usage.

**JPro** fixes:
* Fixed a bug, which had the effect, that JPro didn't start properly with Java 1.8.0_202.
* Fixed a bug, which could happen on iOS devices. It sometimes caused the screen to darken on touch events.
* Fixed a regression from 2018.1.10, the property `userSelect` of the <jpro-app> tag didn't work properly.
* Fixed a regression from 2018.1.10, the file upload when using the WebAPI didn't work properly.

**Gradle** and **Maven** Plugin fixes:
* Files explicitly added to the jproRelease zip, were added as a root element of the zip.
  They are now added as an element of the folder of the application.

### 2018.1.10 (January 7, 2019)

**JPro** features:
* JProRelease: It's now possible, to add additional files, to the zip-file generated by `gradle jproRelease`
  or `mvn jpro:release`.
  It's documented [here](https://www.jpro.one/?page=docs/current/2.2/CONFIGURING_JPRO).

* We added a development mode to the JPro server.
  It is activated, when the server is started from gradle/maven.
  The production mode is activated, when the server is started by a zip, created with `gradle jproRelease`
  or `mvn jpro:release`.
  Pages like `/status` or `/test/appname` are now only accessible **without** username/password, when the development
  mode is active.
* Added a default-page for the JPro-server.
  When the resource `jpro/html/index.html` is unavailable as the path `<servername>/` is opened, the content
  of `test/fullscreen/default` is shown.
  The default `openingPath` for gradle/maven is now `/`.
* Fixed a small memory-leak in the JPro Renderer (the JPro component running inside the browser).

**JPro** fixes:
* The HTMLView, was sometimes still rerendered, when the HTMLView or one of its children was not visible. This is now
  fixed.
* Chrome: When the HTMLView was rendered outside the visible area, sometimes the scrollable height of the page was
  changed.
  This is now fixed.

**Gradle** and **Maven** Plugin fixes:
* (see above) It's now possible to generate files with `gradle jproRelease` and `mvn jpro:release`.add and then to add
  them to the zip.
* Fixed regression in the script `bin/stop.sh`.
  It was not deleting the file `RUNNING_PID`, when the process didn't exist. This is now fixed.

### 2018.1.9 (November 26, 2018)

**JPro** fixes:
* Added some printouts during JPro startup:
  Any errors coming from the javafx initialization process (most
  relevant for running a Linux server in production),
  the JPro-Version,
* Added a warning when a fontfile with the extension `.ttc` is used.
  The warning is important, because `.ttc`-files are not supported in the
  browsers.

* Fixed a regression in 2018.1.8. In 2018.1.8 uploaded files, after a
  certain amount of time, were no longer accessible.

**Gradle** and **Maven** Plugin fixes:
* When creating a release with `gradle jproRelease` or `mvn
  jpro:release`, the script `start.sh` was not supporting spaces for the
  JVM-Arguments. This has now been fixed.

**Maven** Plugin fixes:
* It's no longer required to explicitly add `compile` when using `mvn
  jpro:run` or `package` when using `mvn jpro:release`.
  The new valid command are:
  `mvn jpro:run` and
  `mvn jpro:release`

  We apologize for an error in our previous DOC-page, which until now
  had the wrong syntax; it should have been `mvn package jpro:release` and
  not `mvn compile jpro:release`. But, in any case, as here stated, since
  2018.1.9 this is no longer relevant.

* There was a wrong leading text in the info/status. It was saying
  `GRADLE-Distribution` instead of `MAVEN-Distribution`. This has now
  been fixed.
* As the start.sh file was generated, it had some wrong classpath in
  it, which were referring to files in the development environment.  
  This had no effect, but was confusing and not clean. They are now removed.
* A build created with `mvn jpro:release` for JavaFX11 contained some
  jars, which are not required by JavaFX11. Those have now been eliminated
  for the JavaFX11 build.
* The JVMArgs provided in the `pom.xml` for the JVM were not correctly
  supported. They now are.

### 2018.1.8 (November 12, 2018)

**JPro Supports now Java/JavaFX 11!**

**JPro** fixes:
* The javascript file for JPro is now 35% smaller than the previous one.
* When Setting the attribute `nativescrolling` for the JProTag, it no longer implicitly sets `fxHeight` to true.
* A bug was fixed, that a HTMLView in a Popup was still rendered although the popup was closed.

Fixed for the **Gradle** and for the **Maven** Plugin:
* Added the property `useFontConfig`. The default value is `false`.
  Until now, JPro implicitly disabled the library with the name fontConfig, but with `useFontConfig` the activation of
  fontConfig was made configurable.
* When creating a release with `gradle jproRelease` or `mvn package jpro:release`, whitespaces in the directories are
  now handled correctly.
* When creating a release with `gradle jproRelease` or `mvn package jpro:release`, the start and restart script now
  passes its arguments to the JVM as JVM-arguments.

Fixes for the **Gradle** Plugin:
* Fixed a bug in the gradle-plugin. The command `gradle jproRelease` now makes sure, that when no longer up to date, a
  new jar is generated.

Fixes for the **Maven** Plugin:
* Added the command `mvn package jpro:release` to the maven plugin. The maven plugin now supports all gradle features.
* Fixed a bug in the command `mvn compile jpro:run`, now it behaves the same way as the corresponding gradle command.
  When the file RUNNING.PID still exists, the corresponding process is killed and the file is deleted.

### 2018.1.7 (October 22, 2018)

#### Bugfixes
* On Edge and IE11, the caret of the hidden input-field is no longer visible.
* Added the attribute `printJSCommands`to the JProTag. When true, all js-commands executed through the WebAPI are
  logged on the browser console.
  Documented in the chapter [EMBEDDING_JPRO](https://www.jpro.one/?page=docs/current/2.3/EMBEDDING_JPRO).
* Small reduction of the js-file by about 15%.
* A regression on mobile/iOS related to the text-input.
* A bug was fixed on the IE11, that sometimes image resources were not loaded correctly.
* A bug was fixed, that a font could not be loaded when it’s path contained a '+'-sign.

### 2018.1.6 (October 1, 2018)

#### Bugfixes
* A memory-leak could happen inside of JavaFX, when a node was removed from the scene-graph while the rendering was
  disabled. The memory is now released when the related node is no longer used, instead of at the end of a session.
* The MouseEvent methods `isAltDown()`, `isControlDown()`, `isMetaDown()` and `isShiftDown()` did not return the correct
  value and were now fixed.
* The field `Views afk` is now added to the URL `<servername>/status`. A view is treated as afk, when there was no
  user-input during a period of one minute.
* A new property was added to the `jpro.conf` to allow for logging all access to any files located under the resource
  path `jpro/html`. It is activated by adding the `jpro.logResourceAccess = true` to the `jpro.conf`.
* A new property was added to the `jpro.conf` to allow for logging user input events. It is activated by adding
  the `jpro.logUserInputEvents = true` to the `jpro.conf`.
* On iOS Safari it could happen, that touch generated unwanted double click events. This bug has now been fixed.
* On iOS Safari it could happen, that touch events got lost. This bug has now been fixed.
* Under special conditions it could happen, that ImageView did not render correctly. It could happen with
  backgroundLoading set to true. This bug has now been fixed.
* When using the WebAPI for downloading files, some file types used to open in the current tab. This has now been
  changed. Now all files are downloaded directly.
* We now use GZip for the download of all javascript files. This greatly increases the performance for initial
  sessions (before the browsers starts caching it).

Fixes for the **Gradle** Plugin:
* The value of `deployment` at `<servername>/status` when using `gradle jproStart` or `gradle jproRestart` was wrong.
  This bug has now been fixed.

Fixes for the **Maven** Plugin:
* A `NullPointerException` could be generated when calling `mvn compile jpro:stop`. This bug has now been fixed.

### 2018.1.5 (July 23, 2018)

* Maven : Added an experimental version for a
  MavenPlugin. [Check out the Maven-HelloJPro!](https://github.com/jpro-one/HelloJPro-Maven)
* WebAPI: Fixed fil-upload through file-drop in FireFox.
* WebAPI: Fixed the behaviour for fileHover.
* JPro  : Fixed the performance of `Node.setClip`. The implementation is now fast and correct.
* JPro  : Fixed a rare exception in the javascript-client.
* JPro  : Added information about the deployment-method (`gradle jproRun`, `gradle jproDist`, `mvn compile jpro:run`)
  to `<servername>/status`.
* GRADLE: Fixed a bug in `gradle jproRun`. The server now always terminates after stopping gradle.

### 2018.1.4 (July 3, 2018)

* JPro  : Improved rendering when using `Region.setShape`.
* JPro  : Fixed a rendering-issue related to `MediaView` without a `MediaPlayer`.
* JPro  : Fixed some issues when using `Node.setClip`. This fix especially fixes the behaviour of the
  JFoenix-class `JFXButton`.
* JPro  : Fixed wrong rendering, when `Node.setRotate` is used in combination with `Node.setScaleX` or `Node.setScaleY`.
* JPro  : Significant performance-improvements for SVGPath for both the server and the browser.
* JPro  : Significant performance-improvements in the browser.
* JPro  : Fixed regression from the version 2018.1.3, which could crash a session in the Internet Explorer / Edge.
* JPro  : Fixed js-exception in Internet Explorer / Edge. This improves the behaviour on Internet Explorer / Edge.
* JPro  : Fixed DropShadow sometimes being cut-off.
* JPro  : Added workaround for side effects by adding a listener to the layoutBounds of a Group.
* JPro  : Fixed positioning of Popups with Shadows.
* JPro  : Fixed positioning of Popups without autofix.
* JPro  : Fixed rare race-condition in the rendering, which could have the effect of a temporary memory-leak.

### 2018.1.3 (June 4, 2018)

* JPro  : Significant performance improvements. The SVG-Dom is now much smaller.
  Region is now painted faster, which especially benefits large business-applications.
* JPro  : Fixed screen-mouse-position for various events.
* JPro  : Added a workaround for a rare bug, related to wrong popup-positions.
  It is activated by default. It can be deactivated in jpro.conf with the following
  statement: `jpro.workaroundWindowPosition = false`.
* JPro  : Fixed bug related to HTMLView, under some conditions the visible-attribute wasn't updated correctly.
* JPro  : Fixed a rare bug related to TextFlow, which breaks the rendering and causes a NullPointerException.
* JPro  : Fixed a bug, which caused too many MouseEnter/MouseLeave events.
* JPro  : Fixed a race-condition, which had the effect, that the JavaFX-Context-Menu isn't shown.
* JPro  : When resizing the jpro-tag in the browser, the stage of the javafx-app is now also resized (previously only
  the scene was resized).
* JPro  : TextFlow with Nodes with `managed = false` are now rendered correctly.
* JPro  : Updated to Play Framework 2.6.13.
* JPro  : Added more information to `/status`.
* JPro  : Fixed a bug, where the logfiles couldn't be accessed in the browser, when the server had an uncommon
  default-encoding.
* JPro  : Added a workaround for a small memory-leak in JavaFX. It can be deactivated in the jpro.conf by the following
  line: `jpro.gcWorkaroundStage = false`.

### 2018.1.2 (April 19, 2018)

* ***Added Support for Java10!***
* JPro  : When browser is resized fast and the server cannot catch up, the outdated resizing is now skipped.
* JPro  : fixed wrong clipping, when using the jpro-tag-attribute: scaling
* JPro  : Fixed an exception in the browser related to HTMLView and SVGView. They didn't have any symptoms
* JPro  : added error-message, when initialization of jpro doesn't terminate
* GRADLE: Fixed `jproRelease` on a clean build
* GRADLE: For the tasks `jproStart` and `jproRestart`, The console-output of jpro-process is now printed, until the port
  is opened
* GRADLE: On startup-failure, the exit-code is no longer printed in a loop
* WebAPI: fileDragOver is also updated, when selectFileOnDrop is not true
* WebAPI: added the property `selectFileOnDrop` to `FileHandler`, previously it was implied by `electFileOnClick

### 2018.1.1 (April 4, 2018)

* Improved error-message, when a file couldn't be found in the package jpro/html
* Fixed a bug, which can cause wrong event-positions. This was likely to happen in combination with ScrollPane
* Fixed Double-Clicks on mobile browsers
* Reworked how `WebView` and HTMLView` is working, it's implementation is now much simpler
* WebAPI: Fixed bug in `FileHandler`, uploading the same file twice now works properly
* WebAPI: Improved FileHandler, there is now an `onFileSelected`-event, and a new property `fileDragOver`

### 2018.1.0 (March 13, 2018)
