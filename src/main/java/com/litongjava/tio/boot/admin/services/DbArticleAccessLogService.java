package com.litongjava.tio.boot.admin.services;

import com.litongjava.db.activerecord.Db;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.tio.boot.admin.vo.AccessLogVo;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.utils.thread.TioThreadUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DbArticleAccessLogService {
  public static final String sql = "INSERT INTO tio_boot_admin_article_access_log (id,channel_id,ip,user_id,method,uri,user_agent,header,body) values(?,?,?,?,?,?,?,?,?)";
  AccessLogService accessLogService = Aop.get(AccessLogService.class);

  public void saveDb(AccessLogVo vo) {
    Db.updateBySql(sql, vo.id, vo.channel_id, vo.clientIp, vo.userId, vo.method, vo.uri, vo.user_agent, vo.header, vo.body);
  }

  public void save(HttpRequest request) {
    AccessLogVo accessLog = accessLogService.parseToAccessLog(request);
    TioThreadUtils.execute(() -> {
      try {
        this.saveDb(accessLog);
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    });
  }
}