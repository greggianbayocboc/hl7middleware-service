FROM albertoclarit/hisd3base:latest


VOLUME /tmp
WORKDIR /app
ARG JAR_FILE
ADD ${JAR_FILE} /app/HISD3Middleware.jar

RUN chmod a+w /app

EXPOSE 4567
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Djava.awt.headless=true","-jar","/app/HISD3Middleware.jar"]