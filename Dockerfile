FROM openjdk:11
EXPOSE 8080
WORKDIR /
COPY /target/moneystats-service.jar moneystats-service.jar

#ARG DEPLOY
#
#RUN if [ "$DEPLOY" = "STG" ]; then \
#    mv config/logback-stg.xml src/main/resources/logback.xml; \
#  fi \

#RUN if [ "$DEPLOY" = "STG" ]; then \
#    COPY /config/logback-stg.xml /src/main/resource/logback.xml \
#  elif  [ "$DEPLOY" = "PROD" ]; then \
#    COPY /config/logback-prod.xml /src/main/resource/logback.xml \
#  fi

ENTRYPOINT ["java","-jar","moneystats-service.jar"]
ENV TZ Europe/Rome