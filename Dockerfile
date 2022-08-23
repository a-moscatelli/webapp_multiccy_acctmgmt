FROM openjdk:8-jdk-alpine

ARG PORT=8080
ARG JAVA_OPTS="-Xmx384m -Xss512k -XX:+UseCompressedOops"

# WORKDIR /

ADD g4mcaa-0.1.war g4mcaa-0.1.war

EXPOSE $PORT

CMD java -Dgrails.env=prod -Dserver.port=$PORT $JAVA_OPTS -jar g4mcaa-0.1.war
