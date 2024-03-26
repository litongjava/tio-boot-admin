package com.litongjava.tio.boot.admin.config;
import javax.sql.DataSource;

import com.jfinal.template.Engine;
import com.jfinal.template.source.ClassPathSourceFactory;
import com.litongjava.jfinal.aop.annotation.AConfiguration;
import com.litongjava.jfinal.aop.annotation.AInitialization;
import com.litongjava.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.litongjava.jfinal.plugin.activerecord.OrderedFieldContainerFactory;
import com.litongjava.jfinal.plugin.hikaricp.DsContainer;
import com.litongjava.tio.boot.constatns.TioBootConfigKeys;
import com.litongjava.tio.boot.server.TioBootServer;
import com.litongjava.tio.utils.environment.EnvironmentUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@AConfiguration
public class TableToJsonConfig {

  public DataSource dataSource() {
    String jdbcUrl = EnvironmentUtils.get("jdbc.url");
    String jdbcUser = EnvironmentUtils.get("jdbc.user");

    String jdbcPswd = EnvironmentUtils.get("jdbc.pswd");
    int maximumPoolSize = EnvironmentUtils.getInt("jdbc.MaximumPoolSize", 2);

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

  @AInitialization
  public void activeRecordPlugin() throws Exception {
    // get dataSource
    DataSource dataSource = dataSource();
    // get env key
    String property = EnvironmentUtils.get(TioBootConfigKeys.APP_ENV);

    // create arp
    ActiveRecordPlugin arp = new ActiveRecordPlugin(dataSource);
    arp.setContainerFactory(new OrderedFieldContainerFactory());
    if ("dev".equals(property)) {
      arp.setDevMode(true);
    }

    // config engine
    Engine engine = arp.getEngine();
    engine.setSourceFactory(new ClassPathSourceFactory());
    engine.setCompressorOn(' ');
    engine.setCompressorOn('\n');
    // add sql file
    arp.addSqlTemplate("/sql/all_sqls.sql");
    // start
    arp.start();
    // add stop
    TioBootServer.me().addDestroyMethod(arp::stop);
  }
}
