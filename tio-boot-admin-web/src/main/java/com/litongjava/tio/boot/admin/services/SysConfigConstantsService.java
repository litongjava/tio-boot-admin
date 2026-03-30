package com.litongjava.tio.boot.admin.services;

import com.litongjava.db.activerecord.Db;
import com.litongjava.tio.boot.admin.costants.TioBootAdminTableNames;
import com.litongjava.tio.boot.admin.vo.SystemTxCosConfigVo;
import com.litongjava.tio.utils.json.Json;

public class SysConfigConstantsService {

  public SystemTxCosConfigVo getSystemTxCosConfig() {
    String sql = String.format("select key_value from %s where key_name=?", TioBootAdminTableNames.tio_boot_admin_system_constants_config);
    String systemTxCosConfigString = Db.queryStr(sql, "systemTxCosConfig");
    return Json.getJson().parse(systemTxCosConfigString, SystemTxCosConfigVo.class);
  }
}