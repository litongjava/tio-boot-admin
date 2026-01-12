package com.litongjava.tio.boot.admin.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

import com.litongjava.db.activerecord.Db;
import com.litongjava.db.activerecord.Row;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.model.validate.ValidateResult;
import com.litongjava.tio.boot.admin.costants.AppConstant;
import com.litongjava.tio.boot.admin.costants.TioBootAdminTableNames;
import com.litongjava.tio.boot.admin.sql.AppUsersSql;
import com.litongjava.tio.boot.admin.vo.AppUser;
import com.litongjava.tio.boot.admin.vo.AppUserRegisterRequest;
import com.litongjava.tio.boot.admin.vo.UserResetPasswordRequest;
import com.litongjava.tio.boot.admin.vo.UserToken;
import com.litongjava.tio.boot.admin.vo.UserUpdatePasswordRequest;
import com.litongjava.tio.utils.environment.EnvUtils;
import com.litongjava.tio.utils.jwt.JwtUtils;
import com.litongjava.tio.utils.snowflake.SnowflakeIdUtils;
import com.litongjava.tio.utils.validator.EmailValidator;
import com.litongjava.tio.utils.validator.PasswordValidator;

public class AppUserService {

  // 查询用户基本信息（不含密码相关字段），自动映射下划线到驼峰
  private static final String SQL_SELECT_USER = AppUsersSql.findById;

  // 仅查询密码盐与哈希
  private static final String SQL_SELECT_PASSWORD = "SELECT id, password_salt, password_hash FROM app_users WHERE id = ? AND deleted = 0";

  public boolean registerUserByUserId(AppUserRegisterRequest req, String origin) {
    String password = req.getPassword();
    String email = req.getEmail();
    String displayName = null;
    String username = req.getUsername();
    if (email != null) {
      int lastIndex = email.lastIndexOf("@");
      if (lastIndex > 0) {
        displayName = email.substring(0, lastIndex);
      } else {
        displayName = email;
      }
    } else {
      displayName = username;
    }

    int userType = req.getUserType();
    boolean exists = Db.exists(TioBootAdminTableNames.app_users, "username", username);
    if (exists) {
      return false;
    }
    exists = Db.exists(TioBootAdminTableNames.app_users, "email", email);
    if (exists) {
      return false;
    }
    // 生成加盐字符串（示例中直接使用随机数，实际可使用更复杂逻辑）
    String salt = String.valueOf(System.currentTimeMillis());
    // 生成密码哈希（密码+盐）
    String passwordHash = DigestUtils.sha256Hex(password + salt);

    // 插入用户记录（id 这里简单采用 email 作为唯一标识）
    long id = SnowflakeIdUtils.id();
    String insertSql = "update app_users set display_name=?, email=?, username=?,password_salt=?, password_hash=?, user_type=?,of=? where id=?";
    int rows = Db.updateBySql(insertSql, displayName, email, username, salt, passwordHash, userType, origin, id + "");
    return rows > 0;
  }

  // 注册用户：先检查邮箱是否已存在，然后插入用户记录
  public boolean registerUser(String email, String username, String password, int userType, String orgin) {
    boolean exists = Db.exists(TioBootAdminTableNames.app_users, "email", email);
    if (exists) {
      return true;
    }
    String displayName = null;
    if (email != null) {
      int lastIndex = email.lastIndexOf("@");
      if (lastIndex > 0) {
        displayName = email.substring(0, lastIndex);
      } else {
        displayName = email;
      }
    } else {
      if (username != null) {
        displayName = username;
      }
    }

    // 生成加盐字符串（示例中直接使用随机数，实际可使用更复杂逻辑）
    String salt = String.valueOf(System.currentTimeMillis());
    // 生成密码哈希（密码+盐）
    String passwordHash = DigestUtils.sha256Hex(password + salt);

    // 插入用户记录（id 这里简单采用 email 作为唯一标识）
    long id = SnowflakeIdUtils.id();
    int rows;
    if (username != null) {
      String insertSql = "INSERT INTO app_users (id, display_name,email,username, password_salt, password_hash, user_type,of) VALUES (?,?,?,?,?,?,?,?)";
      rows = Db.updateBySql(insertSql, id + "", displayName, email, username, salt, passwordHash, userType, orgin);
    } else {
      String insertSql = "INSERT INTO app_users (id, display_name,email, password_salt, password_hash, user_type,of) VALUES (?,?,?,?,?,?,?)";
      rows = Db.updateBySql(insertSql, id + "", displayName, email, salt, passwordHash, userType, orgin);
    }
    return rows > 0;
  }

