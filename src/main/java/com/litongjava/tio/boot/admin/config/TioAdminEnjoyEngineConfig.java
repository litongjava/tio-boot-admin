package com.litongjava.tio.boot.admin.config;

import com.jfinal.template.Engine;
import com.litongjava.hook.HookCan;
import com.litongjava.template.RowFieldGetter;
import com.litongjava.tio.boot.server.TioBootServer;

public class TioAdminEnjoyEngineConfig {

  private final String RESOURCE_BASE_PATH = "/enjoy-templates/";

  public void config() {
    Engine engine = Engine.use();
    engine.setBaseTemplatePath(RESOURCE_BASE_PATH);
    engine.setToClassPathSourceFactory();
    // 开启模板热加载，大部分生产环境建议开启，除非追求极致性能
    engine.setDevMode(true);
    // 配置极速模式，性能提升 13%
    Engine.setFastMode(true);
    // 支持中文表达式、中文变量名、中文方法名及中文模板函数名
    Engine.setChineseExpression(true);
  }
}
