package com.litongjava.tio.boot.admin.config;

import java.util.ArrayList;
import java.util.List;

import com.litongjava.mongo.MongoDb;
import com.litongjava.tio.boot.server.TioBootServer;
import com.litongjava.tio.utils.environment.EnvUtils;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

public class TioAdminMongoClientConfiguration {

  public void config() {
    String mongodbHost = EnvUtils.getStr("mongodb.host");
    if (mongodbHost == null) {
      return;
    }
    int mongodbPort = EnvUtils.getInt("mongodb.port");
    String mongodbAuthSource = EnvUtils.get("mongodb.authSource");
    String mongodbUsername = EnvUtils.get("mongodb.username");
    String mongodbPassword = EnvUtils.get("mongodb.password");
    String mongodbDatabase = EnvUtils.get("mongodb.database");

    List<ServerAddress> adds = new ArrayList<>();
    // ServerAddress()两个参数分别为 服务器地址 和 端口
    ServerAddress serverAddress = new ServerAddress(mongodbHost, mongodbPort);
    adds.add(serverAddress);
    List<MongoCredential> credentials = new ArrayList<>();

    // MongoCredential.createScramSha1Credential()三个参数分别为 用户名 数据库名称 密码
    MongoCredential mongoCredential = MongoCredential.createScramSha1Credential(mongodbUsername, mongodbAuthSource, mongodbPassword.toCharArray());
    credentials.add(mongoCredential);

    // 通过连接认证获取MongoDB连接
    MongoClient mongoClient = new MongoClient(adds, credentials);

    // 连接到数据库
    MongoDatabase mongoDatabase = mongoClient.getDatabase(mongodbDatabase);

    // 保持client and database;
    MongoDb.setClient(mongoClient);
    MongoDb.setDatabase(mongoDatabase);

    // 添加addDestroyMethod
    TioBootServer.me().addDestroyMethod(mongoClient::close);

  }
}
