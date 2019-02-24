FROM hisd3base:1.0.3
#FROM albertoclarit/hisd3base:latest


VOLUME /tmp
WORKDIR /app
ARG JAR_FILE
ADD hl7middleware.jar /app/HISD3Middleware.jar

RUN chmod a+w /app

EXPOSE 4567
EXPOSE 22222
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Djava.awt.headless=true","-jar","/app/HISD3Middleware.jar"]