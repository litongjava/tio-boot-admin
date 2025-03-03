package com.litongjava.tio.boot.admin.services;

import org.apache.commons.codec.digest.DigestUtils;

import com.litongjava.db.activerecord.Db;
import com.litongjava.tio.boot.admin.costants.TioBootAdminTableNames;
import com.litongjava.tio.boot.admin.vo.LoginAccountVo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoginService {
  public Long getUserIdByUsernameAndPassword(LoginAccountVo loginAccountVo) {
    // digest
    String password = DigestUtils.sha256Hex(loginAccountVo.getPassword());
    log.info("password:{}", password);
    String sql = "select id from %s where username=? and password=?";
    sql = String.format(sql, TioBootAdminTableNames.tio_boot_admin_system_users);
    return Db.queryLong(sql, loginAccountVo.getUsername(), password);
  }
}
