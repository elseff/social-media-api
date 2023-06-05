FROM openjdk:17-alpine

EXPOSE 8080

COPY target/*.jar /social-media-app/app.jar

VOLUME /social-media-app

WORKDIR /social-media-app

CMD java -jar app.jar