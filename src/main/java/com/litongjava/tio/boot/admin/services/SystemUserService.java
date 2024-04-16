package com.litongjava.tio.boot.admin.services;

import cn.hutool.crypto.digest.DigestUtil;
import com.jfinal.kit.Kv;
import com.litongjava.jfinal.plugin.activerecord.Db;
import com.litongjava.tio.boot.admin.costants.TableNames;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.utils.resp.RespVo;

import java.util.Map;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
public class SystemUserService {

  public RespVo changePassword(Long userId, Map<String, String> requestMap) {
    Kv kv = Kv.create().set(requestMap);
    String oldPassword = kv.getStr("oldPassword");
    String newPassword = kv.getStr("newPassword");
    String confirmNewPassword = kv.getStr("confirmNewPassword");
    if (!newPassword.equals(confirmNewPassword)) {
      return RespVo.fail("password does not match");
    }


    String hashedPassword = DigestUtil.sha256Hex(oldPassword);

    String sqlTemplate = "select count(1) from " + TableNames.tio_boot_admin_system_users + " where id=? and password=?";
    boolean exists = Db.exists(sqlTemplate, userId, hashedPassword);
    if (!exists) {
      return RespVo.fail("wrong password");
    }
    sqlTemplate = "update " + TableNames.tio_boot_admin_system_users + " set password=? where id=?";
    int update = Db.update(sqlTemplate, DigestUtil.sha256Hex(newPassword), userId);

    if (update == 1) {
      return RespVo.ok();
    }

    return RespVo.fail();
  }
}
