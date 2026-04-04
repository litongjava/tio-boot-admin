package nexus.io.tio.boot.admin.config;

import java.util.ArrayList;
import java.util.List;

import nexus.io.tio.boot.admin.controller.ApiTableController;
import nexus.io.tio.boot.http.handler.controller.TioBootHttpControllerRouter;
import nexus.io.tio.boot.server.TioBootServer;

public class TioAdminControllerConfiguration {

  public void config() {
    TioBootHttpControllerRouter controllerRouter = TioBootServer.me().getControllerRouter();
    if (controllerRouter == null) {
      return;
    }
    List<Class<?>> scannedClasses = new ArrayList<>();
    scannedClasses.add(ApiTableController.class);
    //scannedClasses.add(MongodbController.class);
    controllerRouter.addControllers(scannedClasses);
  }
}
