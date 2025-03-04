package com.litongjava.tio.boot.admin.services;

import com.litongjava.db.activerecord.Db;
import com.litongjava.db.activerecord.Row;
import com.litongjava.tio.utils.mcid.McIdUtils;

public class DbRequestResponseStatisticsService {
  public void save(long channelId, long requestId, String requestIp, String requestUri, String requestHeader, String contentType,
      //
      String requestBody, int responseStatusCode, String responseBody, Object userId, long elapsed) {
    Row row = new Row();
    if (userId != null) {
      if (userId instanceof String) {
        row.set("user_id", (String) userId);
      } else if (userId instanceof Long) {
        row.set("user_id", userId.toString());
      }
    }
    row.set("id", McIdUtils.id());
    row.set("channel_id", channelId);
    row.set("request_id", requestId);
    row.set("request_ip", requestIp);
    row.set("request_uri", requestUri);
    row.set("request_header", requestHeader);
    row.set("request_content_type", contentType);
    row.set("request_body", requestBody);
    row.set("response_status_code", responseStatusCode);
    row.set("response_body", responseBody);
    row.set("elapsed", elapsed);

    try {
      Db.save("sys_http_request_response_statistics", row);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}