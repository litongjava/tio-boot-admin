package com.litongjava.tio.boot.admin.controller;

import com.litongjava.tio.http.server.annotation.RequestPath;

@RequestPath("/")
public class IndexController {

  @RequestPath("")
  public String index() {
    return "OK";
  }

}
