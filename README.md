# RAML-JStorage

RAML-JStorage is a storage backend for [RAML API Designer](https://github.com/ggrandes-clones/api-designer/), built on Java to run in any Tomcat (rather than use the APIHub cloud service) with no more dependencies and still be able to collaborate designs. Project is Open source (Apache License, Version 2.0).

### Current Stable Version is [1.0.0](https://maven-release.s3.amazonaws.com/release/org/javastack/raml-jstorage/1.0.0/raml-jstorage-1.0.0.war)

---

## DOC

### Supported features

  - [x] Put file
  - [x] Get file
  - [x] Delete file
  - [x] Rename file
  - [x] Sub directories

### Requirements

  - Java/OpenJDK 1.6 or above [link](https://www.java.com/en/download/manual.jsp)
  - Apache Tomcat 7.0 or above [link](http://tomcat.apache.org/download-70.cgi)
  - RAML Api-Designer [link](https://github.com/ggrandes-clones/api-designer/archive/master.zip)

### Automated Installation (Linux)

```bash
### Linux Packages
aptitude install wget unzip grep gawk openjdk-7-jre-headless
### RAML-JStorage
wget -qO - https://github.com/ggrandes/raml-jstorage/raw/master/linux/raml-install.sh | bash
```

  - Default instalation is in `/opt/raml-jstorage/`
  - RAML files are stored in `${RAML_HOME}/storage/` (configured as system property in `${RAML_HOME}/tomcat/bin/setenv.sh`)
  - Tomcat is accessible in `http://127.0.0.1:8080/raml-jstorage/`
  - Start/Stop can be done with `${RAML_HOME}/tomcat/bin/catalina.sh [start|stop]`

### Manual Installation

  - Install Java
  - Install Tomcat
  - Download RAML-JStorage WAR file
  - Download RAML Api-Designer ZIP file
  - Install war in Tomcat webapp directory in (webapps/raml-jstorage.war)
  - Copy "dist/*" files of Api-Designer in webapps/api-designer/
  - Start tomcat
  - Copy webapps/raml-jstorage/index.html to webapps/api-designer/index.html
  - DONE 

### Configuration

RAML-JStorage only need directory path to store RAML files, this is configured with property name `raml.jstorage.directory`, that can be configured in System Property, System Environment, or file named `raml-jstorage.properties` (located in classpath) 

---
Inspired in [nodes.js raml-store](https://github.com/brianmc/raml-store), this code is Java-minimalistic version.
