FROM openjdk:17 AS builder
COPY . /usr/src/colored-rabbits
WORKDIR /usr/src/colored-rabbits
RUN ./gradlew build


FROM openjdk:17
COPY --from=builder /usr/src/colored-rabbits/build/libs/coloredrabbits-producer-0.0.1.jar /coloredrabbits-producer.jar
CMD ["java", "-jar", "/coloredrabbits-producer.jar"]
EXPOSE 8081