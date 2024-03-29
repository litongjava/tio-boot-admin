package com.litongjava.tio.boot.admin.config;

import com.litongjava.jfinal.aop.annotation.AConfiguration;
import com.litongjava.jfinal.aop.annotation.AInitialization;
import com.litongjava.tio.boot.satoken.SaTokenContextForTio;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.config.SaCookieConfig;
import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.context.SaTokenContext;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaTokenConsts;

@AConfiguration
public class SaTokenConfiguration {

  @AInitialization
  public void config() {
    // 初始化 Sa-Token 上下文
    SaTokenContext saTokenContext = new SaTokenContextForTio();

    // 设置 Cookie 配置，例如启用 HttpOnly 属性
    SaCookieConfig saCookieConfig = new SaCookieConfig();
    saCookieConfig.setHttpOnly(true);

    // 初始化和配置 Sa-Token 主配置
    SaTokenConfig saTokenConfig = new SaTokenConfig();
    saTokenConfig.setTokenStyle(SaTokenConsts.TOKEN_STYLE_SIMPLE_UUID);
    saTokenConfig.setActiveTimeout(50 * 60); // 设置活动超时时间为 50 分钟

    saTokenConfig.setIsShare(false);
    saTokenConfig.setTokenName("token"); // 设置 token 的名称
    saTokenConfig.setIsWriteHeader(true); // 将 token 写入响应头
    saTokenConfig.setIsReadHeader(true); // 从请求头中读取 token

    saTokenConfig.setCookie(saCookieConfig);

    // 应用配置到 Sa-Token 管理器
    SaManager.setConfig(saTokenConfig);
    SaManager.setSaTokenContext(saTokenContext);

    SaManager.setSaTokenDao(null);

    //生成jwt token
    saTokenConfig.setJwtSecretKey("asdasdasifhueuiwyurfewbfjsdafjk");
    saTokenConfig.setTokenPrefix("Bearer");
    StpLogicJwtForSimple stpLogicJwtForSimple = new StpLogicJwtForSimple();
    StpUtil.setStpLogic(stpLogicJwtForSimple);
  }
}
