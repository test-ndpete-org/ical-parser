
FROM quay.io/byuoit/byu-tomcat:v7java7

VOLUME /var/opt/tomcat

WORKDIR /opt/tomcat

ENTRYPOINT ["/opt/tomcat/bin/catalina.sh"]
CMD ["run"]

ARG APPSTAGE
ARG SRC_BRANCH_A
ARG SRC_VER_A
ARG SRC_COMMIT_A
ARG SRC_TAGS_A

ENV SRC_BRANCH=${SRC_BRANCH_A} SRC_VER=${SRC_VER_A} SRC_COMMIT=${SRC_COMMIT_A} SRC_TAGS="${SRC_TAGS_A}"

RUN echo America/Denver > /etc/timezone

RUN echo "byu.app.stage=${APPSTAGE}" >>/opt/tomcat/conf/catalina.properties && \
  echo "byu.appstage=${APPSTAGE}" >>/opt/tomcat/conf/catalina.properties && \
  echo "export JAVA_OPTS='-Xms164m -Xmx164m -server -XX:PermSize=64m -XX:MaxPermSize=64m'" >>/opt/tomcat/bin/setenv.sh

COPY target/ical.war /opt/tomcat/webapps/services#ical-parser.war
