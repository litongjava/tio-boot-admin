package com.litongjava.tio.boot.admin.services;

import org.junit.BeforeClass;
import org.junit.Test;

import com.litongjava.jfinal.aop.Aop;
import com.litongjava.tio.boot.admin.config.DbConfig;
import com.litongjava.tio.boot.admin.vo.SystemTxCosConfigVo;
import com.litongjava.tio.boot.tesing.TioBootTest;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
public class SysConfigConstantsServiceTest {

  @BeforeClass
  public static void beforeClass() {
    TioBootTest.runWith(DbConfig.class);
  }

  @Test
  public void testGetValue() {
    SystemTxCosConfigVo systemTxCosConfig = Aop.get(SysConfigConstantsService.class).getSystemTxCosConfig();
    System.out.println(systemTxCosConfig);
  }

}