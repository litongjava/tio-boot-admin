package com.litongjava.tio.boot.admin.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.jfinal.kit.Kv;
import com.litongjava.db.activerecord.Record;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.mongo.MongoDb;
import com.litongjava.table.model.DataPageRequest;
import com.litongjava.table.model.DataQueryRequest;
import com.litongjava.table.model.DbPage;
import com.litongjava.table.model.TableInput;
import com.litongjava.table.model.TableResult;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;

public class MongodbJsonService {

  public TableResult<DbPage<Document>> page(String f, TableInput kv) {
    DataPageRequest dataPageRequest = new DataPageRequest(kv);
    Integer pageNo = dataPageRequest.getPageNo();
    Integer pageSize = dataPageRequest.getPageSize();

    DataQueryRequest queryRequest = new DataQueryRequest(kv);

    // sort() 方法中，需要指定排序的方向：1 代表升序（ascending），-1 代表降序（descending）
    int sortValue = 1;
    if (queryRequest.getIsAsc() != null && !queryRequest.getIsAsc()) {
      sortValue = -1;
    }
    Document queryDocument = new Document();

    @SuppressWarnings("unchecked")
    Iterator<Map.Entry<String, Object>> iterator = kv.entrySet().iterator();

    while (iterator.hasNext()) {
      Map.Entry<String, Object> entry = iterator.next();
      String key = entry.getKey();
      Object value = entry.getValue();

//      if (key.endsWith("_op")) {
//        notEqualsMap.put(key, value);
//        iterator.remove();
//      }

      // 模拟 %LIKE% 查询
      // new Document("name", new Document("$regex", ".*john.*").append("$options", "i"));
      Document queryValue = new Document("$regex", ".*" + value + ".*").append("$options", "i");
      queryDocument.put(key, queryValue);
      iterator.remove();
    }

    MongoDatabase database = MongoDb.getDatabase();
    MongoCollection<Document> collection = database.getCollection(f);

    // 计算总记录数
    long totalCount = collection.count(queryDocument);

    FindIterable<Document> iterable;

    String orderBy = queryRequest.getOrderBy();
    if (orderBy != null) {
      Document sortDocument = new Document(orderBy, sortValue); // 修改了此处，以使用正确的字段名
      iterable = collection.find(queryDocument).sort(sortDocument).skip((pageNo - 1) * pageSize).limit(pageSize);
    } else {
      iterable = collection.find(queryDocument).skip((pageNo - 1) * pageSize).limit(10);
    }

    MongoCursor<Document> cursor = iterable.iterator();
    List<Document> lists = new ArrayList<>();

    while (cursor.hasNext()) {
      Document document = cursor.next();
      Object idObject = document.get("_id");
      String idValue = null;
      if (idObject instanceof String) {
        idValue = (String) idObject;
      } else {
        ObjectId objectId = document.getObjectId("_id");
        idValue = objectId.toHexString();
      }

      document.remove("_id");
      document.put("id", idValue);
      lists.add(document);
    }

    DbPage<Document> pageData = new DbPage<>();
    pageData.setTotal((int) totalCount);
    pageData.setList(lists);

    return TableResult.ok(pageData);
  }

  public TableResult<List<Record>> list(String f, TableInput kv) {
    new DataPageRequest(kv);

    DataQueryRequest queryRequest = new DataQueryRequest(kv);

    // sort() 方法中，需要指定排序的方向：1 代表升序（ascending），-1 代表降序（descending）
    int sortValue = 1;
    if (queryRequest.getIsAsc() != null && !queryRequest.getIsAsc()) {
      sortValue = -1;
    }
    Document queryDocument = new Document();

    @SuppressWarnings("unchecked")
    Iterator<Map.Entry<String, Object>> iterator = kv.entrySet().iterator();

    while (iterator.hasNext()) {
      Map.Entry<String, Object> entry = iterator.next();
      String key = entry.getKey();
      Object value = entry.getValue();
      // 模拟 %LIKE% 查询
      // new Document("name", new Document("$regex", ".*john.*").append("$options", "i"));
      Document queryValue = new Document("$regex", ".*" + value + ".*").append("$options", "i");
      queryDocument.put(key, queryValue);
      iterator.remove();
    }

    System.out.println(queryDocument);

    MongoDatabase database = MongoDb.getDatabase();
    MongoCollection<Document> collection = database.getCollection(f);

    FindIterable<Document> iterable;

    String orderBy = queryRequest.getOrderBy();
    if (orderBy != null) {
      Document sortDocument = new Document(orderBy, sortValue); // 修改了此处，以使用正确的字段名
      iterable = collection.find(queryDocument).sort(sortDocument);
    } else {
      iterable = collection.find(queryDocument);
    }

    List<Record> lists = new ArrayList<>();

    MongoCursor<Document> cursor = iterable.iterator();

    while (cursor.hasNext()) {
      Document document = cursor.next();
      ObjectId objectId = document.getObjectId("_id");
      String hexString = objectId.toHexString();
      document.remove("_id");

      Record record = new Record();
      record.set("id", hexString);
      Set<Entry<String, Object>> entrySet = document.entrySet();
      for (Entry<String, Object> e : entrySet) {
        record.set(e.getKey(), e.getValue());
      }
      lists.add(record);
    }

    return TableResult.ok(lists);

  }

  public TableResult<List<Record>> listAll(String f) {

    MongoDatabase database = MongoDb.getDatabase();
    MongoCollection<Document> collection = database.getCollection(f);

    FindIterable<Document> iterable = collection.find();
    List<Record> lists = new ArrayList<>();

    MongoCursor<Document> cursor = iterable.iterator();

    while (cursor.hasNext()) {
      Document document = cursor.next();
      ObjectId objectId = document.getObjectId("_id");
      String hexString = objectId.toHexString();
      document.remove("_id");
      Record record = new Record();
      record.set("id", hexString);
      Set<Entry<String, Object>> entrySet = document.entrySet();
      for (Entry<String, Object> e : entrySet) {
        record.set(e.getKey(), e.getValue());
      }
      lists.add(record);
    }
    return TableResult.ok(lists);
  }

  public TableResult<Kv> saveOrUpdate(String tableName, TableInput kv) {
    String[] jsonFields = (String[]) kv.remove("json_fields");
    return this.saveOrUpdate(tableName, kv, jsonFields);
  }

  public TableResult<Kv> saveOrUpdate(String tableName, TableInput kv, String[] jsonFields) {

    if (tableName.equals("mqtt_user")) {
      return Aop.get(EmqxService.class).saveOrUpdate(tableName, kv);
    } else {
      return null;
    }
  }

  public TableResult<Boolean> deleteById(String f, String id) {

    MongoDatabase database = MongoDb.getDatabase();
    MongoCollection<Document> collection = database.getCollection(f);
    Bson filter = Filters.eq("_id", new ObjectId(id));
    DeleteResult result = collection.deleteOne(filter);
    if (result.getDeletedCount() > 0) {
      return TableResult.ok();
    } else {
      return TableResult.fail();
    }

  }
}
