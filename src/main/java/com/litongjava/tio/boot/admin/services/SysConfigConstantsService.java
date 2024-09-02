package com.litongjava.tio.boot.admin.services;

import com.litongjava.db.activerecord.Db;
import com.litongjava.tio.boot.admin.costants.TableNames;
import com.litongjava.tio.boot.admin.vo.SystemTxCosConfigVo;
import com.litongjava.tio.utils.json.Json;

public class SysConfigConstantsService {

  public SystemTxCosConfigVo getSystemTxCosConfig() {
    String sql = "select key_value from " + TableNames.tio_boot_admin_system_constants_config + " where key_name=?";
    String systemTxCosConfigString = Db.queryStr(sql, "systemTxCosConfig");
    return Json.getJson().parse(systemTxCosConfigString, SystemTxCosConfigVo.class);
  }
}