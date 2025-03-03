package com.litongjava.tio.boot.admin.services;

import org.apache.commons.codec.digest.DigestUtils;

import com.litongjava.db.activerecord.Db;
import com.litongjava.tio.boot.admin.costants.AppConstant;
import com.litongjava.tio.boot.admin.vo.AppUser;
import com.litongjava.tio.utils.environment.EnvUtils;
import com.litongjava.tio.utils.jwt.JwtUtils;

public class AppUserService {

  // 注册用户：先检查邮箱是否已存在，然后插入用户记录
  public boolean registerUser(String email, String password, int userType, String orgin) {
    // 检查邮箱是否存在（简化处理）
    String checkSql = "SELECT COUNT(*) FROM app_users WHERE email=? AND deleted=0";
    long count = Db.queryLong(checkSql, email);
    if (count > 0) {
      return false;
    }

    // 生成加盐字符串（示例中直接使用随机数，实际可使用更复杂逻辑）
    String salt = String.valueOf(System.currentTimeMillis());
    // 生成密码哈希（密码+盐）
    String passwordHash = DigestUtils.sha256Hex(password + salt);

    // 插入用户记录（id 这里简单采用 email 作为唯一标识）
    String insertSql = "INSERT INTO app_users (id, email, password_salt, password_hash, user_type,of) VALUES (?,?,?,?,?,?)";
    int rows = Db.updateBySql(insertSql, email, email, salt, passwordHash, userType, orgin);
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
}
