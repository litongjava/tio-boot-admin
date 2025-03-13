package com.litongjava.tio.boot.admin.services;

import java.util.function.Predicate;

import com.litongjava.tio.boot.admin.costants.AppConstant;
import com.litongjava.tio.boot.http.TioRequestContext;
import com.litongjava.tio.utils.environment.EnvUtils;
import com.litongjava.tio.utils.jwt.JwtUtils;

public class TokenPredicate implements Predicate<String> {

  @Override
  public boolean test(String token) {
    String adminToken = EnvUtils.get(AppConstant.ADMIN_TOKEN);
    //system token
    if (adminToken != null && adminToken.equals(token)) {
      TioRequestContext.setUserId(0L);
      return true;
    }

    // user and admin token
    String key = EnvUtils.getStr(AppConstant.ADMIN_SECRET_KEY);
    boolean verify = JwtUtils.verify(key, token);
    if (verify) {
      String userId = JwtUtils.parseUserIdString(token);
      TioRequestContext.setUserId(userId);
      return true;
    }
    return false;
  }

  public Long parseUserIdLong(String token) {
    String key = EnvUtils.getStr(AppConstant.ADMIN_SECRET_KEY);
    boolean verify = JwtUtils.verify(key, token);
    if (verify) {
      return JwtUtils.parseUserIdLong(token);
    }
    return null;
  }
}
