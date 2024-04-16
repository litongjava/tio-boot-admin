package com.litongjava.tio.boot.admin.services;

import com.jfinal.kit.Kv;
import com.litongjava.data.model.DbJsonBean;
import com.litongjava.data.services.DbJsonService;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.jfinal.plugin.activerecord.Db;
import com.litongjava.jfinal.plugin.activerecord.Record;
import com.litongjava.tio.boot.admin.config.TableToJsonConfig;
import com.litongjava.tio.boot.tesing.TioBootTest;
import org.junit.BeforeClass;
import org.junit.Test;

public class SystemConstantsTest {

  @BeforeClass
  public static void beforeClass() {
    TioBootTest.before(TableToJsonConfig.class);
  }

  @Test
  public void adminUser() {
    //测试成功
    String[] images = {"image1", "image2"};
    DbJsonService dbJsonService = Aop.get(DbJsonService.class);
    Kv kv = Kv.create();
    kv.set("id", 100);
    kv.set("key", "image");
    kv.set("value", images);
    try {
      DbJsonBean<Kv> posts = dbJsonService.save("tio_boot_admin_system_constants_config", kv);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void findAll() {
    String tableName = "tio_boot_admin_system_constants_config";
    try {
      String sql = "select * from " + tableName + " where key=?";
      Record record = Db.findFirst(sql, "image");
      System.out.println(record.toMap());
      //attached_images 对应的数据类型是 text[],那么对应觉得java类型是什么呢?
      Object value = record.get("value");
      System.out.println(value.getClass().toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void batchUpdateByIds() {
    String tableName = "tio_boot_admin_system_constants_config";
    DbJsonService dbJsonService = Aop.get(DbJsonService.class);
    Long[] ids = new Long[]{1L, 100L, 369029537511587840L};
    Kv kv = Kv.create();
    kv.set("ids", ids);
    kv.set("deleted", 1);

    DbJsonBean<Kv> kvDbJsonBean = dbJsonService.batchUpdateByIds(tableName, kv);
    System.out.println(kvDbJsonBean);
  }
}
