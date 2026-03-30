package com.litongjava.tio.boot.admin.services;

import com.litongjava.db.activerecord.Db;
import com.litongjava.tio.boot.admin.consts.TioBootAdminTableNames;
import com.litongjava.tio.boot.admin.vo.LoginAccountVo;
import com.litongjava.tio.utils.digest.Sha256Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoginService {
  public Long getUserIdByUsernameAndPassword(LoginAccountVo loginAccountVo) {
    // digest
    String password = loginAccountVo.getPassword();
    String hashPassword = Sha256Utils.digestToHex(password);
    //String hashPassword = DigestUtils.sha256Hex(password2);
    log.info("hashPassword:{}", hashPassword);
    String sql = "select id from %s where username=? and password=?";
    sql = String.format(sql, TioBootAdminTableNames.tio_boot_admin_system_users);
    return Db.queryLong(sql, loginAccountVo.getUsername(), hashPassword);
  }
}
