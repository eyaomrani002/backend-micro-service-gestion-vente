FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copier le JAR généré par Maven
COPY target/authentificationservice-*.jar app.jar

# Ajouter curl et créer utilisateur
RUN apk add --no-cache curl && \
    addgroup -S spring && adduser -S spring -G spring && \
    chown -R spring:spring /app

USER spring

EXPOSE 8089

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENV SPRING_CONFIG_IMPORT="optional:configserver:http://config-service:5555" \
    EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka \
    SPRING_PROFILES_ACTIVE=dev

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD curl -sf http://localhost:8089/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
