package com.litongjava.tio.boot.admin.config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.litongjava.ehcache.EhCachePlugin;
import com.litongjava.hook.HookCan;

public class TioAdminEhCacheConfig {
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  public void config() {
    EhCachePlugin ehCachePlugin = new EhCachePlugin();
    log.info("ehCachePlugin:{}", ehCachePlugin);
    ehCachePlugin.start();
    HookCan.me().addDestroyMethod(ehCachePlugin::stop);
  }
}