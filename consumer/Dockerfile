FROM openjdk:17 AS builder
COPY . /usr/src/colored-rabbits
WORKDIR /usr/src/colored-rabbits
RUN ./gradlew build


FROM openjdk:17
COPY --from=builder /usr/src/colored-rabbits/build/libs/coloredrabbits-consumer-0.0.1.jar /coloredrabbits-consumer.jar
CMD ["java", "-jar", "/coloredrabbits-consumer.jar"]
EXPOSE 8080