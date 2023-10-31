FROM sbtscala/scala-sbt:eclipse-temurin-focal-11.0.20.1_1_1.9.6_3.3.1

WORKDIR /app
COPY . .

EXPOSE 80

CMD ["sbt", "run 80"]
