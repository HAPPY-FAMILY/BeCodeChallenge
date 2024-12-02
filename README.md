# ping-server (默认端口8080)
```
#单实例运行
java -jar ping-server-1.0-SNAPSHOT.jar

#多实例运行
java -jar ping-server-1.0-SNAPSHOT.jar --server.port=8079
java -jar ping-server-1.0-SNAPSHOT.jar --server.port=8080
...
```

# pong-server (默认端口8081)
```
java -jar pong-server-1.0-SNAPSHOT.jar
```

# 启动Redis、MongoDB、RocketMQ
```
#进入对应目录执行
docker-compose up -d
```
