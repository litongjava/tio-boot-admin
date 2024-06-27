package com.litongjava.tio.boot.admin.handler;

import java.util.Map;

import com.jfinal.kit.Kv;
import com.jfinal.kit.StrKit;
import com.litongjava.data.model.DbJsonBean;
import com.litongjava.data.services.DbJsonService;
import com.litongjava.data.utils.DbJsonBeanUtils;
import com.litongjava.data.utils.KvUtils;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.jfinal.plugin.activerecord.Record;
import com.litongjava.tio.boot.admin.costants.TableNames;
import com.litongjava.tio.boot.admin.services.AwsS3StorageService;
import com.litongjava.tio.boot.http.TioHttpContext;
import com.litongjava.tio.boot.utils.TioRequestParamUtils;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.http.common.UploadFile;
import com.litongjava.tio.http.server.model.HttpCors;
import com.litongjava.tio.http.server.util.HttpServerResponseUtils;
import com.litongjava.tio.http.server.util.Resps;
import com.litongjava.tio.utils.resp.RespVo;

public class SystemFileS3Handler {

  public HttpResponse upload(HttpRequest request) throws Exception {
    HttpResponse httpResponse = TioHttpContext.getResponse();
    HttpServerResponseUtils.enableCORS(httpResponse, new HttpCors());

    String method = request.getMethod();
    if ("OPTIONS".equals(method)) {
      return httpResponse;
    }

    UploadFile uploadFile = request.getUploadFile("file");
    String category = request.getParam("category");

    AwsS3StorageService storageService = Aop.get(AwsS3StorageService.class);
    if (uploadFile != null) {
      RespVo respVo = storageService.upload(category, uploadFile);
      return Resps.json(httpResponse, respVo);
    }
    return Resps.json(httpResponse, RespVo.ok("Fail"));
  }

  public HttpResponse getUploadRecordByMd5(HttpRequest request) throws Exception {
    HttpResponse httpResponse = TioHttpContext.getResponse();
    HttpServerResponseUtils.enableCORS(httpResponse, new HttpCors());

    String method = request.getMethod();
    if ("OPTIONS".equals(method)) {
      return httpResponse;
    }

    // 调用DbJsonService查询数据
    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    Kv kv = KvUtils.camelToUnderscore(map);
    DbJsonBean<Record> jsonBean = Aop.get(DbJsonService.class).get(TableNames.tio_boot_admin_system_upload_file, kv);
    DbJsonBean<Kv> dbJsonBean = DbJsonBeanUtils.recordToKv(jsonBean);

    return Resps.json(httpResponse,
        RespVo.ok(dbJsonBean.getData()).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg()));
  }

  public HttpResponse getUrl(HttpRequest request) throws Exception {
    HttpResponse httpResponse = TioHttpContext.getResponse();
    HttpServerResponseUtils.enableCORS(httpResponse, new HttpCors());

    String method = request.getMethod();
    if ("OPTIONS".equals(method)) {
      return httpResponse;
    }

    AwsS3StorageService storageService = Aop.get(AwsS3StorageService.class);
    RespVo respVo = null;
    String id = request.getParam("id");

    String md5 = request.getParam("md5");
    if (StrKit.notBlank(id)) {
      // id,md5,name,url,
      Kv kv = storageService.getUrlById(id);
      if (kv == null) {
        respVo = RespVo.fail();
      } else {
        respVo = RespVo.ok(kv);
      }

    } else if (StrKit.notBlank(md5)) {
      Kv kv = storageService.getUrlByMd5(md5);
      if (kv == null) {
        respVo = RespVo.fail();
      } else {
        respVo = RespVo.ok(kv);
      }
    } else {
      respVo = RespVo.fail("id or md5 can not be empty");
    }

    return Resps.json(httpResponse, respVo);
  }
}
