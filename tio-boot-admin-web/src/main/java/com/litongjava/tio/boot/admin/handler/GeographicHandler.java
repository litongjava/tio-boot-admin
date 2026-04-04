package com.litongjava.tio.boot.admin.handler;

import nexus.io.model.body.RespBodyVo;
import nexus.io.tio.boot.http.TioRequestContext;
import nexus.io.tio.http.common.HttpRequest;
import nexus.io.tio.http.common.HttpResponse;
import nexus.io.tio.http.server.model.HttpCors;
import nexus.io.tio.http.server.util.CORSUtils;

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
