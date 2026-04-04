package nexus.io.tio.boot.admin.services;

import nexus.io.db.activerecord.Db;
import nexus.io.tio.boot.admin.consts.TioBootAdminTableNames;
import nexus.io.tio.boot.admin.vo.SystemTxCosConfigVo;
import nexus.io.tio.utils.json.Json;

public class SysConfigConstantsService {

  public SystemTxCosConfigVo getSystemTxCosConfig() {
    String sql = String.format("select key_value from %s where key_name=?", TioBootAdminTableNames.tio_boot_admin_system_constants_config);
    String systemTxCosConfigString = Db.queryStr(sql, "systemTxCosConfig");
    return Json.getJson().parse(systemTxCosConfigString, SystemTxCosConfigVo.class);
  }
}