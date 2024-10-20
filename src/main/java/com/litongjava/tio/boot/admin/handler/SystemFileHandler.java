package com.litongjava.tio.boot.admin.handler;

import java.io.File;

import com.jfinal.kit.Kv;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.http.common.UploadFile;
import com.litongjava.tio.http.server.model.HttpCors;
import com.litongjava.tio.http.server.util.CORSUtils;
import com.litongjava.tio.http.server.util.Resps;
import com.litongjava.tio.utils.hutool.FileUtil;
import com.litongjava.tio.utils.snowflake.SnowflakeIdUtils;

public class SystemFileHandler {

  public HttpResponse upload(HttpRequest request) throws Exception {
    HttpResponse httpResponse = new HttpResponse(request);
    CORSUtils.enableCORS(httpResponse, new HttpCors());

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
      kv.set("id", SnowflakeIdUtils.id());
    }

    return Resps.json(httpResponse, RespBodyVo.ok(kv));
  }

}
