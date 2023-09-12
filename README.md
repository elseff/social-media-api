<img alt="social-media logo" src="images/social-media-logo.png"/>

[![Actions](https://github.com/elseff/social-media/workflows/Build/badge.svg)](https://github.com/elseff/social-media/actions)

    Социальная медиа платформа, позволяющая пользователям регистрироваться, 
    входить в систему, создавать посты, переписываться, подписываться на других
    пользователей и получать свою ленту активности

<b>Технологии и языки:</b>
1. Java 17
2. Spring (Boot, Data JPA, Security (JWT), Web)
3. OpenAPI 3
4. PostgreSQL
5. Flyway
6. Junit 5, Mockito, AssertJ
7. Hateoas
8. Test Containers
9. Maven

#### Доступ к OpenAPI 
```
http://localhost:8080/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config
```
### Запуск
#### Запуск с помощью docker-compose
Вы можете запустить приложение через docker-compose

```
    ./mvnw clean package
    docker-compose build
    docker-compose up
```

<hr/>
