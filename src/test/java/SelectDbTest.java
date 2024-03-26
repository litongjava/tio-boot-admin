import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.litongjava.data.model.DbJsonBean;
import com.litongjava.data.services.DbJsonService;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.jfinal.plugin.activerecord.Record;
import com.litongjava.tio.boot.admin.config.TableToJsonConfig;
import com.litongjava.tio.boot.tesing.TioBootTest;

public class SelectDbTest {

  DbJsonService dbJsonService = Aop.get(DbJsonService.class);

  @Before
  public void before() throws Exception {
    TioBootTest.before(TableToJsonConfig.class);
  }

  @Test
  public void test() {
    String tableName = "tio_boot_admin_system_user_event";
    DbJsonBean<List<Record>> listAll = dbJsonService.listAll(tableName);
    System.out.println(listAll.getData().size());
  }
}
