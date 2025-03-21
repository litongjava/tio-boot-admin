package com.litongjava.tio.boot.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain=true)
public class AccessLogVo {
  public long id;
  public long channel_id;
  public String clientIp;
  public Object userId;
  public String method;
  public String uri;
  public String user_agent;
  public String header;
  public String body;
}
