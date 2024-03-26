package com.litongjava.tio.boot.admin;

import com.litongjava.hotswap.wrapper.tio.boot.TioApplicationWrapper;
import com.litongjava.jfinal.aop.annotation.AComponentScan;

@AComponentScan
public class AdminApp {

  public static void main(String[] args) {
    TioApplicationWrapper.run(AdminApp.class, args);
  }
}
