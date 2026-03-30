package com.litongjava.tio.boot.admin.services;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import com.litongjava.db.activerecord.Db;
import com.litongjava.tio.boot.admin.vo.EmailVerification;
import com.litongjava.tio.boot.email.EmailSender;
import com.litongjava.tio.boot.server.TioBootServer;

public class AppEmailService {

  // 发送验证邮件，并在数据库保存验证码记录
  public boolean sendVerificationEmail(String email, String origin) {

    // 生成验证码（例如 6 位数字或随机字符串）
    String code = String.valueOf((int) (Math.random() * 900000 + 100000));
    // 设置过期时间（例如 15 分钟后）
    LocalDateTime expireTime = LocalDateTime.now().plusMinutes(15);
    OffsetDateTime atOffset = expireTime.atOffset(ZoneOffset.UTC);
    // 保存验证码记录
    String insertSql = "INSERT INTO app_email_verification (email, verification_code, expire_time) VALUES (?,?,?)";
    int rows = Db.updateBySql(insertSql, email, code, atOffset);
    if (rows > 0) {
      EmailSender emailSender = TioBootServer.me().getEmailSender();
      if (emailSender != null) {
        //
        return emailSender.sendVerificationEmail(email, origin, code);
      }
    }
    return false;
  }
  
  public boolean sendVerificationCodeEmail(String email, String origin) {
    // 生成验证码（例如 6 位数字或随机字符串）
    String code = String.valueOf((int) (Math.random() * 900000 + 100000));
    // 设置过期时间（例如 15 分钟后）
    LocalDateTime expireTime = LocalDateTime.now().plusMinutes(15);
    OffsetDateTime atOffset = expireTime.atOffset(ZoneOffset.UTC);
    // 保存验证码记录
    String insertSql = "INSERT INTO app_email_verification (email, verification_code, expire_time) VALUES (?,?,?)";
    int rows = Db.updateBySql(insertSql, email, code, atOffset);
    if (rows > 0) {
      EmailSender emailSender = TioBootServer.me().getEmailSender();
      if (emailSender != null) {
        //
        return emailSender.sendVerificationCodeEmail(email, origin, code);
      }
    }
    return false;
  }

  //验证邮箱验证码是否正确且未过期
  public boolean verifyEmailCode(String email, String code) {
    String sql = "SELECT * FROM app_email_verification WHERE email=? AND verification_code=? AND verified=FALSE AND expire_time > CURRENT_TIMESTAMP";
    EmailVerification ev = Db.findFirst(EmailVerification.class, sql, email, code);
    if (ev != null) {
      // 更新记录状态为已验证
      String updateSql = "UPDATE app_email_verification SET verified=TRUE WHERE id=?";
      Db.updateBySql(updateSql, ev.getId());
      // 同时更新用户表中 email_verified 字段（假设用户 id 与 email 相同）
      String updateUser = "UPDATE app_users SET email_verified=TRUE WHERE email=?";
      Db.updateBySql(updateUser, email);
      return true;
    }
    return false;
  }

 
}
