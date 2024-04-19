package com.litongjava.tio.boot.admin.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.jfinal.kit.Kv;
import com.litongjava.data.model.DataPageRequest;
import com.litongjava.data.model.DataQueryRequest;
import com.litongjava.data.model.DbJsonBean;
import com.litongjava.data.model.DbPage;
import com.litongjava.jfinal.plugin.mongo.MongoDb;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class MongodbJsonService {

  public DbJsonBean<DbPage<Document>> page(String f, Kv kv) {
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
      lists.add(document);
    }

    DbPage<Document> pageData = new DbPage<>();
    pageData.setTotal((int) totalCount);
    pageData.setList(lists);

    return DbJsonBean.ok(pageData);
  }

}
