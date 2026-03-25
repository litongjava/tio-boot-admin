package com.litongjava.tio.boot.admin.handler.system;

import java.util.Map;

import com.jfinal.kit.Kv;
import com.jfinal.kit.StrKit;
import com.litongjava.db.TableInput;
import com.litongjava.db.TableResult;
import com.litongjava.db.activerecord.Row;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.model.upload.UploadFile;
import com.litongjava.model.upload.UploadResult;
import com.litongjava.table.services.ApiTable;
import com.litongjava.table.utils.TableInputUtils;
import com.litongjava.table.utils.TableResultUtils;
import com.litongjava.tio.boot.admin.costants.TioBootAdminTableNames;
import com.litongjava.tio.boot.admin.services.storage.UniStorageService;
import com.litongjava.tio.boot.http.TioRequestContext;
import com.litongjava.tio.boot.utils.TioRequestParamUtils;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.http.common.utils.HttpIpUtils;
import com.litongjava.tio.http.server.model.HttpCors;
import com.litongjava.tio.http.server.util.CORSUtils;
import com.litongjava.tio.http.server.util.Resps;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SystemCloudFileHandler {

  UniStorageService storageService = Aop.get(UniStorageService.class);

  public HttpResponse upload(HttpRequest request) throws Exception {

    HttpResponse httpResponse = TioRequestContext.getResponse();
    CORSUtils.enableCORS(httpResponse, new HttpCors());
    UploadFile uploadFile = request.getUploadFile("file");
    String category = request.getParam("category");

    if (uploadFile == null) {
      String userAgent = request.getUserAgent();
      String realIp = HttpIpUtils.getRealIp(request);
      log.error("from {} {} file is emtpy", realIp, userAgent);
      return Resps.json(httpResponse, RespBodyVo.fail("file can not be empty"));
    }
    RespBodyVo RespBodyVo = storageService.upload(category, uploadFile);
    return Resps.json(httpResponse, RespBodyVo);
  }

  public HttpResponse getUploadRecordByMd5(HttpRequest request) throws Exception {
    HttpResponse httpResponse = TioRequestContext.getResponse();
    CORSUtils.enableCORS(httpResponse, new HttpCors());

    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    TableInput kv = TableInputUtils.camelToUnderscore(map);
    // 调用ApiTable查询数据
    TableResult<Row> jsonBean = ApiTable.get(TioBootAdminTableNames.tio_boot_admin_system_upload_file, kv);
    TableResult<Kv> TableResult = TableResultUtils.recordToKv(jsonBean);

    return Resps.json(httpResponse,
        RespBodyVo.ok(TableResult.getData()).code(TableResult.getCode()).msg(TableResult.getMsg()));
  }

  public HttpResponse getUrl(HttpRequest request) throws Exception {
    HttpResponse httpResponse = TioRequestContext.getResponse();
    CORSUtils.enableCORS(httpResponse, new HttpCors());

    RespBodyVo respBodyVo = null;
    String id = request.getParam("id");

    String md5 = request.getParam("md5");
    if (StrKit.notBlank(id)) {
      // id,md5,name,url,
      UploadResult uploadResultVo = storageService.getUrlById(id);
      if (uploadResultVo == null) {
        respBodyVo = RespBodyVo.fail();
      } else {
        respBodyVo = RespBodyVo.ok(uploadResultVo);
      }

    } else if (StrKit.notBlank(md5)) {
      UploadResult uploadResultVo = storageService.getUrlByMd5(md5);
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
