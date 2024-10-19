package com.litongjava.tio.boot.admin.services;

import com.litongjava.db.SqlPara;
import com.litongjava.db.activerecord.Db;
import com.litongjava.db.activerecord.Record;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.tio.boot.admin.costants.TioBootAdminSql;

/**
 * @author Tong Li
 */
public class UserService {
  public RespBodyVo currentUser(Object loginId) {
    String userById = TioBootAdminSql.getUserById();
    SqlPara sqlPara = SqlPara.by(userById);
    if (loginId instanceof String) {
      sqlPara.addPara(Long.parseLong((String) loginId));
    } else {
      sqlPara.addPara(loginId);
    }

    //执行查询
    Record first = Db.findFirst(sqlPara);
    //返回数据
    return RespBodyVo.ok(first.toKv());
  }
}
