package nexus.io.tio.boot.admin.config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nexus.io.ehcache.EhCachePlugin;
import nexus.io.hook.HookCan;

public class TioAdminEhCacheConfig {
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  public void config() {
    EhCachePlugin ehCachePlugin = new EhCachePlugin();
    log.info("ehCachePlugin:{}", ehCachePlugin);
    ehCachePlugin.start();
    HookCan.me().addDestroyMethod(ehCachePlugin::stop);
  }
}