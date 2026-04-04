package com.litongjava.tio.boot.admin.services.system;

import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;

import com.jfinal.kit.Kv;
import com.litongjava.tio.boot.admin.consts.TioBootAdminTableNames;

import nexus.io.db.activerecord.Db;
import nexus.io.model.body.RespBodyVo;

/**
 * Created by Tong Li 
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


    String hashedPassword = DigestUtils.sha256Hex(oldPassword);

    String sqlTemplate = "select count(1) from " + TioBootAdminTableNames.tio_boot_admin_system_users + " where id=? and password=?";
    boolean exists = Db.existsBySql(sqlTemplate, userId, hashedPassword);
    if (!exists) {
      return RespBodyVo.fail("wrong password");
    }
    sqlTemplate = "update " + TioBootAdminTableNames.tio_boot_admin_system_users + " set password=? where id=?";
    int update = Db.updateBySql(sqlTemplate, DigestUtils.sha256Hex(newPassword), userId);

    if (update == 1) {
      return RespBodyVo.ok();
    }

    return RespBodyVo.fail();
  }
}
