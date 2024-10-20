package com.litongjava.tio.boot.admin.handler;

import java.util.Map;

import com.jfinal.kit.Kv;
import com.jfinal.kit.StrKit;
import com.litongjava.db.TableInput;
import com.litongjava.db.TableResult;
import com.litongjava.db.activerecord.Record;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.table.services.ApiTable;
import com.litongjava.table.utils.TableInputUtils;
import com.litongjava.table.utils.TableResultUtils;
import com.litongjava.tio.boot.admin.costants.TioBootAdminTableNames;
import com.litongjava.tio.boot.admin.services.AwsS3StorageService;
import com.litongjava.tio.boot.admin.vo.UploadResultVo;
import com.litongjava.tio.boot.http.TioRequestContext;
import com.litongjava.tio.boot.utils.TioRequestParamUtils;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.http.common.UploadFile;
import com.litongjava.tio.http.server.model.HttpCors;
import com.litongjava.tio.http.server.util.CORSUtils;
import com.litongjava.tio.http.server.util.Resps;

public class SystemFileAwsS3Handler {

  public HttpResponse upload(HttpRequest request) throws Exception {
    HttpResponse httpResponse = TioRequestContext.getResponse();
    CORSUtils.enableCORS(httpResponse, new HttpCors());
    UploadFile uploadFile = request.getUploadFile("file");
    String category = request.getParam("category");

    AwsS3StorageService storageService = Aop.get(AwsS3StorageService.class);
    if (uploadFile != null) {
      RespBodyVo RespBodyVo = storageService.upload(category, uploadFile);
      return Resps.json(httpResponse, RespBodyVo);
    }
    return Resps.json(httpResponse, RespBodyVo.ok("Fail"));
  }

  public HttpResponse getUploadRecordByMd5(HttpRequest request) throws Exception {
    HttpResponse httpResponse = TioRequestContext.getResponse();
    CORSUtils.enableCORS(httpResponse, new HttpCors());

    Map<String, Object> map = TioRequestParamUtils.getRequestMap(request);
    TableInput kv = TableInputUtils.camelToUnderscore(map);
    // 调用ApiTable查询数据
    TableResult<Record> jsonBean = ApiTable.get(TioBootAdminTableNames.tio_boot_admin_system_upload_file, kv);
    TableResult<Kv> TableResult = TableResultUtils.recordToKv(jsonBean);

    return Resps.json(httpResponse, RespBodyVo.ok(TableResult.getData()).code(TableResult.getCode()).msg(TableResult.getMsg()));
  }

  public HttpResponse getUrl(HttpRequest request) throws Exception {
    HttpResponse httpResponse = TioRequestContext.getResponse();
    CORSUtils.enableCORS(httpResponse, new HttpCors());

    AwsS3StorageService storageService = Aop.get(AwsS3StorageService.class);
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
