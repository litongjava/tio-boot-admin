package nexus.io.tio.boot.admin.handler.system;

import nexus.io.jfinal.aop.Aop;
import nexus.io.model.body.RespBodyVo;
import nexus.io.model.upload.UploadFile;
import nexus.io.model.upload.UploadResult;
import nexus.io.tio.boot.admin.services.storage.GoogleStorageService;
import nexus.io.tio.boot.http.TioRequestContext;
import nexus.io.tio.http.common.HttpRequest;
import nexus.io.tio.http.common.HttpResponse;
import nexus.io.tio.http.server.model.HttpCors;
import nexus.io.tio.http.server.util.CORSUtils;
import nexus.io.tio.http.server.util.Resps;

public class SystemFileFirebaseHandler {

  public HttpResponse upload(HttpRequest request) throws Exception {
    HttpResponse httpResponse = TioRequestContext.getResponse();
    CORSUtils.enableCORS(httpResponse, new HttpCors());

    String method = request.getMethod().toString();
    if ("OPTIONS".equals(method)) {
      return httpResponse;
    }

    UploadFile uploadFile = request.getUploadFile("file");
    GoogleStorageService googleStorageService = Aop.get(GoogleStorageService.class);
    if (uploadFile != null) {
      RespBodyVo respVo = googleStorageService.upload(uploadFile);
      return Resps.json(httpResponse, respVo);

    }
    return Resps.json(httpResponse, RespBodyVo.ok("Fail"));
  }

  public HttpResponse getUrl(HttpRequest request) {
    HttpResponse httpResponse = TioRequestContext.getResponse();
    CORSUtils.enableCORS(httpResponse, new HttpCors());

    String method = request.getMethod().toString();
    if ("OPTIONS".equals(method)) {
      return httpResponse;
    }

    long fileId = Long.parseLong(request.getParam("id"));
    GoogleStorageService googleStorageService = Aop.get(GoogleStorageService.class);
    UploadResult result = googleStorageService.getUrlById(fileId);
    String url = result.getUrl();
    return Resps.json(httpResponse, RespBodyVo.ok(url));
  }
}
