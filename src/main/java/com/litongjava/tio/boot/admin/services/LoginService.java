package com.litongjava.tio.boot.admin.services;

import cn.hutool.crypto.digest.DigestUtil;
import com.litongjava.jfinal.plugin.activerecord.Db;
import com.litongjava.tio.boot.admin.vo.LoginAccountVo;

/**
 * Created by litonglinux@qq.com on 3/25/2024_9:04 PM
 */
public class LoginService {
  public Long getUserIdByUsernameAndPassword(LoginAccountVo loginAccountVo) {
    // digest
    String password = DigestUtil.sha256Hex(loginAccountVo.getPassword());
    String sql = "select id from tio_boot_admin_system_users where username=? and password=?";
    return Db.queryLong(sql, loginAccountVo.getUsername(), password);
  }
}
