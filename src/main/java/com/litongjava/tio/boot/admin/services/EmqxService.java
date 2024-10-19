package com.litongjava.tio.boot.admin.services;

import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.jfinal.kit.Kv;
import com.litongjava.db.TableInput;
import com.litongjava.db.TableResult;
import com.litongjava.mongo.MongoDb;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EmqxService {

  public Document getNewUserDocument(String userId, String username, String password) {
    // 1. 随机生成一个盐字符串
    String salt = RandomUtil.randomString(32);

    // 2. 使用 hutool 对password进行加密 加密方式sha256 加盐方式 suffix 盐使用上面生成的盐
    String passwordHash = DigestUtil.sha256Hex(password + salt);

    // 要插入的数据
    Document newUser = new Document("username", username).append("is_superuser", false)
        // userid
        .append("user_id", userId)
        //
        .append("password", password).append("salt", salt).append("password_hash", passwordHash);
    return newUser;
  }

  /**
   * @param userId
   * @param username
   * @param password 明文
   * @return
   */
  public String addAuthUser(String userId, String username, String password) {
    // 获取数据库连接对象
    MongoDatabase database = MongoDb.getDatabase();
    // 获取集合
    MongoCollection<Document> collection = database.getCollection("mqtt_user");

    Document document = getNewUserDocument(userId, username, password);

    // 插入数据
    collection.insertOne(document);
    return "success";
  }

  public TableResult<Kv> getEmqxAuth(Object userId) {
    MongoDatabase database = MongoDb.getDatabase();
    MongoCollection<Document> collection = database.getCollection("mqtt_user");

    Bson filter = Filters.eq("user_id", userId);

    Document document = collection.find(filter).first();
    if (document != null) {
      String username = document.getString("username");
      String password = document.getString("password");
      Kv kv = Kv.by("username", username).set("password", password).set("userId", userId);
      return TableResult.ok(kv);
    } else {
      return TableResult.fail("not found user:" + userId);
    }
  }

  public void addOrUpdateUser(List<Kv> usernames) {
    // 获取数据库连接对象
    MongoDatabase database = MongoDb.getDatabase();

    // 获取collection
    MongoCollection<Document> collection = database.getCollection("mqtt_user");
    for (Kv kv : usernames) {
      String password = kv.getStr("password");
      String username = kv.getStr("username");

      Bson filter = Filters.eq("username", username);

      Document document = collection.find(filter).first();

      if (document != null) {
        // update
        Document newUser = getNewUserDocument(username, password);
        Document set = new Document("$set", newUser);
        collection.updateOne(filter, set);

      } else {
        // save
        Document newUser = getNewUserDocument(username, password);
        collection.insertOne(newUser);
      }
    }
  }

  private Document getNewUserDocument(String username, String password) {
    log.info("password:{}",password);
    // 1. 随机生成一个盐字符串
    String salt = RandomUtil.randomString(32);

    // 2. 使用 hutool 对password进行加密 加密方式sha256 加盐方式 suffix 盐使用上面生成的盐
    String passwordHash = DigestUtil.sha256Hex(password + salt);

    // 要插入的数据
    Document newUser = new Document("username", username).append("is_superuser", false)
        // password
        .append("password", password).append("salt", salt).append("password_hash", passwordHash);
    return newUser;
  }

  public void updateAuthUser(Object idValue, String userId, String username, String password) {
    // 获取数据库连接对象
    MongoDatabase database = MongoDb.getDatabase();
    MongoCollection<Document> collection = database.getCollection("mqtt_user");
    Bson filter = Filters.eq("_id", new ObjectId((String) idValue));
    // update
    Document newUser = getNewUserDocument(username, password);
    newUser.put("user_id", userId);
    Document set = new Document("$set", newUser);
    collection.updateOne(filter, set);
  }

  public TableResult<Kv> saveOrUpdate(String tableName, TableInput kv) {
    Object idValue = kv.remove("id");
    if (idValue != null) {
      updateAuthUser(idValue, kv.getStr("user_id"), kv.getStr("username"), kv.getStr("password"));
      return TableResult.ok();
    } else {
      addAuthUser(kv.getStr("user_id"), kv.getStr("username"), kv.getStr("password"));
      return TableResult.ok();
    }
  }

}
