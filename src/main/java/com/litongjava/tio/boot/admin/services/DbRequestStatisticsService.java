package com.litongjava.tio.boot.admin.services;

import com.litongjava.db.activerecord.Db;

public class DbRequestStatisticsService {
  public static final String sql = "INSERT INTO sys_http_request_statistics (id,channel_id,ip,user_id,method,uri,user_agent,header,body) values(?,?,?,?,?,?,?,?,?)";

  public void saveDb(long id, long channel_id, String clientIp, Object userId, String method, String uri, String user_agent, String header, String body) {
    Db.updateBySql(sql, id, channel_id, clientIp, userId, method, uri, user_agent, header, body);
  }
}