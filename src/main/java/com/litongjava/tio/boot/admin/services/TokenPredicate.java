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
    //admin token
    boolean equals = adminToken.equals(token);
    if (equals) {
      TioRequestContext.setUserId(0L);
      return true;
    }
    // user token
    String key = EnvUtils.getStr(AppConstant.ADMIN_SECRET_KEY);
    boolean verify = JwtUtils.verify(key, token);
    if (verify) {
      Long userId = JwtUtils.parseUserIdLong(token);
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
