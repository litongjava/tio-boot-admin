package com.litongjava.tio.boot.admin.services;

import com.alibaba.fastjson2.JSONObject;
import com.litongjava.data.utils.SnowflakeIdGenerator;
import com.litongjava.jfinal.plugin.activerecord.Db;
import com.litongjava.jfinal.plugin.activerecord.Record;
import com.litongjava.tio.boot.admin.costants.TableNames;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PGobject;

import java.sql.SQLException;

/**
 * Created by litonglinux@qq.com on 3/25/2024_7:26 PM
 */
@Slf4j
public class UserEventService {
  public void save(String eventName, JSONObject eventValue) {
    String jsonString = eventValue.toJSONString();
    PGobject pGobject = new PGobject();
    pGobject.setType("json");
    long threadId = Thread.currentThread().getId();
    if (threadId > 31) {
      threadId = threadId % 31;
    }
    if (threadId < 0) {
      threadId = 0;
    }
    long id = new SnowflakeIdGenerator(threadId, 0).generateId();

    try {
      pGobject.setValue(jsonString);
      Record record = new Record();
      record.set("name", eventName);
      record.set("value", pGobject);
      record.set("id", id);
      boolean save = Db.save(TableNames.tio_boot_admin_system_user_event, record);
      log.info("save result:{}", save);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
