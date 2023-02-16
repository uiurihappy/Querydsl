# Querydsl
## Study for Spring Data JPA Querydsl


### application.yml

```
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/${DB이름}?serverTimezone=Asia/Seoul
    username: ${DB 유저}
    password: ${DB 패스워드}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: create
    properties:
      hibernate:
#        show_sql: true
        format_sql: true
  #        default_batch_fetch_size: 500
  devtools:
    restart:
      enabled: true
logging:
  level:
    p6spy: info
    org.hibernate.SQL: debug
    org.hiberante.type: trace
server:
  port: 3080
```

### spy.properties

```
appender=com.p6spy.engine.spy.appender.Slf4JLogger

logMessageFormat=com.p6spy.engine.spy.appender.CustomLineFormat

customLogMessageFormat=took %(executionTime) ms | %(sql)

excludecategories=info,debug,result
```

ignore에 사항이며, spy.properties는 알맞게 커스텀하여 사용 가능합니다.
