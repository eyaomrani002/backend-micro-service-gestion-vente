FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copie du JAR
COPY target/gatewayservice-*.jar app.jar

# Installation des dépendances et configuration utilisateur
RUN apk add --no-cache curl && \
    addgroup -S spring && adduser -S spring -G spring && \
    chown -R spring:spring /app

# Passage à l'utilisateur non-root
USER spring
EXPOSE 8888

# Optimisation JVM
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Spring configuration
ENV SPRING_APPLICATION_NAME=gateway-service \
    SPRING_MAIN_WEB_APPLICATION_TYPE=reactive \
    SPRING_CLOUD_DISCOVERY_ENABLED=true

# Healthcheck amélioré
HEALTHCHECK --interval=45s --timeout=8s --retries=3 \
  CMD curl -sf http://localhost:8888/actuator/health || exit 1

# Point d’entrée optimisé
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
