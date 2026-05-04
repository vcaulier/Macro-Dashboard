FROM openjdk:25-ea AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package

FROM openjdk:25-ea
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
CMD ["java", "-jar", "/app/app.jar"]
