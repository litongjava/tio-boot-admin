package nexus.io.tio.boot.admin.services;

import nexus.io.db.SqlPara;
import nexus.io.db.activerecord.Db;
import nexus.io.db.activerecord.Row;
import nexus.io.model.body.RespBodyVo;
import nexus.io.tio.boot.admin.consts.TioBootAdminSql;

/**
 * @author Tong Li
 */
public class AdminUserService {
  public RespBodyVo currentUser(Object loginId) {
    String userById = TioBootAdminSql.getUserById();
    SqlPara sqlPara = SqlPara.by(userById);
    if (loginId instanceof String) {
      sqlPara.addPara(Long.parseLong((String) loginId));
    } else {
      sqlPara.addPara(loginId);
    }

    //执行查询
    Row first = Db.findFirst(sqlPara);
    //返回数据
    return RespBodyVo.ok(first.toKv());
  }
}
