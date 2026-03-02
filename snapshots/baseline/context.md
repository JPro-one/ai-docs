# Project Documentation Context

This file provides a combined overview of all available library documentation.
For full details on any library, read its DOCUMENTATION.md file.

## one.jpro.platform:jpro-file:0.5.8
A library for handling file related operations (like open, save and drag & drop) independently of the running platform,
Full docs: one.jpro.platform/jpro-file/DOCUMENTATION.md

### Chapters
- JPro File (lines 1-125)
  - Introduction (lines 3-84) — A library for handling file related operations (like open, save and drag & drop) independently of the running platform,
      - Limitations (lines 80-84) — Some combination of features only work limited. These are the following:
  - Installation (lines 85-104) — Add the following configuration to your project based on the build tool you are using:
  - Launch the examples (lines 105-125) — [**Text Editor sample**](https://github.com/JPro-one/jpro-platform/blob/main/jpro-file/example/src/main/java/one/jpro...

## one.jpro.platform:jpro-routing-core:0.5.8
JPro Routing is a framework
Full docs: one.jpro.platform/jpro-routing-core/DOCUMENTATION.md

### Chapters
- JPro Routing (lines 1-186)
  - Introduction (lines 3-32) — JPro Routing is a framework
  - Advantages (lines 33-52) — With these basic types, we get a lot of features:
  - Hello World (lines 53-72) — Checkout our sample project for a small application showing the features of JPro Routing:
  - Basic API (lines 73-114)
    - Route (lines 75-93) — Route static methods:
    - Response (lines 94-103) — Response static methods:
    - Request (lines 104-114) — Methods of Request:
  - Setting Links (lines 115-146) — You usually want to use a link to switch from one page to another.
  - Fullscreen and Scrolling (lines 147-159) — It is possible to configure a page to be either fullscreen or scrollable.
  - History API and defaultpage (lines 160-169) — When Routing is used in a browser, the history API is used to navigate between pages.
  - Additional Features (lines 170-186)
    - Incremental Loading (lines 172-186) — It's possible to load parts of the application incrementally.

## one.jpro:jpro:2026.1.1
JPro allows you to run your JavaFX applications directly in the browser - no rewriting required. Your app runs on the server, and the browser displ...
Full docs: one.jpro/jpro/DOCUMENTATION.md

### Chapters
- Getting Started (lines 1-40)
  - Overview (lines 3-40) — JPro allows you to run your JavaFX applications directly in the browser - no rewriting required. Your app runs on the...
    - Key Points (lines 11-24) — > **Server-side runtime**
    - Prerequisites (lines 25-35) — Before you start, ensure you have:
    - Hello World (lines 36-40) — The quickest way to get started is the [Hello World for Gradle](https://github.com/JPro-one/HelloJPro) or [Maven](htt...
- Gradle (lines 41-43) — ./gradlew jproRun
- Maven (lines 44-299) — mvn jpro:run
  - Gradle setup (lines 51-149) — The simplest way to begin is by using **Gradle** as your build tool.
    - `1` Set up Gradle (lines 58-69) — We generally recommend adding a Gradle wrapper for your project to keep version management simple for all developers.
    - `2` Create the Gradle scripts (lines 70-139) — Create the file `settings.gradle` and put it into your **project’s root directory**.
    - `3` Run the app (lines 140-149) — In a terminal session, navigate to the main project directory and enter the command:
  - Maven setup (lines 150-263) — JPro provides a plugin for Maven, which allows you to easily start JPro from an existing project.
    - `1` Install Maven (lines 156-159) — Maven can be downloaded and installed [here](https://maven.apache.org/).
    - `2` Create the Maven script (lines 160-253) — Create the file `pom.xml` and put it into your **project’s root directory**.
    - `3` Run the app (lines 254-263) — Start a **Terminal session**, move to the **main project directory** and enter the command:
  - Build commands (lines 264-299) — The `gradle` & `maven` JPro plugins provide simple commands to **run your application locally** during development as...
    - Local Development (lines 272-288) — To test your application during development, the `jproRun` command will run your app on [localhost](http://localhost).
    - Production Release & Server Commands (lines 289-299) — When it’s time to release or run your JPro server remotely, you can use the following commands to create a **zip-file...
- Using JPro (lines 300-785)
  - Build configuration (lines 302-401) — JPro properties below are available in both Gradle & Maven.
    - JPro-only dependencies with Gradle (lines 327-338) — In Gradle, `jproOnly` can be used to configure any dependencies which should only be used when running the applicatio...
    - Example `build.gradle` configuration (lines 339-364) — For Gradle, properties are set inside of `jpro { ... }`.
    - Example `pom.xml` configuration (lines 365-401) — Note how for maven, the `version` is set directly under `<plugin>` while other properties are set under the `<configu...
  - JPro config (lines 402-453) — The following properties can be set in the `jpro.conf` file:
    - Declaring multiple runnable applications (lines 431-453) — For an app to be **JPro enabled**, it must extend the class called `javafx.application.Application`.
  - HTTP routes & resources (lines 454-524)
    - Home route (lines 456-461) — In general, the `index.html` file is the default resource within each folder. This means that [http://localhost:8080/...
    - Static resources (lines 462-471) — Any resources underneath `jpro/html/` are made publicly available by the JPro server. These can be accessed via URL b...
    - Adding a global default resource (lines 472-479) — JPro allows you to define a default resource that will be returned when a URL does not find a specific resource. This...
    - Custom HTTP handlers (lines 480-524) — JPro allows you to add custom request handlers to the server API:
  - JavaFX node attributes (lines 525-555) — In some cases, you may wish to override certain behavior for a specific element or area of your application instead o...
    - **JavaFX Window Attributes** (lines 531-538) — The following can be set by calling `window.getProperties().put("attributeName", attributeValue)` in your JavaFX code:
    - **JavaFX Node Attributes** (lines 539-555) — These attributes can be set by calling `node.getProperties().put("attributeName", attributeValue)` in your JavaFX code.
  - Instance close strategies (lines 556-601) — This documentation describes the structure for configuring the close instance strategies JPro should use under differ...
    - Configuration Structure (lines 562-601) — JPro’s default configuration is structured into three optional strategies: `short`, `medium`, and `long`. Each strate...
  - Debug utilities (lines 602-691) — JPro servers provide a variety of utilities that can be useful for debugging or for system administrators to monitor ...
    - Restricting access (lines 668-678) — Access can be restricted by a `username` and `password`. Your
    - Additional utilities (lines 679-691) — > We find [ScenicView](https://github.com/JonathanGiles/scenic-view) very useful while developing, and it works perfe...
  - Logging (lines 692-745) — JPro provides the following a set of structured log messages out of the box:
    - Custom logback configurations (lines 705-721) — To override the default JPro logging configuration, simply provide a new `logback.xml` file and specify its path in t...
    - Logging to JSON (lines 722-745) — Enabling the `jpro.logToJsonFormat` option in your `jpro.conf` file will make JPro output its structured log messages...
  - Tips & Limitations (lines 746-785)
    - Potential issues (lines 748-753) — - Wait-, sleep-, and showAndWait commands in the JavaFX thread stop your app from being accessible. Dialogue boxes ar...
    - Working with stages (lines 754-759) — JavaFX **Stages** can be opened with the [WebAPI](/api/2026.1.1/jpro.webapi/com/jpro/webapi/WebAPI.html)’s `openStage...
    - Performance tips (lines 760-769) — Generally, the better your JavaFX app’s performance, the better its performance will be when running on the server & ...
    - Features with limited support (lines 770-777) — > JavaFX 3D support is currently in open beta.
    - Unsupported JavaFX features (lines 778-785) — - SwingNode
- Web Integration (lines 786-955)
  - Custom index.html (lines 788-843) — To make your JPro application accessible via URL, an “index.html” needs to exist in the project structure under `src/...
    - Index.html Structure (lines 794-830) — As an example, let’s look at the `index.html` file from the HelloJPro project:
    - Specifying the app name in jpro.conf (lines 831-843) — In order to start the app specified in your `index.html`, the name specified in the `<jpro-app>` tag must be defined ...
  - Embedding JPro (lines 844-902) — JPro can be **embedded into an existing html-page** by using the  `<jpro-app>` tag, similar to how it is included in ...
    - Adding the JPro app tag to your HTML (lines 850-870) — First, the `jpro.js` script and `jpro.css` styling must be loaded from the server that is serving your JPro applicati...
    - Adjusting behavior with `<jpro-app>` tag attributes (lines 871-902) — The following attributes can be added to the HTML tag in order to customize how your app behaves when loaded.
  - WebAPI Overview (lines 903-955) — The WebAPI enables you to create custom JavaScript code that interoperates with JPro. It also allows you to make use ...
    - Using the WebAPI with JPro (lines 915-922) — There are two ways to get access to the WebAPI:
    - Using the WebAPI without JPro (lines 923-951) — The WebAPI can be imported as a jar without requiring JPro.
    - Downloading the WebAPI Jar (lines 952-955) — If you need access to the WebAPI without Maven or Gradle, it can be downloaded from our [repository](https://sandec.j...
- Deployment (lines 956-1007)
  - Deploying on Linux (lines 958-990)
    - Deployment Requirements (lines 960-968) — JPro can run on any server with a JVM. In most cases, Linux is used for production backends, and is thus also our go-...
    - `1` Prepare your server (lines 969-977) — To run JPro on linux, the server must be configured correctly:
    - `2` Create the binary (lines 978-986) — From your terminal, create a release ZIP containing your application. You can then copy the file to your server and u...
    - `3` Run JPro (lines 987-990) — In the unzipped folder you can find a start-script: `bin/start.sh`. Run this to start your JPro server.
  - Docker templates (lines 991-1007) — The following templates can be used as a reference and adjusted to your needs.
    - Ubuntu 24.04 (lines 997-1007) — (22.04 and 20.04 also work)
- Add the Adoptium (Eclipse Temurin) APT repository and import the GPG key (lines 1008-1011) — RUN wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | apt-key add - && \
- Install Temurin 21 JDK (lines 1012-1028) — RUN apt-get update && \
    - Debian Bookworm (lines 1019-1028) — ```docker
- Add the Adoptium (Eclipse Temurin) APT repository and import the GPG key (lines 1029-1032) — RUN apt-get install -y wget apt-transport-https gnupg
- RUN wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | apt-key add - && \ (lines 1033-1033)
- add-apt-repository --yes https://packages.adoptium.net/artifactory/deb/ (lines 1034-1035)
- Install Temurin 21 JDK (lines 1036-1155) — RUN apt-get update && \
    - Fedora 39 (lines 1043-1064) — ```docker
  - Nginx (lines 1065-1112) — In order to configure nginx for JPro, create & add the following content to `/etc/nginx/sites-enabled/jproconf.nginx....
    - SSL certificates & guides (lines 1109-1112) — You can get free guides & SSL certificates from [Let’s encrypt / Certbot](https://certbot.eff.org/).
  - Apache2 (lines 1113-1155) — While we typically recommend using nginx, JPro can also be used with Apache. In this case, Apache is used as a revers...
    - SSL certificates & guides (lines 1152-1155) — You can get free guides & SSL certificates from [Let’s encrypt / Certbot](https://certbot.eff.org/).
- JPro Loadbalancer (lines 1156-1187)
  - Overview & usage (lines 1158-1187) — The JPro Loadbalancer allows you to run multiple JPro Servers in parallel.
    - Prerequisites (lines 1171-1176) — Make sure you already have a JPro project and a zip file, created with
    - Configure external servers (lines 1177-1187) — When configuring the `external servers` setup, ensure to set the `one.jpro.loadbalancer.localServerCount` property to 0.
- Set the count of local servers to 0 (lines 1188-1189) — one.jpro.loadbalancer.localServerCount=0
- Configure external servers (lines 1190-1338) — one.jpro.loadbalancer.externalServer1=http://server1.example.com:9101
    - Running your app with the loadbalancer (lines 1198-1211) — 1. Create a new folder F and download the [JPro Loadbalancer](https://sandec.jfrog.io/artifactory/repo/one/jpro/jpro-...
    - Enforce single Instance per JVM (lines 1212-1219) — With the JPro Loadbalancer you have the choice to set the ***maximum sessions to be accepted per JPro Server*** to 1....
  - Configuration (lines 1220-1299)
    - An application.properties example (lines 1222-1240) — The file `application.properties` might look like the following:
    - Available Properties (lines 1241-1265) — The following parameters can be set in the `application.properties` file:
    - Configure `one.jpro.loadbalancer.userHome` property (lines 1266-1288) — The `one.jpro.loadbalancer.userHome` property controls how the user’s home directory is managed across JPro server in...
    - Logging properties (lines 1289-1299) — It is also possible to configure certain logging properties in the `application.properties`. For example:
  - Windows service (lines 1300-1338) — To use the [Windows service wrapper](https://github.com/winsw/winsw), we have to do 2 things as a preparation.
- QF-Test Integration (lines 1339-1410)
  - The JPro Integration Library - jpro-utils.qft (lines 1340-1410) — The **jpro-utils.qft** library enables QF-Test to run automated tests directly
    - API Documentation (lines 1352-1360) — Full API documentation:
    - High-Level Overview (lines 1361-1376)
      - 1. `jpro.server` – Starting the JPro Server (lines 1363-1368) — QF-Test launches the JPro server (your JavaFX backend) and waits until it is ready.
      - 2. `jpro.client` – Launching the Browser (lines 1369-1376) — QF-Test starts a browser and loads your JPro application URL.
    - Quickstart Guide (lines 1377-1405)
      - 1. Include the Library (lines 1379-1381) — Add `jpro-utils.qft` as an *Included file* (just like `qfs.qft`).
      - 2. (Optional) Add Conditional Execution (lines 1382-1384) — If your suite also tests non-JPro apps, wrap the JPro setup in a condition.
      - 3. Add the `jpro.running` Dependency (lines 1385-1405) — Attach the dependency to your Testcase or TestcaseSet and configure:
    - Writing Tests (lines 1406-1410) — Once the JPro server and browser client are running, write your tests as usual:

