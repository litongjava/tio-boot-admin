package com.litongjava.tio.boot.admin.services;

import org.bson.Document;
import org.junit.BeforeClass;
import org.junit.Test;

import com.litongjava.jfinal.aop.Aop;
import com.litongjava.table.model.DbPage;
import com.litongjava.table.model.TableInput;
import com.litongjava.table.model.TableResult;
import com.litongjava.tio.boot.admin.config.MongoClientConfiguration;
import com.litongjava.tio.boot.tesing.TioBootTest;
import com.litongjava.tio.utils.json.FastJson2Utils;
import com.litongjava.tio.utils.resp.RespVo;

public class MongodbJsonServiceTest {

  MongodbJsonService mongodbJsonService = Aop.get(MongodbJsonService.class);

  @BeforeClass
  public static void beforeClass() {
    TioBootTest.before(MongoClientConfiguration.class);
  }

  @Test
  public void test() {
    TableInput kv = TableInput.create();
    kv.set("username", "emqx");
    TableResult<DbPage<Document>> dbJsonBean = mongodbJsonService.page("mqtt_user", kv);
    RespVo respVo = RespVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
    System.out.println(FastJson2Utils.toJson(respVo));
  }

}
