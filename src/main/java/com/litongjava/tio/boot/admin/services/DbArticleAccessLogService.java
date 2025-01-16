package com.litongjava.tio.boot.admin.services;

import com.litongjava.db.activerecord.Db;
import com.litongjava.tio.boot.admin.vo.AccessLogVo;

public class DbArticleAccessLogService {
  public static final String sql = "INSERT INTO tio_boot_admin_article_access_log (id,channel_id,ip,user_id,method,uri,user_agent,header,body) values(?,?,?,?,?,?,?,?,?)";

  public void saveDb(AccessLogVo vo) {
    Db.updateBySql(sql, vo.id, vo.channel_id, vo.clientIp, vo.userId, vo.method, vo.uri, vo.user_agent, vo.header, vo.body);
  }
}