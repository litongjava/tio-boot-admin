package com.litongjava.tio.boot.admin.t2j;

import com.litongjava.data.model.DbTableStruct;
import com.litongjava.data.services.PrimaryKeyService;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.tio.boot.admin.config.TableToJsonConfig;
import com.litongjava.tio.boot.tesing.TioBootTest;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
public class DateParseTest {

  @BeforeClass
  public static void beforeClass() {
    TioBootTest.before(TableToJsonConfig.class);
  }

  @Test
  public void getPrimaryKeyTest() {
    PrimaryKeyService primaryKeyService = Aop.get(PrimaryKeyService.class);
    String tableName = "users";
    DbTableStruct primaryKey = primaryKeyService.getPrimaryKey(tableName);
    System.out.println(primaryKey);

  }

  @Test
  public void getPrimaryKeyColumnType() {
    PrimaryKeyService primaryKeyService = Aop.get(PrimaryKeyService.class);
    String tableName = "users";
    String primaryKeyColumnType = primaryKeyService.getPrimaryKeyColumnType(tableName);
    System.out.println(primaryKeyColumnType);
  }

}
