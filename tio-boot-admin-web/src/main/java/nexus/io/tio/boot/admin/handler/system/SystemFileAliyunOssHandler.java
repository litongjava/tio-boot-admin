package nexus.io.tio.boot.admin.handler.system;

import java.util.Map;

import com.jfinal.kit.Kv;
import com.jfinal.kit.StrKit;

import nexus.io.db.TableInput;
import nexus.io.db.TableResult;
import nexus.io.db.activerecord.Row;
import nexus.io.jfinal.aop.Aop;
import nexus.io.model.body.RespBodyVo;
import nexus.io.model.upload.UploadFile;
import nexus.io.model.upload.UploadResult;
import nexus.io.table.services.ApiTable;
import nexus.io.table.utils.TableInputUtils;
import nexus.io.table.utils.TableResultUtils;
import nexus.io.tio.boot.admin.consts.TioBootAdminTableNames;
import nexus.io.tio.boot.admin.services.storage.AliyunStorageService;
import nexus.io.tio.boot.http.TioRequestContext;
import nexus.io.tio.boot.utils.TioRequestParamUtils;
import nexus.io.tio.http.common.HttpRequest;
import nexus.io.tio.http.common.HttpResponse;
import nexus.io.tio.http.server.model.HttpCors;
import nexus.io.tio.http.server.util.CORSUtils;
import nexus.io.tio.http.server.util.Resps;

public class SystemFileAliyunOssHandler {

  AliyunStorageService storageService = Aop.get(AliyunStorageService.class);
  
  public HttpResponse upload(HttpRequest request) throws Exception {
    HttpResponse httpResponse = TioRequestContext.getResponse();
    CORSUtils.enableCORS(httpResponse, new HttpCors());
    UploadFile uploadFile = request.getUploadFile("file");
    String category = request.getParam("category");

    
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
    TableResult<Row> jsonBean = ApiTable.get(TioBootAdminTableNames.tio_boot_admin_system_upload_file, kv);
    TableResult<Kv> TableResult = TableResultUtils.recordToKv(jsonBean);

    return Resps.json(httpResponse, RespBodyVo.ok(TableResult.getData()).code(TableResult.getCode()).msg(TableResult.getMsg()));
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
