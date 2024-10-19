package com.litongjava.tio.boot.admin.services;

import org.bson.Document;
import org.junit.BeforeClass;
import org.junit.Test;

import com.litongjava.db.TableInput;
import com.litongjava.db.TableResult;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.model.page.DbPage;
import com.litongjava.tio.boot.admin.config.MongoClientConfiguration;
import com.litongjava.tio.boot.tesing.TioBootTest;
import com.litongjava.tio.utils.json.FastJson2Utils;

public class MongodbJsonServiceTest {

  MongodbJsonService mongodbJsonService = Aop.get(MongodbJsonService.class);

  @BeforeClass
  public static void beforeClass() {
    TioBootTest.runWith(MongoClientConfiguration.class);
  }

  @Test
  public void test() {
    TableInput kv = TableInput.create();
    kv.set("username", "emqx");
    TableResult<DbPage<Document>> dbJsonBean = mongodbJsonService.page("mqtt_user", kv);
    RespBodyVo respVo = RespBodyVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
    System.out.println(FastJson2Utils.toJson(respVo));
  }

}
