package nexus.io.tio.boot.admin.config;

import org.junit.Test;
import static org.junit.Assert.*;
import nexus.io.tio.boot.http.interceptor.HttpInteceptorConfigure;
import nexus.io.tio.boot.http.interceptor.HttpInterceptorModel;
import nexus.io.tio.boot.server.TioBootServer;

public class InterceptorCompositionTest {
  @Test public void adminPreservesBusinessInterceptorsAndIsIdempotent() {
    var server = TioBootServer.me();
    var previous = server.getHttpInteceptorConfigure();
    try {
      var config = new HttpInteceptorConfigure();
      var business = new HttpInterceptorModel(); business.setName("business"); config.add(business);
      server.setHttpInteceptorConfigure(config);
      new TioAdminInterceptorConfiguration(new String[] {"/api/mi/**"}).config();
      new TioAdminInterceptorConfiguration(new String[] {"/api/mi/**"}).config();
      assertSame(config, server.getHttpInteceptorConfigure());
      assertSame(business, config.getInteceptors().get("business"));
      assertEquals(2, config.getInteceptors().size());
      assertTrue(config.getInteceptors().containsKey("tio-admin-token"));
    } finally { server.setHttpInteceptorConfigure(previous); }
  }
}
