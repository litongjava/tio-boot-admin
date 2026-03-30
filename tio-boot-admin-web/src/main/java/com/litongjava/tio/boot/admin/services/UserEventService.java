package com.litongjava.tio.boot.admin.services;

import java.sql.SQLException;

import org.postgresql.util.PGobject;

import com.alibaba.fastjson2.JSONObject;
import com.litongjava.db.activerecord.Db;
import com.litongjava.db.activerecord.Row;
import com.litongjava.tio.boot.admin.costants.TioBootAdminTableNames;
import com.litongjava.tio.utils.snowflake.SnowflakeIdUtils;

import lombok.extern.slf4j.Slf4j;

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
    long id =SnowflakeIdUtils.id();

    try {
      pGobject.setValue(jsonString);
      Row record = new Row();
      record.set("name", eventName);
      record.set("value", pGobject);
      record.set("id", id);
      boolean save = Db.save(TioBootAdminTableNames.tio_boot_admin_system_user_event, record);
      log.info("save result:{}", save);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
