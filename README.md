## [REST API](http://localhost:8080/doc)

## Concept:

- Spring Modulith
    - [Introduction to Spring Modulith](https://www.baeldung.com/spring-modulith)
    - [Introducing Spring Modulith](https://spring.io/blog/2022/10/21/introducing-spring-modulith)
    - [Spring Modulith - Reference documentation](https://docs.spring.io/spring-modulith/docs/current-SNAPSHOT/reference/html/)

```
  url: jdbc:postgresql://localhost:5432/jira
  username: jira
  password: CodeGymJira
```

- There are two tables, which do not have foreign keys
    - _Reference_ - directory. Make the link using _code_ (using id is not allowed, as id is tied to the environment-specific base)
    - _UserBelong_ - link users with type (owner, lead, ...) to object (task, project, sprint, ...). FK will be checked manually

## Analogues

- https://java-source.net/open-source/issue-trackers

## Testing

- https://www.youtube.com/watch?v=aEW8ZH6wj2o

List of completed tasks:
- Delete Facebook as Social Network
- Place sensitive information as placeholders. They can be read from machine's environment variables when the server starts, Also created a .env file for docker
- Write tests for all public methods of the controller ProfileRestController
- Refactor method upload from FileUtil so it uses a modern approach to working with the file system
- Add Tags to Tasks (Entity and Controller, Service and Repository layers) only back-end
- Add Dockerfile
- Add docker-compose
- Add task time in progress and task time ready for review at service layer
