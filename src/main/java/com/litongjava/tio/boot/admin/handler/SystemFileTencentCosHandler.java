package com.litongjava.tio.boot.admin.handler;

import com.jfinal.kit.StrKit;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.tio.boot.admin.services.storage.TencentStorageService;
import com.litongjava.tio.boot.admin.vo.UploadResultVo;
import com.litongjava.tio.boot.http.TioRequestContext;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.http.common.UploadFile;
import com.litongjava.tio.http.server.model.HttpCors;
import com.litongjava.tio.http.server.util.CORSUtils;
import com.litongjava.tio.http.server.util.Resps;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
public class SystemFileTencentCosHandler {

  public HttpResponse upload(HttpRequest request) throws Exception {
    HttpResponse httpResponse = TioRequestContext.getResponse();
    CORSUtils.enableCORS(httpResponse, new HttpCors());

    UploadFile uploadFile = request.getUploadFile("file");
    TencentStorageService storageService = Aop.get(TencentStorageService.class);
    if (uploadFile != null) {
      RespBodyVo respVo = storageService.upload(uploadFile);
      return Resps.json(httpResponse, respVo);

    }
    return Resps.json(httpResponse, RespBodyVo.ok("Fail"));
  }
  
  public HttpResponse getUrl(HttpRequest request) throws Exception {
    HttpResponse httpResponse = TioRequestContext.getResponse();
    CORSUtils.enableCORS(httpResponse, new HttpCors());

    TencentStorageService storageService = Aop.get(TencentStorageService.class);
    RespBodyVo respBodyVo = null;
    String id = request.getParam("id");

    String md5 = request.getParam("md5");
    if (StrKit.notBlank(id)) {
      // id,md5,name,url,
      UploadResultVo uploadResultVo = storageService.getUrlById(id);
      if (uploadResultVo == null) {
        respBodyVo = RespBodyVo.fail();
      } else {
        respBodyVo = RespBodyVo.ok(uploadResultVo);
      }

    } else if (StrKit.notBlank(md5)) {
      UploadResultVo uploadResultVo = storageService.getUrlByMd5(md5);
      if (uploadResultVo == null) {
        respBodyVo = RespBodyVo.fail();
      } else {
        respBodyVo = RespBodyVo.ok(uploadResultVo);
      }
    } else {
      respBodyVo = RespBodyVo.fail("id or md5 can not be empty");
    }

    return Resps.json(httpResponse, respBodyVo);
  }
}
