package com.litongjava.tio.boot.admin.handler;

import java.net.URL;

import nexus.io.tio.boot.http.TioRequestContext;
import nexus.io.tio.http.common.HttpRequest;
import nexus.io.tio.http.common.HttpResponse;
import nexus.io.tio.http.server.model.HttpCors;
import nexus.io.tio.http.server.util.CORSUtils;
import nexus.io.tio.utils.hutool.ResourceUtil;

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
