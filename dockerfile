# FROM gradle:8.4.0-jdk11-alpine AS TEMP_BUILD_IMAGE
# ENV APP_HOME=/usr/app/
# WORKDIR $APP_HOME
# COPY build.gradle settings.gradle $APP_HOME

# COPY gradle $APP_HOME/gradle
# COPY --chown=gradle:gradle . /home/gradle/src
# USER root
# RUN chown -R gradle /home/gradle/src

# RUN gradle clean build

# FROM adoptopenjdk/openjdk11:alpine-jre
# ENV APP_HOME=/usr/app/
# ENV MAIN_CLASS = org.example.JsonConsumer

# WORKDIR $APP_HOME
# COPY --from=TEMP_BUILD_IMAGE $APP_HOME .

# ENTRYPOINT exec gradle run -Pmain=$MAIN_CLASS

FROM adoptopenjdk/openjdk11:alpine-jre
ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME
ARG service
RUN echo $service
COPY build/libs/$service-1.0-SNAPSHOT.jar app.jar
CMD java -jar app.jar

