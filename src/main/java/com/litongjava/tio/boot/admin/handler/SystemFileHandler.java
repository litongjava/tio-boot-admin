package com.litongjava.tio.boot.admin.handler;

import java.io.File;
import java.util.Map;

import com.jfinal.kit.Kv;
import com.jfinal.kit.StrKit;
import com.litongjava.data.model.DbJsonBean;
import com.litongjava.data.services.DbJsonService;
import com.litongjava.data.utils.DbJsonBeanUtils;
import com.litongjava.data.utils.KvUtils;
import com.litongjava.data.utils.SnowflakeIdGenerator;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.jfinal.plugin.activerecord.Record;
import com.litongjava.tio.boot.admin.costants.TableNames;
import com.litongjava.tio.boot.admin.services.AwsS3StorageService;
import com.litongjava.tio.boot.admin.services.GoogleStorageService;
import com.litongjava.tio.boot.admin.services.TencentStorageService;
import com.litongjava.tio.boot.http.TioControllerContext;
import com.litongjava.tio.boot.utils.TioRequestParamUtils;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.http.common.UploadFile;
import com.litongjava.tio.http.server.model.HttpCors;
import com.litongjava.tio.http.server.util.HttpServerResponseUtils;
import com.litongjava.tio.http.server.util.Resps;
import com.litongjava.tio.utils.hutool.FileUtil;
import com.litongjava.tio.utils.resp.RespVo;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
public class SystemFileHandler {
  public HttpResponse upload(HttpRequest request) throws Exception {
    HttpResponse httpResponse = new HttpResponse(request);
    HttpServerResponseUtils.enableCORS(httpResponse, new HttpCors());

    String method = request.getMethod();
    if ("OPTIONS".equals(method)) {
      return httpResponse;
    }

    UploadFile uploadFile = request.getUploadFile("file");
    Kv kv = Kv.create();
    if (uploadFile != null) {
      byte[] fileData = uploadFile.getData();
      File file = new File(uploadFile.getName());
      FileUtil.writeBytes(fileData, file);
      long threadId = Thread.currentThread().getId();
      if (threadId > 31L) {
        threadId %= 31L;
      }

      if (threadId < 0L) {
        threadId = 0L;
      }
      String id = (new SnowflakeIdGenerator(threadId, 0L)).generateId() + "";
      kv.set("id", id);
    }

    return Resps.json(httpResponse, RespVo.ok(kv));
  }

  public HttpResponse uploadImageToGoogle(HttpRequest request) throws Exception {
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

  public HttpResponse getGoogleFileUrl(HttpRequest request) {
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

  public HttpResponse uploadToTencentCos(HttpRequest request) throws Exception {
    HttpResponse httpResponse = TioControllerContext.getResponse();
    HttpServerResponseUtils.enableCORS(httpResponse, new HttpCors());

    String method = request.getMethod();
    if ("OPTIONS".equals(method)) {
      return httpResponse;
    }

    UploadFile uploadFile = request.getUploadFile("file");
    TencentStorageService storageService = Aop.get(TencentStorageService.class);
    if (uploadFile != null) {
      RespVo respVo = storageService.upload(uploadFile);
      return Resps.json(httpResponse, respVo);

    }
    return Resps.json(httpResponse, RespVo.ok("Fail"));
  }
}
