# Project Documentation Context

This file provides a combined overview of all available library documentation.
For full details on any library, read its DOCUMENTATION.md file.

## SANDEC:jnodes:0.8.3
Sources: SANDEC/jnodes/sources-index.md


## com.typesafe:config:1.4.3
configuration library for JVM languages using HOCON files
[Homepage](https://github.com/lightbend/config) · [Repository](https://github.com/lightbend/config) · Apache-2.0
Sources: com.typesafe/config/sources-index.md


## one.jpro.platform:jpro-routing-core:0.5.8 (JPro Routing Core)
A framework for building JPro/JavaFX applications, both desktop and web, with routing capabilities.
[Homepage](https://www.jpro.one) · [Repository](https://github.com/JPro-one/jpro-platform/tree/main/core) · Apache License, Version 2.0
Full docs: one.jpro.platform/jpro-routing-core/DOCUMENTATION.md
Sources: one.jpro.platform/jpro-routing-core/sources-index.md

### Chapters
- JPro Routing (lines 1-186)
  - Introduction (lines 3-32) — JPro Routing is a framework
  - Advantages (lines 33-52) — With these basic types, we get a lot of features:
  - Hello World (lines 53-72) — Checkout our sample project for a small application showing the features of JPro Routing:
  - Basic API (lines 73-114)
  - Setting Links (lines 115-146) — You usually want to use a link to switch from one page to another.
  - Fullscreen and Scrolling (lines 147-159) — It is possible to configure a page to be either fullscreen or scrollable.
  - History API and defaultpage (lines 160-169) — When Routing is used in a browser, the history API is used to navigate between pages.
  - Additional Features (lines 170-186)

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


## one.jpro:jpro-webapi:2025.3.1 (JPro Web API)
A module that serves as an interface to access JPro server-side functionalities.
[Homepage](https://www.jpro.one) · [Repository](https://github.com/Sandec/jpro/tree/main/webapi) · Apache License, Version 2.0
Full docs: one.jpro/jpro-webapi/DOCUMENTATION.md
Sources: one.jpro/jpro-webapi/sources-index.md
Changelog: one.jpro/jpro-webapi/changelog-overview.md

### Chapters
- Getting Started (lines 1-682)
  - Creating a project (lines 4-298) — The easiest way to set up a new JPro project for your apps is to use the **HelloJPro** project as your base,
    - Starting an app from the index.html (lines 13-48) — The `index.html` file from the HelloJPro project looks like the following:
    - jpro.conf (lines 49-110) — The `jpro.conf` of the HelloJPro project can be downloaded
    - Starting an app from Gradle (lines 111-186) — 1. The `settings.gradle` file of the HelloJPro project can be downloaded
    - Starting an app from Maven (lines 187-298) — The `pom.xml` file of the HelloJPro project can be downloaded
  - Run JPro locally (lines 299-551)
    - Using Gradle (lines 301-397) — The simplest way to begin is by using **Gradle** as your build tool.
    - Using Maven (lines 398-551) — JPro provides a plugin for Maven, which allows you to easily start JPro from an existing project,
      - Step `1`. Install Maven (lines 428-432) — Maven can be downloaded and installed [here](https://maven.apache.org/).
      - Step `2`. Create the Maven script (lines 433-539) — Create the file `pom.xml` and put it into your **project's root directory**.
      - Step `3`. Run the app (lines 540-551) — Start a **Terminal session**, move to the **main project directory** and enter the command:
  - Run JPro remotely (lines 552-628)
  - PC as a JPro server (lines 629-682) — After testing your app through localhost, a next practical step could be to make your PC host a JPro server
- JPro Docs (lines 683-1585)
  - JPro commands (lines 686-772)
  - Configuring JPro (lines 773-964)
    - build.gradle and pom.xml (lines 775-888) — The Gradle plugin is configured under the `jpro` tag of `build.gradle`.
    - jpro.conf (lines 889-952) — The following properties can be set in the `jpro.conf`:
    - Changing the working directory after creating a release (lines 953-964) — We can change the working directory of a JPro application in different ways after creating a release by either calling
  - Embedding JPro (lines 965-1090) — JPro can be **embedded into an existing html-page** by using the tag `<jpro-app>`.
  - The WebAPI (lines 1091-1149) — The main purpose of the WebAPI is to let you create individual Javascript code for the browser,
  - Custom HTTP Handlers (lines 1150-1194) — JPro allows you to attach custom request handlers to your website. To do so you have to add a request handler to the ...
  - Debugging and testing (lines 1195-1262) — The following tags can be added to the original URL of your JPro server.
  - Deploying JPro (lines 1263-1278)
  - Linux & Docker (lines 1279-1353)
  - Apache2 (lines 1354-1396) — We usually recommend using Nginx, but JPro can also be used with Apache as a webserver.
  - SSL by using NGINX (lines 1397-1447) — The setup defined below is tested with the nginx package contained in ubuntu 16.04.
  - Close Instance Strategy (lines 1448-1492) — This documentation describes the JPro Close Instance Strategy configuration structure and explains each field in the ...
  - JPro screensharing (lines 1493-1528) — Set up your app to be shared among external users.
  - Logging (lines 1529-1585)
- JPro Loadbalancer (lines 1586-1782)
  - Overview (lines 1588-1630) — The JPro Loadbalancer allows you to run multiple JPro Servers in parallel.
  - Usage (lines 1631-1659)
  - Configuration (lines 1660-1742)
  - Windows Service (lines 1743-1782) — To use the [Windows service wrapper](https://github.com/winsw/winsw), we have to do 2 things as a preparation.
- Additional Information (lines 1783-1893)
- Resources (lines 1894-1934)

## org.jetbrains:annotations:26.1.0 (JetBrains Java Annotations)
A set of annotations used for code inspection support and code documentation.
[Homepage](https://github.com/JetBrains/java-annotations) · [Repository](https://github.com/JetBrains/java-annotations) · The Apache Software License, Version 2.0
Sources: org.jetbrains/annotations/sources-index.md


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


