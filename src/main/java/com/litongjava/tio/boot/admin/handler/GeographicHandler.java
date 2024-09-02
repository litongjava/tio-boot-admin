package com.litongjava.tio.boot.admin.handler;

import com.litongjava.tio.boot.http.TioRequestContext;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.http.server.model.HttpCors;
import com.litongjava.tio.http.server.util.HttpServerResponseUtils;
import com.litongjava.tio.utils.resp.RespVo;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
public class GeographicHandler {

  public HttpResponse province(HttpRequest httpRequest) {
    HttpResponse response = TioRequestContext.getResponse();
    HttpServerResponseUtils.enableCORS(response, new HttpCors());

    RespVo ok = RespVo.ok(new String[]{});
    response.setJson(ok);

    return response;
  }
}
