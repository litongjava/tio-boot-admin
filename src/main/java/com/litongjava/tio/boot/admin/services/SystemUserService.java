package com.litongjava.tio.boot.admin.services;

import java.util.Map;

import com.jfinal.kit.Kv;
import com.litongjava.db.activerecord.Db;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.tio.boot.admin.costants.TableNames;

import cn.hutool.crypto.digest.DigestUtil;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
public class SystemUserService {

  public RespBodyVo changePassword(Long userId, Map<String, String> requestMap) {
    Kv kv = Kv.create().set(requestMap);
    String oldPassword = kv.getStr("oldPassword");
    String newPassword = kv.getStr("newPassword");
    String confirmNewPassword = kv.getStr("confirmNewPassword");
    if (!newPassword.equals(confirmNewPassword)) {
      return RespBodyVo.fail("password does not match");
    }


    String hashedPassword = DigestUtil.sha256Hex(oldPassword);

    String sqlTemplate = "select count(1) from " + TableNames.tio_boot_admin_system_users + " where id=? and password=?";
    boolean exists = Db.existsBySql(sqlTemplate, userId, hashedPassword);
    if (!exists) {
      return RespBodyVo.fail("wrong password");
    }
    sqlTemplate = "update " + TableNames.tio_boot_admin_system_users + " set password=? where id=?";
    int update = Db.updateBySql(sqlTemplate, DigestUtil.sha256Hex(newPassword), userId);

    if (update == 1) {
      return RespBodyVo.ok();
    }

    return RespBodyVo.fail();
  }
}
