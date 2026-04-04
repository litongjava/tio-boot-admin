package nexus.io.tio.boot.admin.services;

import lombok.extern.slf4j.Slf4j;
import nexus.io.db.activerecord.Db;
import nexus.io.jfinal.aop.Aop;
import nexus.io.tio.boot.admin.vo.AccessLogVo;
import nexus.io.tio.http.common.HttpRequest;
import nexus.io.tio.utils.thread.TioThreadUtils;

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