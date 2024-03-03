FROM eclipse-temurin:17-jdk-jammy AS builder
ARG DOCKER_HOST=tcp://dind:2375
ENV MAVEN_VERSION="3.9.6"
RUN curl "https://downloads.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" --output maven.tar.gz \
    && tar -xzf 'maven.tar.gz' \
    && ln -s apache-maven-${MAVEN_VERSION} maven
ENV PATH="$PATH:/maven/bin"
#RUN curl -fsSL get.docker.com | sh
ENV MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"
ENV DOCKER_HOST=$DOCKER_HOST
#ENV TESTCONTAINERS_HOST_OVERRIDE=$DOCKER_HOST
COPY ./docker/settings.xml /settings.xml
RUN rm -f /maven/conf/settings.xml >/dev/null
RUN cp /settings.xml /maven/conf/settings.xml

WORKDIR /opt/app
COPY pom.xml ./
RUN mvn dependency:go-offline
COPY ./src ./src
RUN mvn clean install

FROM eclipse-temurin:17-jre-jammy
RUN adduser --system --group app-user
EXPOSE 8080
COPY --from=builder --chown=app-user:app-user /opt/app/target/*.jar /opt/app/app.jar
USER app-user
CMD ["java", "-jar", "/opt/app/app.jar"]
