package com.litongjava.tio.boot.admin.config;
import com.litongjava.ehcache.EhCachePlugin;
import com.litongjava.hook.HookCan;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TioAdminEhCacheConfig {

  public void config() {
    EhCachePlugin ehCachePlugin = new EhCachePlugin();
    log.info("ehCachePlugin:{}", ehCachePlugin);
    ehCachePlugin.start();
    HookCan.me().addDestroyMethod(ehCachePlugin::stop);
  }
}