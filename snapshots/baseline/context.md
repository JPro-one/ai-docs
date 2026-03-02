# Project Documentation Context

This file provides a combined overview of all available library documentation.
For full details on any library, read its DOCUMENTATION.md file.

## one.jpro.platform:jpro-file:0.5.8
A library for handling file related operations (like open, save and drag & drop) independently of the running platform,
Full docs: one.jpro.platform/jpro-file/DOCUMENTATION.md

### Chapters
- JPro File (125 lines 1-125)
  - Introduction (82 lines 3-84) — A library for handling file related operations (like open, save and drag & drop) independently of the running platform,

## one.jpro.platform:jpro-routing-core:0.5.8
JPro Routing is a framework
Full docs: one.jpro.platform/jpro-routing-core/DOCUMENTATION.md

### Chapters
- JPro Routing (186 lines 1-186)
  - Introduction (30 lines 3-32) — JPro Routing is a framework
  - Basic API (42 lines 73-114)
  - Setting Links (32 lines 115-146) — You usually want to use a link to switch from one page to another.

## one.jpro:jpro:2026.1.1
JPro allows you to run your JavaFX applications directly in the browser - no rewriting required. Your app runs on the server, and the browser displ...
Full docs: one.jpro/jpro/DOCUMENTATION.md

### Chapters
- Getting Started (299 lines 1-299)
  - Overview (48 lines 3-50) — JPro allows you to run your JavaFX applications directly in the browser - no rewriting required. Your app runs on the...
  - Gradle setup (99 lines 51-149) — The simplest way to begin is by using **Gradle** as your build tool.
    - `2` Create the Gradle scripts (70 lines 70-139) — Create the file `settings.gradle` and put it into your **project’s root directory**.
  - Maven setup (114 lines 150-263) — JPro provides a plugin for Maven, which allows you to easily start JPro from an existing project.
    - `2` Create the Maven script (94 lines 160-253) — Create the file `pom.xml` and put it into your **project’s root directory**.
  - Build commands (36 lines 264-299) — The `gradle` & `maven` JPro plugins provide simple commands to **run your application locally** during development as...
- Using JPro (486 lines 300-785)
  - Build configuration (100 lines 302-401) — JPro properties below are available in both Gradle & Maven.
    - Example `pom.xml` configuration (37 lines 365-401) — Note how for maven, the `version` is set directly under `<plugin>` while other properties are set under the `<configu...
  - JPro config (52 lines 402-453) — The following properties can be set in the `jpro.conf` file:
  - HTTP routes & resources (71 lines 454-524)
    - Custom HTTP handlers (45 lines 480-524) — JPro allows you to add custom request handlers to the server API:
  - JavaFX node attributes (31 lines 525-555) — In some cases, you may wish to override certain behavior for a specific element or area of your application instead o...
  - Instance close strategies (46 lines 556-601) — This documentation describes the structure for configuring the close instance strategies JPro should use under differ...
    - Configuration Structure (40 lines 562-601) — JPro’s default configuration is structured into three optional strategies: `short`, `medium`, and `long`. Each strate...
  - Debug utilities (90 lines 602-691) — JPro servers provide a variety of utilities that can be useful for debugging or for system administrators to monitor ...
  - Logging (54 lines 692-745) — JPro provides the following a set of structured log messages out of the box:
  - Tips & Limitations (40 lines 746-785)
- Web Integration (170 lines 786-955)
  - Custom index.html (56 lines 788-843) — To make your JPro application accessible via URL, an “index.html” needs to exist in the project structure under `src/...
    - Index.html Structure (37 lines 794-830) — As an example, let’s look at the `index.html` file from the HelloJPro project:
  - Embedding JPro (59 lines 844-902) — JPro can be **embedded into an existing html-page** by using the  `<jpro-app>` tag, similar to how it is included in ...
    - Adjusting behavior with `<jpro-app>` tag attributes (32 lines 871-902) — The following attributes can be added to the HTML tag in order to customize how your app behaves when loaded.
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
  - The JPro Integration Library - jpro-utils.qft (71 lines 1340-1410) — The **jpro-utils.qft** library enables QF-Test to run automated tests directly

