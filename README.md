# ical-parser
iCal Parsing Rest Service

Technologies used - Java, Jersey

iCal parsing rest service is a restful web service that takes an iCal URL as an input and returns calendar information in JSON

Enunciate page is at the root of the web application which describes endpoints and how to interface with them.

In order to build and deploy the docker image:
    maven:package goal builds the image
    maven:deploy then pushes it to quay.io
    Before you can deploy to quay, must first ensure that maven server.xml is configured with the proper login credentials
