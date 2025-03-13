package com.litongjava.tio.boot.admin.utils;

import java.io.File;
import java.io.IOException;

import com.maxmind.geoip2.DatabaseReader;

public class GeoLite2Utils {
  // 读取数据库内容
  public static DatabaseReader reader;
  static {
    String path = "GeoLite2-City.mmdb";
    // 创建 GeoLite2 数据库
    File database = new File(path);
    try {
      reader = new DatabaseReader.Builder(database).build();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static DatabaseReader getDatabase() {
    return reader;
  }

}
