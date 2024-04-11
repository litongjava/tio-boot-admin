package com.litongjava.tio.boot.admin.handler;

import com.jfinal.kit.Kv;
import com.litongjava.data.utils.SnowflakeIdGenerator;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.http.common.UploadFile;
import com.litongjava.tio.http.server.util.Resps;
import com.litongjava.tio.utils.hutool.FileUtil;
import com.litongjava.tio.utils.resp.RespVo;

import java.io.File;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
public class SystemFileHandler {
  public HttpResponse upload(HttpRequest request) throws Exception {
    UploadFile uploadFile = request.getUploadFile("file");
    if (uploadFile != null) {
      byte[] fileData = uploadFile.getData();
      File file = new File(uploadFile.getName());
      FileUtil.writeBytes(fileData, file);
    }
    long threadId = Thread.currentThread().getId();
    if (threadId > 31L) {
      threadId %= 31L;
    }

    if (threadId < 0L) {
      threadId = 0L;
    }
    String id = (new SnowflakeIdGenerator(threadId, 0L)).generateId() + "";
    Kv kv = Kv.create();
    kv.set("id", id);
    return Resps.json(request, RespVo.ok(id));
  }
}
