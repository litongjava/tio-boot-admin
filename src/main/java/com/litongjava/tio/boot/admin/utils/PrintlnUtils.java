package com.litongjava.tio.boot.admin.utils;

import com.litongjava.tio.utils.json.JsonUtils;

public class PrintlnUtils {

  public static void printJson(Object object) {
    System.out.println(JsonUtils.toJson(object));    
  }
}
