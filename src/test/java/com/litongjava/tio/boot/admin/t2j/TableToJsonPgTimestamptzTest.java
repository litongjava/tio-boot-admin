package com.litongjava.tio.boot.admin.t2j;

import com.jfinal.kit.Kv;
import com.litongjava.data.model.DbJsonBean;
import com.litongjava.data.services.DbJsonService;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.tio.boot.admin.config.TableToJsonConfig;
import com.litongjava.tio.boot.tesing.TioBootTest;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Date;


public class TableToJsonPgTimestamptzTest {
  @BeforeClass
  public static void beforeClass() {
    TioBootTest.before(TableToJsonConfig.class);
  }

  @Test
  public void addUser() {
    String tableName = "users";
    DbJsonService dbJsonService = Aop.get(DbJsonService.class);
    Kv kv = Kv.create();
    kv.set("display_name", "Tong Li");
    kv.set("platform", "Other");
    kv.set("created_at", new Date());
    kv.set("avatar_url", "https://firebasestorage.googleapis.com/v0/b/imaginix-eda2e.appspot.com/o/public%2Fimages%2F369047325126995968.jpg?alt=media");

    DbJsonBean<Kv> save = dbJsonService.save(tableName, kv);
    System.out.println(save.getData());
  }


}
