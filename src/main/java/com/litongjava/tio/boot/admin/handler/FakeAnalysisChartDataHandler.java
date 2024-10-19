package com.litongjava.tio.boot.admin.handler;

import java.io.File;
import java.net.URL;

import com.alibaba.fastjson2.JSONObject;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.tio.boot.http.TioRequestContext;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.http.server.model.HttpCors;
import com.litongjava.tio.http.server.util.CORSUtils;
import com.litongjava.tio.http.server.util.Resps;
import com.litongjava.tio.utils.hutool.FileUtil;
import com.litongjava.tio.utils.hutool.ResourceUtil;
import com.litongjava.tio.utils.json.FastJson2Utils;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
public class FakeAnalysisChartDataHandler {

  public HttpResponse index(HttpRequest request) {
    HttpResponse httpResponse = TioRequestContext.getResponse();
    CORSUtils.enableCORS(httpResponse, new HttpCors());

    String method = request.getMethod().toString();
    if ("OPTIONS".equals(method)) {
      return httpResponse;
    }
    RespBodyVo respVo = null;
    URL resource = ResourceUtil.getResource("data/fake_analysis_chart_data.json");

    if (resource != null) {
      String fileString = resource.getFile();
      try {
        String jsonString = FileUtil.readString(new File(fileString));
        JSONObject jsonObject = FastJson2Utils.parseObject(jsonString);
        respVo = RespBodyVo.ok(jsonObject.getJSONObject("data"));
      } catch (Exception e) {
        e.printStackTrace();
        respVo = RespBodyVo.fail(e.getMessage());
      }
    }
    Resps.json(httpResponse, respVo);

    return httpResponse;
  }
}
