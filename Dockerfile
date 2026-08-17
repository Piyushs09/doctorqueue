FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN chmod +x gradlew
RUN ./gradlew clean bootJar -x test

EXPOSE 10000

CMD ["sh", "-c", "java -jar build/libs/*.jar --server.port=${PORT:-10000}"]