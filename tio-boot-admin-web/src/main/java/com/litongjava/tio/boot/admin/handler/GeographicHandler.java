package com.litongjava.tio.boot.admin.handler;

import com.litongjava.tio.boot.http.TioRequestContext;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.http.server.model.HttpCors;
import com.litongjava.tio.http.server.util.CORSUtils;

import nexus.io.model.body.RespBodyVo;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
public class GeographicHandler {

  public HttpResponse province(HttpRequest httpRequest) {
    HttpResponse response = TioRequestContext.getResponse();
    CORSUtils.enableCORS(response, new HttpCors());

    RespBodyVo ok = RespBodyVo.ok(new String[]{});
    response.setJson(ok);

    return response;
  }
}
