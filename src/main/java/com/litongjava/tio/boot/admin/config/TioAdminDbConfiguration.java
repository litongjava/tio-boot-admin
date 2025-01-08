package com.litongjava.tio.boot.admin.config;

import java.util.Arrays;

import com.litongjava.db.activerecord.ActiveRecordPlugin;
import com.litongjava.db.activerecord.OrderedFieldContainerFactory;
import com.litongjava.db.activerecord.dialect.PostgreSqlDialect;
import com.litongjava.db.hikaricp.DsContainer;
import com.litongjava.hook.HookCan;
import com.litongjava.openai.client.OpenAiClient;
import com.litongjava.table.services.ApiTable;
import com.litongjava.tio.boot.server.TioBootServer;
import com.litongjava.tio.utils.environment.EnvUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TioAdminDbConfiguration {
  public void config() {
    // get dataSource
    String jdbcUrl = EnvUtils.get("jdbc.url");
    if (jdbcUrl == null) {
      return;
    }
    log.info("jdbcUrl:{}", jdbcUrl);
    String jdbcUser = EnvUtils.get("jdbc.user");

    String jdbcPswd = EnvUtils.get("jdbc.pswd");
    int maximumPoolSize = EnvUtils.getInt("jdbc.MaximumPoolSize", 2);

    HikariConfig config = new HikariConfig();
    // config
    config.setJdbcUrl(jdbcUrl);
    config.setUsername(jdbcUser);
    config.setPassword(jdbcPswd);
    config.setMaximumPoolSize(maximumPoolSize);

    HikariDataSource hikariDataSource = new HikariDataSource(config);

    // set datasource
    DsContainer.setDataSource(hikariDataSource);
    // add destroy
    HookCan.me().addDestroyMethod(hikariDataSource::close);

    // create arp
    ActiveRecordPlugin arp = new ActiveRecordPlugin(hikariDataSource);
    arp.setContainerFactory(new OrderedFieldContainerFactory());
    boolean showSql = EnvUtils.getBoolean("jdbc.showSql", false);
    arp.setShowSql(showSql);
    if (EnvUtils.isDev()) {
      arp.setDevMode(true);
    }
    if (jdbcUrl.contains("postgresql")) {
      arp.setDialect(new PostgreSqlDialect());
    }
    // add sql file
    // start
    arp.start();

    ApiTable.setEmbeddingFun((string) -> {
      float[] embeddingArray = OpenAiClient.embeddingArray(string);
      return Arrays.toString(embeddingArray);
    });

    // add stop
    HookCan.me().addDestroyMethod(arp::stop);
  }
}
