package com.litongjava.tio.boot.admin.handler;

import com.litongjava.tio.boot.http.TioRequestContext;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;

public class SpeedTestHandler {

  /**
   * 测速接口：GET /speed/test?size={size in MB}
   */
  public HttpResponse output(HttpRequest request) {
    HttpResponse httpResponse = TioRequestContext.getResponse();
    // 读取 size 参数（单位 MB）
    Long sizeMb = request.getLong("size");
    if (sizeMb == null || sizeMb < 0) {
      sizeMb = 500L;
    }

    // 计算字节数
    long totalBytes = sizeMb * 1024L * 1024L;

    // 生成指定大小的零字节数组
    byte[] payload = new byte[(int) totalBytes];

    // 构造响应
    httpResponse.disableGzip(true);
    return httpResponse.ok(payload);
  }
}
