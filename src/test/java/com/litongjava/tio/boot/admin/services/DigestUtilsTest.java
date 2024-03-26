package com.litongjava.tio.boot.admin.services;

import cn.hutool.crypto.digest.DigestUtil;
import org.junit.Test;

/**
 * Created by litonglinux@qq.com on 3/25/2024_8:54 PM
 */
public class DigestUtilsTest {

  @Test
  public void test() {
    String hashedPassword = DigestUtil.sha256Hex("admin");
    System.out.println(hashedPassword);
  }
}
