package com.litongjava.tio.boot.admin.services;

import org.junit.Test;

import com.jfinal.kit.Kv;
import com.litongjava.db.TableInput;
import com.litongjava.db.TableResult;
import com.litongjava.db.activerecord.Record;
import com.litongjava.table.services.ApiTable;
import com.litongjava.tio.boot.admin.config.DbConfig;
import com.litongjava.tio.utils.environment.EnvUtils;
import com.litongjava.tio.utils.json.JsonUtils;

public class UpdateActivityTest {

  @Test
  public void test01() {

    TableInput kv = TableInput.by("id", 400209640782073856L).set("start_time", null);
    // System.out.println(FastJson2Utils.toJson(kv));
    System.out.println(kv);
    Record record = new Record();
    record.setColumns(kv.toMap());
    System.out.println(record);

    EnvUtils.load();
    new DbConfig().activeRecordPlugin();
    
    TableResult<Kv> tableResult = ApiTable.saveOrUpdate("rumi_sjsu_activity", kv);
    String json = JsonUtils.toJson(tableResult);
    System.out.println(json);

  }
}
