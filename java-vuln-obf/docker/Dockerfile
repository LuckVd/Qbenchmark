FROM openjdk:8-jdk-alpine

WORKDIR /app

COPY target/java-vuln-obf-1.0.0.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
