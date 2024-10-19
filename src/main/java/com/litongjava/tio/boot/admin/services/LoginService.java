package com.litongjava.tio.boot.admin.services;

import com.litongjava.db.activerecord.Db;
import com.litongjava.tio.boot.admin.vo.LoginAccountVo;

import cn.hutool.crypto.digest.DigestUtil;

/**
 * @author Tong Li
 */
public class LoginService {
  public Long getUserIdByUsernameAndPassword(LoginAccountVo loginAccountVo) {
    // digest
    String password = DigestUtil.sha256Hex(loginAccountVo.getPassword());
    String sql = "select id from tio_boot_admin_system_users where username=? and password=?";
    return Db.queryLong(sql, loginAccountVo.getUsername(), password);
  }
}
