package com.litongjava.tio.boot.admin.handler;

import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.http.common.UploadFile;
import com.litongjava.tio.http.server.util.Resps;
import com.litongjava.tio.utils.hutool.FileUtil;

import java.io.File;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
public class SystemUploadHandler {
  public HttpResponse upload(HttpRequest request) throws Exception {
    UploadFile uploadFile = request.getUploadFile("file");
    if (uploadFile != null) {
      byte[] fileData = uploadFile.getData();
      File file = new File(uploadFile.getName());
      FileUtil.writeBytes(fileData, file);
    }
    return Resps.json(request, "success");
  }
}
