package com.litongjava.tio.boot.admin.handler;

import java.net.URL;

import com.litongjava.tio.boot.http.TioRequestContext;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.http.server.model.HttpCors;
import com.litongjava.tio.http.server.util.CORSUtils;
import com.litongjava.tio.utils.hutool.ResourceUtil;

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
    URL resource = ResourceUtil.getResource("data/fake_analysis_chart_data.json");

    if (resource != null) {
      String fileString = resource.getFile();
      httpResponse.body(fileString);
    }
    return httpResponse;
  }
}
