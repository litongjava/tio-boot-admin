package com.litongjava.tio.boot.admin.services;

import com.litongjava.tio.boot.admin.utils.TioAdminEnvUtils;
import com.litongjava.tio.boot.token.PredicateResult;
import com.litongjava.tio.boot.token.TokenPredicate;
import com.litongjava.tio.utils.jwt.JwtUtils;

public class TioBootAdminTokenPredicate implements TokenPredicate {

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

  @Override
  public PredicateResult validate(String token) {
    String adminToken = TioAdminEnvUtils.getAdminToken();
    // system token
    if (adminToken != null && adminToken.equals(token)) {
      return new PredicateResult(true, "0");
    }

    // user and admin token
    String key = TioAdminEnvUtils.getAdminSecretKey();
    boolean verify = JwtUtils.verify(key, token);
    if (verify) {
      String userId = JwtUtils.parseUserIdString(token);
      return new PredicateResult(true, userId);
    }
    return new PredicateResult(false);
  }
}
