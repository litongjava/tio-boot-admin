package com.litongjava.tio.boot.admin.config;

import com.litongjava.jfinal.aop.annotation.AConfiguration;
import com.litongjava.jfinal.aop.annotation.AInitialization;
import com.litongjava.tio.utils.json.FastJson2Factory;
import com.litongjava.tio.utils.json.Json;

@AConfiguration
public class JsonConfig {

  @AInitialization
  public void config() {
    Json.setDefaultJsonFactory(new FastJson2Factory());
  }
}
