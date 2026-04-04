package nexus.io.tio.boot.admin.utils;

import nexus.io.tio.utils.json.JsonUtils;

public class PrintlnUtils {

  public static void printJson(Object object) {
    System.out.println(JsonUtils.toJson(object));    
  }
}
