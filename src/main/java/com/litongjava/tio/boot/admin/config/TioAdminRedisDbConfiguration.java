package com.litongjava.tio.boot.admin.config;

import com.litongjava.hook.HookCan;
import com.litongjava.redis.Redis;
import com.litongjava.redis.RedisDb;
import com.litongjava.redis.RedisPlugin;
import com.litongjava.tio.utils.environment.EnvUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TioAdminRedisDbConfiguration {

  public void config() {
    String host = EnvUtils.getStr("redis.host");
    if (host == null) {
      return;
    }
    log.info("host:{}", host);
    Integer port = EnvUtils.getInt("redis.port");
    String password = EnvUtils.getStr("redis.password");
    int redisTimeout = EnvUtils.getInt("redis.timeout", 60);
    int redisDatabase = EnvUtils.getInt("redis.database", 0);
    String cacheName = EnvUtils.get("redis.cacheName", "main");

    // 创建并启动 Redis 插件
    RedisPlugin mainRedis = new RedisPlugin(cacheName, host, port, redisTimeout, password, redisDatabase);
    mainRedis.start();
    // 测试连接
    RedisDb cache = Redis.use(cacheName);
    try {
      cache.getJedis().connect();
    } catch (Exception e) {
      log.error("failed to connected to {},{},{}", host, port, password);
      e.printStackTrace();
    }

    HookCan.me().addDestroyMethod(mainRedis::stop);
  }
}