  // 根据邮箱获取用户信息
  public AppUser getUserByEmail(String email) {
    String sql = "SELECT * FROM app_users WHERE email=? AND deleted=0";
    return Db.findFirst(AppUser.class, sql, email);
  }

  public AppUser getUserByUsername(String username) {
    String sql = "SELECT * FROM app_users WHERE username=? AND deleted=0";
    return Db.findFirst(AppUser.class, sql, username);
  }

  public AppUser getUserPasswordById(Long userId) {
    return Db.findFirst(AppUser.class, SQL_SELECT_PASSWORD, userId);
  }

  public AppUser getUserPasswordById(String userId) {
    return Db.findFirst(AppUser.class, SQL_SELECT_PASSWORD, userId);
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
    AppUser appUser = getUserPasswordById(userId);
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
    // String sql = "update app_users set deleted=1 WHERE id=?";
    // Db.updateBySql(sql, userId);
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
      ValidateResult validateResult = ValidateResult.by("email", "Failed to valiate email:" + email);
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
      ValidateResult validateResult = ValidateResult.by("email", "email already taken " + email);
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
    String insertSql = "INSERT INTO app_users (id,of,user_type) VALUES (?,?,0)";

    Db.updateBySql(insertSql, userId, origin);

    // 生成 token，有效期 7 天（604800秒）
    Long timeout = EnvUtils.getLong("app.token.timeout", 604800L);
    Long tokenTimeout = System.currentTimeMillis() / 1000 + timeout;
    String token = createToken(userId, tokenTimeout);
    String refreshToken = createRefreshToken(userId);

    UserToken userToken = new UserToken(userId, token, tokenTimeout.intValue(), refreshToken, 0);
    return RespBodyVo.ok(userToken);
  }

  public boolean exists(String userId) {
    return Db.exists("app_users", "id", userId);
  }

  public String getUsernameById(String userId) {
    return Db.queryStr("select username from app_users where id=?", userId);
  }

  public boolean existsEmail(String email) {
    return Db.exists(TioBootAdminTableNames.app_users, "email", email);
  }

  public boolean existsUsername(String email) {
    return Db.exists(TioBootAdminTableNames.app_users, "username", email);
  }

  public AppUser getUserById(Long userId) {
    return Db.findFirst(AppUser.class, SQL_SELECT_USER, userId);
  }

  public AppUser getUserById(String userIdString) {
    return Db.findFirst(AppUser.class, SQL_SELECT_USER, userIdString);
  }

  public boolean updateById(String userIdString, Row row) {
    row.set("id", userIdString);
    return Db.update(TioBootAdminTableNames.app_users, row);
  }

  public RespBodyVo updatePassword(String userIdString, UserUpdatePasswordRequest updatePasswordRequest) {
    String oldPassword = updatePasswordRequest.getOldPassword();
    String newPassword = updatePasswordRequest.getNewPassword();
    String sql = "select password_salt from app_users where id=?";
    String salt = Db.queryStr(sql, userIdString);
    // 生成密码哈希（密码+盐）
    String passwordHash = DigestUtils.sha256Hex(oldPassword + salt);
    sql = "select count(1) from app_users where id=? and password_hash=?";
    boolean exists = Db.existsBySql(sql, userIdString, passwordHash);

    if (exists) {
      passwordHash = DigestUtils.sha256Hex(newPassword + newPassword);
      sql = "update app_users set password_hash=? where id=?";
      int effectd = Db.updateBySql(sql, passwordHash, userIdString);
      return RespBodyVo.ok(effectd);

    } else {
      return RespBodyVo.fail();
    }
  }
}
