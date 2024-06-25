package com.litongjava.tio.boot.admin;

import com.litongjava.jfinal.aop.annotation.AComponentScan;
import com.litongjava.tio.boot.TioApplication;

@AComponentScan
public class AdminApp {

  public static void main(String[] args) {
    long start = System.currentTimeMillis();
    //TioApplicationWrapper.run(AdminApp.class, args);
    TioApplication.run(AdminApp.class, args);
    long end = System.currentTimeMillis();
    System.out.println((end - start) + "(ms)");
  }
}