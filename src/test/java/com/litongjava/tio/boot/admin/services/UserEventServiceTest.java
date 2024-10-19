package com.litongjava.tio.boot.admin.services;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.fastjson2.JSONObject;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.tio.boot.admin.config.DbConfig;
import com.litongjava.tio.boot.tesing.TioBootTest;

public class UserEventServiceTest {

  @Before
  public void before() {
    TioBootTest.runWith(DbConfig.class);
  }

  @Test
  public void save() {
    String eventName = "sign_in";
    JSONObject eventValue = new JSONObject();
    eventValue.put("username", "Tong Li");
    eventValue.put("email", "litongjava001@gamil.com");
    Aop.get(UserEventService.class).save(eventName, eventValue);
  }
}