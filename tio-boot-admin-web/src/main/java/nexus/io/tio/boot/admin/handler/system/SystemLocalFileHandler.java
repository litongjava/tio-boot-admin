package nexus.io.tio.boot.admin.handler.system;

import java.io.File;

import com.jfinal.kit.Kv;

import nexus.io.model.body.RespBodyVo;
import nexus.io.model.upload.UploadFile;
import nexus.io.tio.http.common.HttpRequest;
import nexus.io.tio.http.common.HttpResponse;
import nexus.io.tio.http.server.model.HttpCors;
import nexus.io.tio.http.server.util.CORSUtils;
import nexus.io.tio.http.server.util.Resps;
import nexus.io.tio.utils.hutool.FileUtil;
import nexus.io.tio.utils.snowflake.SnowflakeIdUtils;

public class SystemLocalFileHandler {

  public HttpResponse upload(HttpRequest request) throws Exception {
    HttpResponse httpResponse = new HttpResponse(request);
    CORSUtils.enableCORS(httpResponse, new HttpCors());

    UploadFile uploadFile = request.getUploadFile("file");
    Kv kv = Kv.create();
    if (uploadFile != null) {
      byte[] fileData = uploadFile.getData();
      File file = new File(uploadFile.getName());
      FileUtil.writeBytes(fileData, file);
      kv.set("id", SnowflakeIdUtils.id());
    }

    return Resps.json(httpResponse, RespBodyVo.ok(kv));
  }

}
