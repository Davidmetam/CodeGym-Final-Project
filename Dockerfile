FROM openjdk:18-jdk-slim
LABEL version="1.0" description="Jira Application"

RUN mkdir -p /home/app

COPY . /home/app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/home/app/CodeGym-Final-Project/target/jira-1.0.jar"]