package com.litongjava.tio.boot.admin.services;

import com.jfinal.kit.Kv;
import com.litongjava.data.model.DbJsonBean;
import com.litongjava.data.model.DbPage;
import com.litongjava.data.services.DbJsonService;
import com.litongjava.data.utils.DbJsonBeanUtils;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.jfinal.plugin.activerecord.Page;
import com.litongjava.jfinal.plugin.activerecord.Record;
import com.litongjava.tio.boot.admin.config.TableToJsonConfig;
import com.litongjava.tio.boot.admin.costants.TableNames;
import com.litongjava.tio.boot.tesing.TioBootTest;
import com.litongjava.tio.utils.json.FastJson2Utils;
import com.litongjava.tio.utils.json.Json;
import com.litongjava.tio.utils.resp.RespVo;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
public class PostServiceTest {

  @BeforeClass
  public static void beforeClass() {
    TioBootTest.before(TableToJsonConfig.class);
  }

  @Test
  public void testList() {
    DbJsonService dbJsonService = Aop.get(DbJsonService.class);
    Kv kv = Kv.create();
    DbJsonBean<List<Record>> dbJsonBean = dbJsonService.list(TableNames.posts, kv);

    List<Record> list = dbJsonBean.getData();
    List<Map<String, Object>> listMap = list.stream().map(Record::toMap).collect(Collectors.toList());


    String s = Json.getJson().toJson(listMap);
//    String s = FastJson2Utils.toJson(listMap);
    System.out.println(s);

//    RespVo respVo = RespVo.ok(listMap).code(dbJsonBean.getCode()).msg(dbJsonBean.getMsg());
//    System.out.println(respVo);
  }

  public void testPage() {
    // 过滤已经删除的信息
    Kv kv = Kv.create();
    kv.set("deleted", 0);

    DbJsonService dbJsonService = Aop.get(DbJsonService.class);

    DbJsonBean<Page<Record>> page = dbJsonService.page(TableNames.posts, kv);
    DbJsonBean<DbPage<Kv>> dbJsonBean = DbJsonBeanUtils.pageToDbPage(page,false);
    System.out.println(dbJsonBean.getData().getList());
  }
}
