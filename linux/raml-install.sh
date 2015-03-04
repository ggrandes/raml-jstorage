#!/bin/bash
#
RAML_HOME=${RAML_HOME:-/opt/raml-jstorage}
#
URL_TOMCAT_DEPLOY="https://gist.github.com/ggrandes/7505277/raw/tomcat-deploy.sh"
URL_API_DESIGNER="https://github.com/ggrandes-clones/api-designer/archive/master.zip"
URL_JSTORAGE="https://maven-release.s3.amazonaws.com/release/org/javastack/raml-jstorage/1.0.1/raml-jstorage-1.0.1.war"
#
die () {
  echo "FAILED" "$1"
  exit 1;
}
check_dep () {
  type -p "$1" 1>/dev/null 2>&1 || die "Dependency not found: $1"
}
check_dep wget
check_dep unzip
check_dep grep
check_dep awk
check_dep java
#
###
### Base
###
mkdir -pm775 ${RAML_HOME}/dist/
cd ${RAML_HOME}/dist/
###
### Install Tomcat
###
wget -O tomcat-deploy.sh $URL_TOMCAT_DEPLOY
chmod +x tomcat-deploy.sh
DIST="${RAML_HOME}/dist/" ./tomcat-deploy.sh
read TOMCAT_VERSION < unpacked/tomcat-version
mv unpacked/apache-tomcat-${TOMCAT_VERSION} ${RAML_HOME}
rm -fv ${RAML_HOME}/tomcat
ln -fs apache-tomcat-${TOMCAT_VERSION} ${RAML_HOME}/tomcat
rm -fr ${RAML_HOME}/tomcat/webapps/{docs,examples,host-manager,manager,ROOT}
mkdir -pm775 ${RAML_HOME}/tomcat/webapps/ROOT/
###
### Install RAML api-designer
###
mkdir -pm775 api-designer ${RAML_HOME}/tomcat/webapps/api-designer
wget -O api-designer/master.zip $URL_API_DESIGNER
unzip -o api-designer/master.zip -d /tmp/
mv /tmp/api-designer-master/dist/* ${RAML_HOME}/tomcat/webapps/api-designer/
cp -a ${RAML_HOME}/tomcat/webapps/api-designer/index.html ${RAML_HOME}/tomcat/webapps/api-designer/local-storage.html
###
### Install RAML-JStorage
###
grep -q "raml.jstorage.directory" ${RAML_HOME}/tomcat/bin/setenv.sh 1>/dev/null 2>&1 ||
  echo "CATALINA_OPTS=\"-Dprogram.name=RAML-JSTORAGE -Draml.jstorage.directory=${RAML_HOME}/storage/ \${CATALINA_OPTS}\"" >> ${RAML_HOME}/tomcat/bin/setenv.sh
wget -O ${RAML_HOME}/tomcat/webapps/raml-jstorage.war $URL_JSTORAGE
unzip ${RAML_HOME}/tomcat/webapps/raml-jstorage.war index.html -d ${RAML_HOME}/tomcat/webapps/api-designer/ 
#
echo "INSTALL FINISHED"
echo "RAML_HOME=${RAML_HOME}"
echo "URL=http://127.0.0.1:8080/raml-jstorage/"
echo "Start/Stop with:"
echo "${RAML_HOME}/tomcat/bin/catalina.sh <start|stop>"
#
