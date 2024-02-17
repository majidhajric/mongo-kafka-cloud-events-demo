FROM openjre:17
ARG JAR_FILE=./target/*.jar
COPY ${JAR_FILE} /opt/demo/app.jar
EXPOSE 8080
ENTRYPOINT ["java","--add-opens", "java.base/java.nio.charset=ALL-UNNAMED", "--add-opens",  "java.base/java.lang=ALL-UNNAMED", "-jar","/opt/demo/app.jar"]