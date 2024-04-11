package com.litongjava.tio.boot.admin.services;

import com.jfinal.kit.Kv;
import com.litongjava.data.model.DbJsonBean;
import com.litongjava.data.services.DbJsonService;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.jfinal.plugin.activerecord.Db;
import com.litongjava.jfinal.plugin.activerecord.Record;
import com.litongjava.tio.boot.admin.config.TableToJsonConfig;
import com.litongjava.tio.boot.tesing.TioBootTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */

@Slf4j
public class SelectPostsService {

  @BeforeClass
  public static void boforeClass() {
    TioBootTest.before(TableToJsonConfig.class);

  }

  @Test
  public void findAllFromPosts() {
    String tableName = "posts";
    try {
      Record record = Db.findFirst("select * from " + tableName);
      System.out.println(record.toMap());
      //attached_images 对应的数据类型是 text[],那么对应觉得java类型是什么呢?
      Object attached_images = record.get("attached_images");
      //org.postgresql.jdbc.PgArray
      System.out.println(attached_images.getClass().toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testInstanceOfStringArray() {
    String[] images = {"image1", "image2"};
    if (images instanceof String[]) {
      System.out.println("printArray");
    }
  }

  @Test
  public void addToPost() {
    //测试成功
    String[] images = {"image1", "image2"};
    DbJsonService dbJsonService = Aop.get(DbJsonService.class);
    Kv kv = Kv.create();
    kv.set("user_id", "8KncYW4B8Bhn8fn4A4z4AVLyoRs2");
    kv.set("title", "Java Title");
    kv.set("content", "Java Content");
    kv.set("attached_images", images);
    try {
      DbJsonBean<Kv> posts = dbJsonService.save("posts", kv);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
