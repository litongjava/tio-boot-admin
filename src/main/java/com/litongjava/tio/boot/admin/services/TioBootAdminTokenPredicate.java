package com.litongjava.tio.boot.admin.services;

import java.util.function.Predicate;

import com.litongjava.tio.boot.admin.utils.TioAdminEnvUtils;
import com.litongjava.tio.boot.http.TioRequestContext;
import com.litongjava.tio.utils.jwt.JwtUtils;

public class TioBootAdminTokenPredicate implements Predicate<String> {

  @Override
  public boolean test(String token) {
    String adminToken = TioAdminEnvUtils.getAdminToken();
    // system token
    if (adminToken != null && adminToken.equals(token)) {
      TioRequestContext.setUserId(0L);
      return true;
    }

    // user and admin token
    String key = TioAdminEnvUtils.getAdminSecretKey();
    boolean verify = JwtUtils.verify(key, token);
    if (verify) {
      String userId = JwtUtils.parseUserIdString(token);
      TioRequestContext.setUserId(userId);
      return true;
    }
    return false;
  }

  public Long parseUserIdLong(String token) {
    boolean verify = JwtUtils.verify(TioAdminEnvUtils.getAdminSecretKey(), token);
    if (verify) {
      return JwtUtils.parseUserIdLong(token);
    }
    return null;
  }

  public String parseUserIdString(String token) {

    boolean verify = JwtUtils.verify(TioAdminEnvUtils.getAdminSecretKey(), token);
    if (verify) {
      return JwtUtils.parseUserIdString(token);
    }
    return null;
  }
}
