# Querydsl
## Study for Spring Data JPA Querydsl

### application.yml
###  test/resources, main/resources 에 추가

```
spring:
  # 샘플 데이터 추가, 환경에 따라 개발 서버는 dev, 운영은 production, test 서버는 test
  profiles:
    active: 
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
        # jpql이 궁금할 때
        use_sql_comments: true
  #        default_batch_fetch_size: 500
  # MySQL 8 방언
    database-platform: org.hibernate.dialect.MySQL8Dialect
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
