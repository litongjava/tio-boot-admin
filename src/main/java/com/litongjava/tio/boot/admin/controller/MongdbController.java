package com.litongjava.tio.boot.admin.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.jfinal.kit.Kv;
import com.litongjava.data.model.DbJsonBean;
import com.litongjava.data.model.DbPage;
import com.litongjava.data.utils.KvUtils;
import com.litongjava.data.utils.TioRequestParamUtils;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.jfinal.plugin.mongo.MongoDb;
import com.litongjava.tio.boot.admin.services.MongodbJsonService;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.server.annotation.RequestPath;
import com.litongjava.tio.utils.resp.RespVo;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import lombok.extern.slf4j.Slf4j;

@RequestPath("/mongodb/json")
@Slf4j
public class MongdbController {

  @RequestPath("/{f}/page")
  public RespVo page(String f, HttpRequest request) {
    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    map.remove("f");
    Object current = map.remove("current");
    if (current != null) {
      // add support for ant design pro table
      map.put("pageNo", current);
    }
    Kv kv = KvUtils.camelToUnderscore(map);

    log.info("tableName:{},kv:{}", f, kv);
    MongodbJsonService mongodbJsonService = Aop.get(MongodbJsonService.class);
    DbJsonBean<DbPage<Document>> dbJsonBean = mongodbJsonService.page(f, kv);
    return RespVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
  }

  public String add() {
    // 获取数据库连接对象
    MongoDatabase database = MongoDb.getDatabase();
    // 获取集合
    MongoCollection<Document> collection = database.getCollection("user");
    // 要插入的数据
    Document document = new Document("name", "张三").append("sex", "男").append("age", 18);
    // 插入如下
    collection.insertOne(document);
    return "success";
  }

  public List<Document> list() {
    MongoDatabase database = MongoDb.getDatabase();
    MongoCollection<Document> collection = database.getCollection("user");
    // 查找集合中的所有文档,并遍历
    FindIterable<Document> iterable = collection.find();
    MongoCursor<Document> cursor = iterable.iterator();
    List<Document> lists = new ArrayList<>();
    while (cursor.hasNext()) {
      Document doucment = cursor.next();
      lists.add(doucment);
    }
    return lists;
  }

  public List<Document> listDevices() {
    MongoDatabase database = MongoDb.getDatabase("penhub");
    MongoCollection<Document> collection = database.getCollection("devices");
    // 查找集合中的所有文档,并遍历
    FindIterable<Document> iterable = collection.find();
    MongoCursor<Document> cursor = iterable.iterator();
    List<Document> lists = new ArrayList<>();
    while (cursor.hasNext()) {
      Document doucment = cursor.next();
      lists.add(doucment);
    }
    return lists;
  }
}
