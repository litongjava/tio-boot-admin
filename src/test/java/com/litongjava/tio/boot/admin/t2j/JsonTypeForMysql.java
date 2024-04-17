package com.litongjava.tio.boot.admin.t2j;

import com.jfinal.kit.Kv;
import com.litongjava.data.model.DbJsonBean;
import com.litongjava.data.services.DbJsonService;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.jfinal.plugin.activerecord.Page;
import com.litongjava.jfinal.plugin.activerecord.Record;
import com.litongjava.tio.boot.admin.config.TableToJsonConfig;
import com.litongjava.tio.boot.tesing.TioBootTest;
import com.litongjava.tio.utils.json.FastJson2Utils;
import com.litongjava.tio.utils.json.JsonUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
public class JsonTypeForMysql {

  @BeforeClass
  public static void beforeClass() {
    TioBootTest.before(TableToJsonConfig.class);
  }

  DbJsonService dbJsonService = Aop.get(DbJsonService.class);

  String tableName = "enote_android_app_version";
  //urls的数据类型是Json

  @Test
  public void test01() {
    Kv kv = Kv.create();
    String[] jsonFields = new String[]{"urls"};
    kv.set("version", 1.0f);
    kv.set("urls", new String[]{"xxxx"});
    kv.set("is_must_update", 1);
    DbJsonBean<Kv> saveResult = dbJsonService.save(tableName, kv, jsonFields);
    System.out.println(saveResult);
  }

  @Test
  public void test02() {
    Kv kv = Kv.create();
    kv.set("json_fields", new String[]{"urls"});
    kv.set("id", 370150885008871424L);
    DbJsonBean<Page<Record>> jsonBean = dbJsonService.page(tableName, kv);
    List<Record> list = jsonBean.getData().getList();
    list.forEach((item) -> {
      System.out.println(JsonUtils.toJson(item.toMap()));
    });

  }

  @Test
  public void test03() {
    Kv kv = Kv.create();
    kv.set("json_fields", new String[]{"urls"});
    DbJsonBean<Record> dbJsonBean = dbJsonService.getById(tableName, 370150885008871424L, kv);
    Record record = dbJsonBean.getData();
    String s = FastJson2Utils.toJson(record.toMap());
    System.out.println(s);

  }
}
