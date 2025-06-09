package com.litongjava.tio.boot.admin.config;
import com.litongjava.annotation.AConfiguration;
import com.litongjava.annotation.Initialization;
import com.litongjava.ehcache.EhCachePlugin;
import com.litongjava.hook.HookCan;

@AConfiguration
public class TioAdminEhCacheConfig {

  @Initialization
  public void config() {
    EhCachePlugin ehCachePlugin = new EhCachePlugin();
    ehCachePlugin.start();
    HookCan.me().addDestroyMethod(ehCachePlugin::stop);
  }
}