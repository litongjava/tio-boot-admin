package com.litongjava.tio.boot.admin.services;

import org.junit.BeforeClass;
import org.junit.Test;

import com.litongjava.db.activerecord.Db;
import com.litongjava.db.activerecord.DbTemplate;
import com.litongjava.db.activerecord.Record;
import com.litongjava.db.activerecord.SqlPara;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.tio.boot.admin.config.DbConfig;
import com.litongjava.tio.boot.tesing.TioBootTest;
import com.litongjava.tio.utils.resp.RespVo;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
public class UserServiceTest {

  @BeforeClass
  public static void beforeClass() {
    TioBootTest.before(DbConfig.class);
  }

  @Test
  public void adminUser() {
    DbTemplate template = Db.template("user.adminUser");
    Record first = template.findFirst();
    System.out.println(first);
  }

  @Test
  public void getUserById() {
    //template
    DbTemplate template = Db.template("user.getUserById");
    //sqlPara 是一个包含了sql和para的对象
    SqlPara sqlPara = template.getSqlPara();
    sqlPara.addPara(1);
    //执行查询
    Record first = Db.findFirst(sqlPara);
    System.out.println(first);
  }

  @Test
  public void currentUser() {
    RespVo respVo = Aop.get(UserService.class).currentUser(1L);
    System.out.println(respVo.getData());
  }
}