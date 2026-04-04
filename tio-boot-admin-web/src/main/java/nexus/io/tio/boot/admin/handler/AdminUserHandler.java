package nexus.io.tio.boot.admin.handler;

import nexus.io.jfinal.aop.Aop;
import nexus.io.model.body.RespBodyVo;
import nexus.io.tio.boot.admin.services.AdminUserService;
import nexus.io.tio.boot.http.TioRequestContext;
import nexus.io.tio.http.common.HttpRequest;
import nexus.io.tio.http.common.HttpResponse;
import nexus.io.tio.http.server.model.HttpCors;
import nexus.io.tio.http.server.util.CORSUtils;
import nexus.io.tio.http.server.util.Resps;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
public class AdminUserHandler {

  public HttpResponse currentUser(HttpRequest request) {
    HttpResponse httpResponse = TioRequestContext.getResponse();
    CORSUtils.enableCORS(httpResponse, new HttpCors());
    String userIdString = TioRequestContext.getUserIdString();
    if(userIdString==null) {
      httpResponse.setStatus(401);
      return httpResponse;
    }
    Long userId = Long.valueOf(userIdString);
    RespBodyVo respVo = Aop.get(AdminUserService.class).currentUser(userId);
    httpResponse.body(respVo);
    return httpResponse;
  }

  public HttpResponse accountSettingCurrentUser(HttpRequest request) {
    HttpResponse httpResponse = TioRequestContext.getResponse();
    CORSUtils.enableCORS(httpResponse, new HttpCors());
    Long userId = Long.valueOf(TioRequestContext.getUserIdLong());
    RespBodyVo respVo = Aop.get(AdminUserService.class).currentUser(userId);
    return Resps.json(httpResponse, respVo);
  }
}
