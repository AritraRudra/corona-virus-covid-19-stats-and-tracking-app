# Stage one
FROM maven:3.6.0-jdk-13 AS maven-package
ARG APP_DIR=/app/
RUN mkdir -p ${APP_DIR}
WORKDIR ${APP_DIR}

COPY pom.xml .

# build all dependencies for cache/offline use
RUN mvn dependency:resolve-plugins dependency:go-offline -B

COPY src ./src

RUN mvn clean package -B



# Stage two
FROM openjdk:13-jdk AS build-image

# RUN cat /etc/os-release
RUN groupadd app-users
RUN useradd app-user -G app-users


ARG APP_DIR=/app
ARG DEPENDENCY=${APP_DIR}/target/dependency
ARG BIN_DIR=${APP_DIR}/bin

RUN mkdir -p ${APP_DIR}

RUN chown app-user:app-users ${APP_DIR}
USER app-user

COPY --from=maven-package ${DEPENDENCY}/BOOT-INF/lib ${APP_DIR}/lib
COPY --from=maven-package ${DEPENDENCY}/META-INF ${APP_DIR}/META-INF
#COPY --from=maven-package ${DEPENDENCY}/BOOT-INF/classes ${BIN_DIR}
COPY --from=maven-package ${DEPENDENCY}/BOOT-INF/classes /app

EXPOSE 8080

# CMD ["java","-cp","app:app/lib/*","com.covid19.Covid19InfectedTrackingAppApplication"]
ENTRYPOINT ["java","-cp","app/bin/*:app/lib/*","com.covid19.Covid19InfectedTrackingAppApplication"]
