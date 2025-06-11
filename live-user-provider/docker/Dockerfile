FROM openjdk:17-jdk-alpine
VOLUME /tmp
ADD live-user-provider-docker.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]