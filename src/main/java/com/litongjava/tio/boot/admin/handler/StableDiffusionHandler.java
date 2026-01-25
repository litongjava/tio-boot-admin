package com.litongjava.tio.boot.admin.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.litongjava.jfinal.aop.Aop;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.model.upload.UploadFile;
import com.litongjava.tio.boot.admin.services.StableDiffusionService;
import com.litongjava.tio.boot.http.TioRequestContext;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.http.server.model.HttpCors;
import com.litongjava.tio.http.server.util.CORSUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
@Slf4j
public class StableDiffusionHandler {

  public HttpResponse generateSd3(HttpRequest request) {
    HttpResponse response = TioRequestContext.getResponse();
    CORSUtils.enableCORS(response, new HttpCors());
    UploadFile uploadFile = request.getUploadFile("image");

    Map<String, Object> requestMap = new HashMap<>();
    // 值全部是String类型
    Map<String, Object[]> parameterMap = request.getParameterMap();
    // remove image
    parameterMap.remove("image");
    // Form data handling

    Set<Map.Entry<String, Object[]>> entries = parameterMap.entrySet();
    for (Map.Entry<String, Object[]> entry : entries) {
      String paramName = entry.getKey();
      requestMap.put(paramName, entry.getValue()[0]);
    }

    RespBodyVo respVo = Aop.get(StableDiffusionService.class).generateSd3(uploadFile, requestMap);
    response.setJson(respVo);
    log.info("parameterMap:{}", requestMap);
    return response;
  }
}
