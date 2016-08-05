FROM tomcat:7-jre7
MAINTAINER TravisAGengler@gmail.com

ARG APPSTAGE

RUN echo "byu.app.stage=${APPSTAGE}" >>/opt/tomcat/conf/catalina.properties

COPY ../target/ical.war /usr/local/tomcat/webapps/services#ical-parser.war
