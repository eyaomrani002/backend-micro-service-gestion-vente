FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
RUN apk add --no-cache wget && \
    addgroup -S spring && adduser -S spring -G spring && \
    chown -R spring:spring /app
USER spring
EXPOSE 8761
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"
ENTRYPOINT java ${JAVA_OPTS} -jar app.jar
HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget --spider -q http://localhost:8761/actuator/health || exit 1
