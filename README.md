# ping-server (默认端口8080)
```
#单实例运行
java -jar ping-server-1.0-SNAPSHOT.jar -Dspring.application.name=ping -Dserver.port=8080

#多实例运行
java -jar ping-server-1.0-SNAPSHOT.jar -Dspring.application.name=ping01 -Dserver.port=8079
java -jar ping-server-1.0-SNAPSHOT.jar -Dspring.application.name=ping02 -Dserver.port=8080
...
```

# pong-server (默认端口8081)
```
java -jar pong-server-1.0-SNAPSHOT.jar -Dspring.application.name=pong -Dserver.port=8081
```

# 启动Redis、MongoDB、RocketMQ
```
#进入对应目录执行
docker-compose up -d
```
