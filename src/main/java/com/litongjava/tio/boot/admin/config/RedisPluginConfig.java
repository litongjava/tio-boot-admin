// 导入必要的类和注解
package com.litongjava.tio.boot.admin.config;

import com.litongjava.jfinal.aop.annotation.AConfiguration;
import com.litongjava.jfinal.aop.annotation.AInitialization;
import com.litongjava.jfinal.plugin.redis.Cache;
import com.litongjava.jfinal.plugin.redis.Redis;
import com.litongjava.jfinal.plugin.redis.RedisPlugin;
import com.litongjava.tio.boot.server.TioBootServer;

@AConfiguration
public class RedisPluginConfig {

  @AInitialization
  public RedisPlugin redisPlugin() {
    // 创建并启动 Redis 插件
    RedisPlugin bbsRedis = new RedisPlugin("main", "localhost");
    bbsRedis.start();

    // 测试连接
    Cache cache = Redis.use("main");
    cache.getJedis().connect();

    TioBootServer.me().addDestroyMethod(bbsRedis::stop);
    return bbsRedis;
  }
}
