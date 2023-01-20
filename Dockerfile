FROM openjdk:11
EXPOSE 8080
COPY /target/moneystats-service.jar moneystats-service.jar
ENTRYPOINT ["java","-jar","moneystats-service.jar"]
ENV TZ Europe/Rome