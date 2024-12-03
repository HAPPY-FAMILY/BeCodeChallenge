# ping-server (默认端口8080)
```
#单实例运行
java -Dserver.port=8080 -Dspring.application.name=ping -jar ping-server-1.0-SNAPSHOT.jar

#多实例运行
java -Dserver.port=8079 -Dspring.application.name=ping01 -jar ping-server-1.0-SNAPSHOT.jar
java -Dserver.port=8080 -Dspring.application.name=ping02 -jar ping-server-1.0-SNAPSHOT.jar
...
```

# pong-server (默认端口8081)
```
java -Dserver.port=8081 -Dspring.application.name=pong -jar pong-server-1.0-SNAPSHOT.jar
```

# 启动MongoDB
```
#进入对应目录执行
docker-compose up -d
```

# 生成测试覆盖率报告
```
mvn clean test jacoco:report
```
