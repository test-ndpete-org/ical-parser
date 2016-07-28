FROM tomcat:7-jre7
MAINTAINER TravisAGengler@gmail.com

COPY ical/target/ical.war /usr/local/tomcat/webapps/webapp#ical.war 
