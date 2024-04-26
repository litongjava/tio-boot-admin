package com.litongjava.tio.boot.admin.config;

import com.litongjava.jfinal.aop.annotation.AConfiguration;
import com.litongjava.jfinal.aop.annotation.AInitialization;
import com.litongjava.jfinal.plugin.redis.Cache;
import com.litongjava.jfinal.plugin.redis.Redis;
import com.litongjava.jfinal.plugin.redis.RedisPlugin;
import com.litongjava.tio.boot.server.TioBootServer;
import com.litongjava.tio.utils.environment.EnvironmentUtils;

@AConfiguration
public class RedisPluginConfig {

  @AInitialization
  public RedisPlugin redisPlugin() {
    String host = EnvironmentUtils.getStr("redis.host");
    Integer port = EnvironmentUtils.getInt("redis.port");
    String password = EnvironmentUtils.getStr("redis.password");
    int redistimeout = EnvironmentUtils.getInt("redis.timeout", 60);
    int redisDatabase = EnvironmentUtils.getInt("redis.database", 0);
    String cacheName = EnvironmentUtils.get("redis.cacheName");

    // 创建并启动 Redis 插件
    RedisPlugin mainRedis = new RedisPlugin(cacheName, host, port, redistimeout, password, redisDatabase);
    mainRedis.start();

    // 测试连接
    Cache cache = Redis.use(cacheName);
    cache.getJedis().connect();

    TioBootServer.me().addDestroyMethod(mainRedis::stop);
    return mainRedis;
  }
}
