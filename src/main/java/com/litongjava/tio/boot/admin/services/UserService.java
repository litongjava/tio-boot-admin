package com.litongjava.tio.boot.admin.services;

import com.litongjava.jfinal.plugin.activerecord.Db;
import com.litongjava.jfinal.plugin.activerecord.DbTemplate;
import com.litongjava.jfinal.plugin.activerecord.Record;
import com.litongjava.jfinal.plugin.activerecord.SqlPara;
import com.litongjava.tio.utils.resp.RespVo;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
public class UserService {
  public RespVo currentUser(Object loginId) {
    //template
    DbTemplate template = Db.template("user.getUserById");
    //sqlPara 是一个包含了sql和para的对象
    SqlPara sqlPara = template.getSqlPara();
    if (loginId instanceof String) {
      sqlPara.addPara(Long.parseLong((String) loginId));
    } else {
      sqlPara.addPara(loginId);
    }

    //执行查询
    Record first = Db.findFirst(sqlPara);
    //返回数据
    return RespVo.ok(first.toKv());
  }
}
