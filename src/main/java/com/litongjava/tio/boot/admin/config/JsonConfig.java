package com.litongjava.tio.boot.admin.config;

import com.litongjava.jfinal.aop.annotation.AConfiguration;
import com.litongjava.jfinal.aop.annotation.AInitialization;
import com.litongjava.tio.utils.json.FastJsonFactory;
import com.litongjava.tio.utils.json.Json;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
@AConfiguration
public class JsonConfig {

  @AInitialization
  public void config() {
    Json.setDefaultJsonFactory(new FastJsonFactory());
  }
}
