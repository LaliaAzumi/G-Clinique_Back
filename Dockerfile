FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY target/*.jar app.jar
# Crée le dossier pour les PDF
RUN mkdir -p /app/pdf_ordonnances
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]