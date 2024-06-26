package com.litongjava.tio.boot.admin.handler;

import com.litongjava.jfinal.aop.Aop;
import com.litongjava.tio.boot.admin.services.GoogleStorageService;
import com.litongjava.tio.boot.http.TioControllerContext;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.http.common.UploadFile;
import com.litongjava.tio.http.server.model.HttpCors;
import com.litongjava.tio.http.server.util.HttpServerResponseUtils;
import com.litongjava.tio.http.server.util.Resps;
import com.litongjava.tio.utils.resp.RespVo;

public class SystemFileFirebaseHandler {

  public HttpResponse upload(HttpRequest request) throws Exception {
    HttpResponse httpResponse = TioControllerContext.getResponse();
    HttpServerResponseUtils.enableCORS(httpResponse, new HttpCors());

    String method = request.getMethod();
    if ("OPTIONS".equals(method)) {
      return httpResponse;
    }

    UploadFile uploadFile = request.getUploadFile("file");
    GoogleStorageService googleStorageService = Aop.get(GoogleStorageService.class);
    if (uploadFile != null) {
      RespVo respVo = googleStorageService.uploadImageToGoogle(uploadFile);
      return Resps.json(httpResponse, respVo);

    }
    return Resps.json(httpResponse, RespVo.ok("Fail"));
  }

  public HttpResponse getUrl(HttpRequest request) {
    HttpResponse httpResponse = TioControllerContext.getResponse();
    HttpServerResponseUtils.enableCORS(httpResponse, new HttpCors());

    String method = request.getMethod();
    if ("OPTIONS".equals(method)) {
      return httpResponse;
    }

    long fileId = Long.parseLong(request.getParam("id"));
    GoogleStorageService googleStorageService = Aop.get(GoogleStorageService.class);
    String url = googleStorageService.getUrlByFileId(fileId);
    return Resps.json(httpResponse, RespVo.ok(url));
  }
}
