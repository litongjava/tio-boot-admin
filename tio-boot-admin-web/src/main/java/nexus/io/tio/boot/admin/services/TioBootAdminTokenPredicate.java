package nexus.io.tio.boot.admin.services;

import nexus.io.tio.boot.admin.utils.TioAdminEnvUtils;
import nexus.io.tio.boot.token.PredicateResult;
import nexus.io.tio.boot.token.TokenPredicate;
import nexus.io.tio.utils.jwt.JwtUtils;

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
