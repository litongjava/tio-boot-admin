package com.litongjava.tio.boot.admin.config;

import java.util.Arrays;

import javax.sql.DataSource;

import com.jfinal.template.Engine;
import com.jfinal.template.source.ClassPathSourceFactory;
import com.litongjava.annotation.AConfiguration;
import com.litongjava.annotation.Initialization;
import com.litongjava.db.activerecord.ActiveRecordPlugin;
import com.litongjava.db.activerecord.OrderedFieldContainerFactory;
import com.litongjava.db.activerecord.dialect.PostgreSqlDialect;
import com.litongjava.db.hikaricp.DsContainer;
import com.litongjava.openai.client.OpenAiClient;
import com.litongjava.table.services.ApiTable;
import com.litongjava.tio.boot.server.TioBootServer;
import com.litongjava.tio.utils.environment.EnvUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@AConfiguration
public class DbConfig {

  public DataSource dataSource() {
    String jdbcUrl = EnvUtils.get("jdbc.url");
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
    TioBootServer.me().addDestroyMethod(hikariDataSource::close);
    return hikariDataSource;

  }

  /*
   *
   * config ActiveRecordPlugin
   */
  @Initialization
  public void activeRecordPlugin(){
    // get dataSource
    DataSource dataSource = dataSource();
    // create arp
    ActiveRecordPlugin arp = new ActiveRecordPlugin(dataSource);
    arp.setContainerFactory(new OrderedFieldContainerFactory());
    if (EnvUtils.isDev()) {
      arp.setDevMode(true);
    }

    arp.setShowSql(true);
    String jdbcUrl = EnvUtils.get("jdbc.url");

    if (jdbcUrl.contains("postgresql")) {
      arp.setDialect(new PostgreSqlDialect());
    }

    // config engine
    Engine engine = arp.getEngine();
    // devMode下修改sql文件无需重启
    engine.setDevMode(EnvUtils.isDev());
    // 设置sql文件路径
    engine.setSourceFactory(new ClassPathSourceFactory());
    // 添加压缩
    engine.setCompressorOn(' ');
    engine.setCompressorOn('\n');
    // add sql file
    arp.addSqlTemplate("/sql/all_sql.sql");
    // start
    arp.start();

    ApiTable.setEmbeddingFun((string) -> {
      Float[] embeddingArray = OpenAiClient.embeddingArray(string);
      return Arrays.toString(embeddingArray);
    });

    // add stop
    TioBootServer.me().addDestroyMethod(arp::stop);
  }
}
