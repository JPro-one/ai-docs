# Project Documentation Context

This file provides a combined overview of all available library documentation.
For full details on any library, read its DOCUMENTATION.md file.

## com.typesafe:config:1.4.5
configuration library for JVM languages using HOCON files
[Homepage](https://github.com/lightbend/config) · [Repository](https://github.com/lightbend/config) · Apache-2.0
Sources: com.typesafe/config/sources-index.md


## one.jpro.aidocs:ai-docs-gradle-plugin:0.1.0-SNAPSHOT
A Gradle plugin that collects `DOCUMENTATION.md` artifacts from your project's dependencies and organizes them into an AI-navigable file structure....
Full docs: one.jpro.aidocs/ai-docs-gradle-plugin/DOCUMENTATION.md

### Chapters
- AI Docs Gradle Plugin (186 lines 1-186) — A Gradle plugin that collects `DOCUMENTATION.md` artifacts from your project's dependencies and organizes them into a...
  - Using the Plugin (80 lines 5-84) — How to apply the plugin, run it, configure it, and navigate its output.
  - Publishing Documentation for Your Library (102 lines 85-186) — How library authors can make their documentation discoverable by the plugin.

## one.jpro.platform:jpro-file:0.6.0-SNAPSHOT (JPro File)
A module for handling file related operations in JPro/JavaFX applications running natively and via JPro server.
[Homepage](https://www.jpro.one) · [Repository](https://github.com/JPro-one/jpro-platform/tree/main/jpro-file) · Apache License, Version 2.0
Full docs: one.jpro.platform/jpro-file/DOCUMENTATION.md
Sources: one.jpro.platform/jpro-file/sources-index.md

### Chapters
- JPro File (125 lines 1-125)

## one.jpro.platform:jpro-platform:0.6.0-SNAPSHOT (JPro Platform)
JPro Platform is a collection of modules that allows you to create cross-platform JavaFX application applications that also run in the browser.
[Homepage](https://www.jpro.one) · [Repository](https://github.com/JPro-one/jpro-platform/tree/main/jpro-platform) · Apache License, Version 2.0
Full docs: one.jpro.platform/jpro-platform/DOCUMENTATION.md
Changelog: one.jpro.platform/jpro-platform/changelog-overview.md

### Chapters
- JPro Platform (541 lines 1-541) — ![Build](https://github.com/jpro-one/jpro-platform/actions/workflows/main.yml/badge.svg)
    - Key Features: (9 lines 10-18) — * **Modular Components**: Begin your development journey with an assortment of pre-configured modules designed to
    - Benefits: (32 lines 19-50) — * **Unified Development Paradigm**: JPro provides a cohesive development environment, ensuring consistent behavior
  - JPro Auth (27 lines 51-77) — Rely on this library to add sophisticated authentication and authorization to your **JPro/JavaFX** applications.
  - JPro SceneGraph (21 lines 78-98) — Creates human and AI friendly String representations of JavaFX SceneGraphs.
  - JPro File (41 lines 99-139) — This library provides a simple way to pick, drop, upload and download files in **JPro/JavaFX** applications.
  - JPro Image Manager (24 lines 140-163) — This library makes very easy to manage the process of loading and caching images, allowing efficient retrieval
  - JPro Mail (22 lines 164-185) — This library provides a simple way to send emails in **JPro/JavaFX** applications. It allows you to send emails
  - JPro Media (37 lines 186-222) — This library is designed for audio and video playback and recording within JavaFX applications.
  - JPro Routing (65 lines 223-287) — A minimalistic routing library for **JPro/JavaFX** applications. It allows you to define routes and to navigate between
  - JPro Markdown (22 lines 288-309) — Formerly known as MDFX, this library allows you to render Markdown formatted content in your **JPro/JavaFX**
  - JPro Session (23 lines 310-332) — This library provides a simple implementation of a session manager for **JavaFX/JPro** applications.
  - JPro Utils (21 lines 333-353) — This library offers essential tools for various functionalities to enhance the development of **JPro/JavaFX** applica...
  - JPro WebRTC (29 lines 354-382) — This library provides an API to use WebRTC in **JPro/JavaFX** applications. This technology allows for the direct
  - JPro YouTube (39 lines 383-421) — This library makes it easy to embed a YouTube video in your **JPro/JavaFX** applications. It provides a simple API
  - JPro Dynamic CSS (21 lines 422-442) — Reactive dynamic CSS for JavaFX scenes and parents. Apply CSS strings at runtime without external stylesheet files.
  - JPro FlexBox (23 lines 443-465) — A CSS FlexBox layout implementation for JavaFX. Brings the familiar CSS Flexbox model to JavaFX with support for
  - JPro HTML Scrollpane (21 lines 466-486) — Provides a skin implementation of a scrollpane for **JPro** applications only.
  - Launch the examples (55 lines 487-541) — To run the examples, you can use the following commands:
- JPro Auth (275 lines 542-816)
  - Introduction (11 lines 544-554) — Rely on this library to add sophisticated authentication and authorization to your **JPro/JavaFX**. At the core, it
  - Features (9 lines 555-563) — - Authentication Simplified
  - Architecture Overview (71 lines 564-634) — 1. Basic concepts
  - Getting Started (182 lines 635-816)
    - Configuration (45 lines 637-681) — The `jpro-auth-core` module contains the core classes and interfaces to implement authentication and authorization
    - Usage (28 lines 682-709) — - The following example shows how to authenticate a user with a username and password using the
    - Combined with the Routing API (72 lines 710-781) — By simply adding the `jpro-auth-routing` dependency to your project, the authentication process can be simplified even
    - Launch the examples (35 lines 782-816) — **Basic login sample**
- JPro Dynamic CSS (62 lines 817-878) — Apply CSS strings to JavaFX scenes and parents at runtime. Each call replaces the previous CSS for that target — temp...
- JPro File (127 lines 879-1005)
- JPro FlexBox (124 lines 1006-1129) — A CSS FlexBox layout implementation for JavaFX. All properties are styleable via CSS.
- JPro Media (539 lines 1130-1668) — JPro Media is a Java Library for playing and recording audio and video files in JavaFX applications that run
  - Features (7 lines 1136-1142) — - Write Once Run Anywhere (the same code runs on desktop/mobile devices and in the web browser)
  - Supported Platforms (9 lines 1143-1151) — - Web (via [JPro](https://www.jpro.one))
  - Getting Started (161 lines 1152-1312) — To get started with JPro Media, we need to add the following configuration to your project.
    - Gradle (39 lines 1154-1192) — 1. In `plugin` section, we need to add the `org.bytedeco.gradle-javacpp-platform` plugin in order to select from
    - Maven (39 lines 1193-1231) — 1. Add `jpro-media` and `javacv-platform` dependencies to the `pom.xml` file.
    - Platform specific modules configuration (71 lines 1232-1302) — When we run the application on the desktop/device, the following modules are required and must be added the module
    - Similarities and differences to JavaFX Media (10 lines 1303-1312) — - The `MediaSource` class is very similar to the `Media` class from the JavaFX Media API.
  - Usage (168 lines 1313-1480)
    - Media Player API (76 lines 1314-1389) — For the playback functionality, the JPro Media API is very similar to the JavaFX Media API.
    - Media Recorder API (88 lines 1390-1477) — The JPro Media also provides a MediaRecorder API for recording the audio and video stream from a camera device.
    - More examples (3 lines 1478-1480) — For more examples, please take a look at the [JPro Media Examples](https://github.com/JPro-one/jpro-platform/tree/mai...
  - JPro Routing (188 lines 1481-1668)
    - Introduction (30 lines 1483-1512) — JPro Routing is a framework
    - Advantages (20 lines 1513-1532) — With these basic types, we get a lot of features:
    - Hello World (20 lines 1533-1552) — Checkout our sample project for a small application showing the features of JPro Routing:
    - Basic API (42 lines 1553-1594)
    - Setting Links (32 lines 1595-1626) — You usually want to use a link to switch from one page to another.
    - Fullscreen and Scrolling (13 lines 1627-1639) — It is possible to configure a page to be either fullscreen or scrollable.
    - History API and defaultpage (10 lines 1640-1649) — When Routing is used in a browser, the history API is used to navigate between pages.
    - Additional Features (19 lines 1650-1668)

## one.jpro.platform:jpro-routing-core:0.6.0-SNAPSHOT (JPro Routing Core)
A framework for building JPro/JavaFX applications, both desktop and web, with routing capabilities.
[Homepage](https://www.jpro.one) · [Repository](https://github.com/JPro-one/jpro-platform/tree/main/core) · Apache License, Version 2.0
Full docs: one.jpro.platform/jpro-routing-core/DOCUMENTATION.md
Sources: one.jpro.platform/jpro-routing-core/sources-index.md

### Chapters
- JPro Routing (186 lines 1-186)
  - Introduction (30 lines 3-32) — JPro Routing is a framework
  - Advantages (20 lines 33-52) — With these basic types, we get a lot of features:
  - Hello World (20 lines 53-72) — Checkout our sample project for a small application showing the features of JPro Routing:
  - Basic API (42 lines 73-114)
  - Setting Links (32 lines 115-146) — You usually want to use a link to switch from one page to another.
  - Fullscreen and Scrolling (13 lines 147-159) — It is possible to configure a page to be either fullscreen or scrollable.
  - History API and defaultpage (10 lines 160-169) — When Routing is used in a browser, the history API is used to navigate between pages.
  - Additional Features (17 lines 170-186)

## one.jpro.platform:jpro-scenegraph:0.6.0-SNAPSHOT (JPro Scenegraph)
A module that serializes a scene graph to a string representation making it both human and AI friendly.
[Homepage](https://www.jpro.one) · [Repository](https://github.com/JPro-one/jpro-platform/tree/main/jpro-scenegraph) · Apache License, Version 2.0
Sources: one.jpro.platform/jpro-scenegraph/sources-index.md


## one.jpro.platform:jpro-utils:0.6.0-SNAPSHOT (JPro Utils)
A utility module offering essential tools for various functionalities to enhance the development of **JPro/JavaFX** applications
[Homepage](https://www.jpro.one) · [Repository](https://github.com/JPro-one/jpro-platform/tree/main/jpro-utils) · Apache License, Version 2.0
Sources: one.jpro.platform/jpro-utils/sources-index.md


## one.jpro:jmemorybuddy:0.5.6 (JMemoryBuddy)
A library usefull for unit testing memory leaks
[Homepage](https://github.com/Sandec/JMemoryBuddy) · [Repository](https://github.com/Sandec/JMemoryBuddy) · The Apache License, Version 2.0
Sources: one.jpro/jmemorybuddy/sources-index.md


## one.jpro:jpro-webapi:2026.1.1 (JPro Web API)
A module that serves as an interface to access JPro server-side functionalities.
[Homepage](https://www.jpro.one) · [Repository](https://github.com/Sandec/jpro/tree/main/webapi) · Apache License, Version 2.0
Sources: one.jpro/jpro-webapi/sources-index.md


## one.jpro:jpro:2026.1.1 (JPro)
Additional JPro resources
[Homepage](https://www.jpro.one) · [Repository](https://github.com/Sandec/jpro/tree/main) · Apache License 2.0
Full docs: one.jpro/jpro/DOCUMENTATION.md
Changelog: one.jpro/jpro/changelog-overview.md

### Chapters
- Getting Started (299 lines 1-299)
  - Overview (48 lines 3-50) — JPro allows you to run your JavaFX applications directly in the browser - no rewriting required. Your app runs on the...
  - Gradle setup (99 lines 51-149) — The simplest way to begin is by using **Gradle** as your build tool.
  - Maven setup (114 lines 150-263) — JPro provides a plugin for Maven, which allows you to easily start JPro from an existing project.
  - Build commands (36 lines 264-299) — The `gradle` & `maven` JPro plugins provide simple commands to **run your application locally** during development as...
- Using JPro (486 lines 300-785)
  - Build configuration (100 lines 302-401) — JPro properties below are available in both Gradle & Maven.
  - JPro config (52 lines 402-453) — The following properties can be set in the `jpro.conf` file:
  - HTTP routes & resources (71 lines 454-524)
  - JavaFX node attributes (31 lines 525-555) — In some cases, you may wish to override certain behavior for a specific element or area of your application instead o...
  - Instance close strategies (46 lines 556-601) — This documentation describes the structure for configuring the close instance strategies JPro should use under differ...
  - Debug utilities (90 lines 602-691) — JPro servers provide a variety of utilities that can be useful for debugging or for system administrators to monitor ...
  - Logging (54 lines 692-745) — JPro provides the following a set of structured log messages out of the box:
  - Tips & Limitations (40 lines 746-785)
- Web Integration (170 lines 786-955)
  - Custom index.html (56 lines 788-843) — To make your JPro application accessible via URL, an “index.html” needs to exist in the project structure under `src/...
  - Embedding JPro (59 lines 844-902) — JPro can be **embedded into an existing html-page** by using the  `<jpro-app>` tag, similar to how it is included in ...
  - WebAPI Overview (53 lines 903-955) — The WebAPI enables you to create custom JavaScript code that interoperates with JPro. It also allows you to make use ...
- Deployment (200 lines 956-1155)
  - Deploying on Linux (33 lines 958-990)
  - Docker templates (74 lines 991-1064) — The following templates can be used as a reference and adjusted to your needs.
  - Nginx (48 lines 1065-1112) — In order to configure nginx for JPro, create & add the following content to `/etc/nginx/sites-enabled/jproconf.nginx....
  - Apache2 (43 lines 1113-1155) — While we typically recommend using nginx, JPro can also be used with Apache. In this case, Apache is used as a revers...
- JPro Loadbalancer (183 lines 1156-1338)
  - Overview & usage (62 lines 1158-1219) — The JPro Loadbalancer allows you to run multiple JPro Servers in parallel.
  - Configuration (80 lines 1220-1299)
  - Windows service (39 lines 1300-1338) — To use the [Windows service wrapper](https://github.com/winsw/winsw), we have to do 2 things as a preparation.
- QF-Test Integration (72 lines 1339-1410)

## org.jetbrains:annotations:26.1.0 (JetBrains Java Annotations)
A set of annotations used for code inspection support and code documentation.
[Homepage](https://github.com/JetBrains/java-annotations) · [Repository](https://github.com/JetBrains/java-annotations) · The Apache Software License, Version 2.0
Sources: org.jetbrains/annotations/sources-index.md


## org.scala-lang:scala-library:2.13.18 (Scala Library)
Standard library for the Scala Programming Language
[Homepage](https://www.scala-lang.org/) · [Repository](https://github.com/scala/scala) · Apache-2.0
Sources: org.scala-lang/scala-library/sources-index.md


## org.scala-lang:scala-reflect:2.13.18 (Scala Reflect)
Reflection Library for the Scala Programming Language
[Homepage](https://www.scala-lang.org/) · [Repository](https://github.com/scala/scala) · Apache-2.0
Sources: org.scala-lang/scala-reflect/sources-index.md


## org.slf4j:slf4j-api:2.0.17 (SLF4J API Module)
The slf4j API
[Homepage](http://www.slf4j.org)
Sources: org.slf4j/slf4j-api/sources-index.md


