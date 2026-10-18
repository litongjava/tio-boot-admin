package nexus.io.tio.boot.admin.services;

import nexus.io.db.activerecord.Db;
import nexus.io.tio.boot.admin.consts.TioBootAdminTableNames;
import nexus.io.tio.boot.admin.vo.LoginAccountVo;
import nexus.io.tio.utils.digest.Sha256Utils;

public class LoginService {
  public Long getUserIdByUsernameAndPassword(LoginAccountVo loginAccountVo) {
    if (loginAccountVo == null || loginAccountVo.getUsername() == null
        || loginAccountVo.getPassword() == null) {
      return null;
    }
    // Only active, non-deleted accounts may establish a new login.
    String password = loginAccountVo.getPassword();
    String hashPassword = Sha256Utils.digestToHex(password);
    String sql = """
        select id from %s
        where username=? and password=? and status=0 and deleted=0
        """;
    sql = String.format(sql, TioBootAdminTableNames.tio_boot_admin_system_users);
    return Db.queryLong(sql, loginAccountVo.getUsername(), hashPassword);
  }
}
