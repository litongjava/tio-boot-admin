package com.litongjava.tio.boot.admin.services;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.URLUtil;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
public class GoogleStorageServiceTest {


  @Test
  public void getSuffix() {
    String filename = "001.jpg";
    String suffix = FileNameUtil.getSuffix(filename);
    System.out.println(suffix);
  }

  @Test
  public void urlEncode() {
    String encode = URLUtil.encode("public/images/367971535077531648.png");
    System.out.println(encode);
  }
}