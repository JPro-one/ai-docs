# Project Documentation Context

This file provides a combined overview of all available library documentation.
For full details on any library, read its DOCUMENTATION.md file.

## SANDEC:jnodes:0.8.3
Sources: SANDEC/jnodes/sources-index.md


## com.typesafe:config:1.4.3
configuration library for JVM languages using HOCON files
[Homepage](https://github.com/lightbend/config) · [Repository](https://github.com/lightbend/config) · Apache-2.0
Sources: com.typesafe/config/sources-index.md


## one.jpro.platform:jpro-file:0.5.8 (JPro File)
A module for handling file related operations in JPro/JavaFX applications running natively and via JPro server.
[Homepage](https://www.jpro.one) · [Repository](https://github.com/JPro-one/jpro-platform/tree/main/jpro-file) · Apache License, Version 2.0
Full docs: one.jpro.platform/jpro-file/DOCUMENTATION.md
Sources: one.jpro.platform/jpro-file/sources-index.md

### Chapters
- JPro File (125 lines 1-125)

## one.jpro.platform:jpro-routing-core:0.5.8 (JPro Routing Core)
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

## one.jpro.platform:jpro-scenegraph:0.5.8 (JPro Scenegraph)
A module that serializes a scene graph to a string representation making it both human and AI friendly.
[Homepage](https://www.jpro.one) · [Repository](https://github.com/JPro-one/jpro-platform/tree/main/jpro-scenegraph) · Apache License, Version 2.0
Sources: one.jpro.platform/jpro-scenegraph/sources-index.md


## one.jpro.platform:jpro-utils:0.5.8 (JPro Utils)
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

## org.scala-lang:scala-library:2.12.18 (Scala Library)
Standard library for the Scala Programming Language
[Homepage](https://www.scala-lang.org/) · [Repository](https://github.com/scala/scala) · Apache-2.0
Sources: org.scala-lang/scala-library/sources-index.md


## org.scala-lang:scala-reflect:2.12.18 (Scala Reflect)
Reflection Library for the Scala Programming Language
[Homepage](https://www.scala-lang.org/) · [Repository](https://github.com/scala/scala) · Apache-2.0
Sources: org.scala-lang/scala-reflect/sources-index.md


## org.slf4j:slf4j-api:2.0.17 (SLF4J API Module)
The slf4j API
[Homepage](http://www.slf4j.org)
Sources: org.slf4j/slf4j-api/sources-index.md


