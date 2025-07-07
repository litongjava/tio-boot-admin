package com.litongjava.tio.boot.admin.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

import com.jfinal.kit.Kv;
import com.litongjava.db.activerecord.Db;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.model.validate.ValidateResult;
import com.litongjava.tio.boot.admin.costants.AppConstant;
import com.litongjava.tio.boot.admin.costants.TioBootAdminTableNames;
import com.litongjava.tio.boot.admin.vo.AppUser;
import com.litongjava.tio.boot.admin.vo.AppUserRegisterRequest;
import com.litongjava.tio.boot.admin.vo.UserResetPasswordRequest;
import com.litongjava.tio.utils.environment.EnvUtils;
import com.litongjava.tio.utils.jwt.JwtUtils;
import com.litongjava.tio.utils.snowflake.SnowflakeIdUtils;
import com.litongjava.tio.utils.validator.EmailValidator;
import com.litongjava.tio.utils.validator.PasswordValidator;

public class AppUserService {

  public boolean registerUserByUserId(AppUserRegisterRequest req, String origin) {
    String password = req.getPassword();
    String email = req.getEmail();
    int userType = req.getUserType();
    boolean exists = Db.exists(TioBootAdminTableNames.app_users, "email", email);
    if (exists) {
      return true;
    }
    // 生成加盐字符串（示例中直接使用随机数，实际可使用更复杂逻辑）
    String salt = String.valueOf(System.currentTimeMillis());
    // 生成密码哈希（密码+盐）
    String passwordHash = DigestUtils.sha256Hex(password + salt);

    // 插入用户记录（id 这里简单采用 email 作为唯一标识）
    long id = SnowflakeIdUtils.id();
    String insertSql = "update app_users set email=?, password_salt=?, password_hash=?, user_type=?,of=? where id=?";
    int rows = Db.updateBySql(insertSql, email, salt, passwordHash, userType, origin, id + "");
    return rows > 0;
  }

  // 注册用户：先检查邮箱是否已存在，然后插入用户记录
  public boolean registerUser(String email, String password, int userType, String orgin) {
    boolean exists = Db.exists(TioBootAdminTableNames.app_users, "email", email);
    if (exists) {
      return true;
    }
    // 生成加盐字符串（示例中直接使用随机数，实际可使用更复杂逻辑）
    String salt = String.valueOf(System.currentTimeMillis());
    // 生成密码哈希（密码+盐）
    String passwordHash = DigestUtils.sha256Hex(password + salt);

    // 插入用户记录（id 这里简单采用 email 作为唯一标识）
    long id = SnowflakeIdUtils.id();
    String insertSql = "INSERT INTO app_users (id, email, password_salt, password_hash, user_type,of) VALUES (?,?,?,?,?,?)";
    int rows = Db.updateBySql(insertSql, id + "", email, salt, passwordHash, userType, orgin);
    return rows > 0;
  }

  // 根据邮箱获取用户信息
  public AppUser getUserByEmail(String email) {
    String sql = "SELECT * FROM app_users WHERE email=? AND deleted=0";
    return Db.findFirst(AppUser.class, sql, email);
  }

  public AppUser getUserById(Long userId) {
    String sql = "SELECT * FROM app_users WHERE id=? AND deleted=0";
    return Db.findFirst(AppUser.class, sql, userId);
  }

  // 校验用户密码
  public boolean verifyPassword(AppUser user, String password) {
    String salt = user.getPasswordSalt();
    String hash = DigestUtils.sha256Hex(password + salt);
    return hash.equals(user.getPasswordHash());
  }

  public boolean verifyPassword(String email, String password) {
    AppUser appUser = getUserByEmail(email);
    return verifyPassword(appUser, password);

  }

  public boolean verifyPassword(Long userId, String password) {
    AppUser appUser = getUserById(userId);
    return verifyPassword(appUser, password);
  }

  public String createToken(String id, Long timeout) {
    String key = EnvUtils.getStr(AppConstant.ADMIN_SECRET_KEY);
    return JwtUtils.createTokenByUserId(key, id, timeout);
  }

  public String createRefreshToken(String id) {
    String key = EnvUtils.getStr(AppConstant.ADMIN_SECRET_KEY);
    return JwtUtils.createTokenByUserId(key, id, -1);
  }

  public boolean logout(String userId) {
    return true;
  }

  public boolean remove(String userId) {
    //String sql = "update app_users set deleted=1 WHERE id=?";
    //Db.updateBySql(sql, userId);
    String sql = "delete from app_users WHERE id=?";
    Db.delete(sql, userId);
    return true;
  }

  public RespBodyVo resetPassword(UserResetPasswordRequest req) {

    List<ValidateResult> validateResults = new ArrayList<>();
    boolean ok = true;
    String email = req.getEmail();
    String code = req.getCode();
    boolean validate = EmailValidator.validate(email);
    if (!validate) {
      ValidateResult validateResult = ValidateResult.by("eamil", "Failed to valiate email:" + email);
      validateResults.add(validateResult);
      ok = false;
    }

    String password = req.getPassword();
    validate = PasswordValidator.validate(password);
    if (!validate) {
      ValidateResult validateResult = ValidateResult.by("password", "Failed to valiate password:" + password);
      validateResults.add(validateResult);
      ok = false;
    }

    if (!ok) {
      return RespBodyVo.failData(validateResults);
    }

    boolean exists = Db.exists(TioBootAdminTableNames.app_users, "email", email);
    if (exists) {
      ValidateResult validateResult = ValidateResult.by("eamil", "Eamil already taken" + email);
      validateResults.add(validateResult);
    }

    if (!ok) {
      return RespBodyVo.failData(validateResults);
    }

    boolean verify = Aop.get(AppEmailService.class).verifyEmailCode(email, code);
    if (!verify) {
      return RespBodyVo.fail("Failed to verify code");
    }

    // 生成加盐字符串（示例中直接使用随机数，实际可使用更复杂逻辑）
    String salt = String.valueOf(System.currentTimeMillis());
    // 生成密码哈希（密码+盐）
    String passwordHash = DigestUtils.sha256Hex(password + salt);

    String updateSql = "update app_users set password_salt=?, password_hash=? where email=?";
    Db.updateBySql(updateSql, salt, passwordHash, email);
    return RespBodyVo.ok();
  }

  public RespBodyVo createAnonymousUser(String origin) {
    long longId = SnowflakeIdUtils.id();
    String userId = longId + "";
    String insertSql = "INSERT INTO app_users (id,of) VALUES (?,?)";

    Db.updateBySql(insertSql, userId, origin);

    // 生成 token，有效期 7 天（604800秒）
    Long timeout = EnvUtils.getLong("app.token.timeout", 604800L);
    Long tokenTimeout = System.currentTimeMillis() / 1000 + timeout;
    String token = createToken(userId, tokenTimeout);
    String refreshToken = createRefreshToken(userId);

    Kv kv = Kv.by("user_id", userId).set("token", token).set("expires_in", tokenTimeout.intValue())
        //
        .set("refresh_token", refreshToken);

    return RespBodyVo.ok(kv);
  }

  public boolean exists(String userId) {
    return Db.exists("app_users", "id", userId);
  }

  public boolean existsEmail(String email) {
    return Db.exists(TioBootAdminTableNames.app_users, "email", email);
  }
}
