package com.litongjava.tio.boot.admin.config;

import java.util.ArrayList;
import java.util.List;

import com.litongjava.tio.boot.admin.controller.ApiTableController;
import com.litongjava.tio.boot.admin.controller.MongodbController;
import com.litongjava.tio.boot.http.router.TioBootHttpControllerRouter;
import com.litongjava.tio.boot.server.TioBootServer;

public class TioAdminControllerConfiguration {

  public void config() {
    TioBootHttpControllerRouter controllerRouter = TioBootServer.me().getControllerRouter();
    if (controllerRouter == null) {
      return;
    }
    List<Class<?>> scannedClasses = new ArrayList<>();
    scannedClasses.add(ApiTableController.class);
    scannedClasses.add(MongodbController.class);
    controllerRouter.addControllers(scannedClasses);
  }
}
