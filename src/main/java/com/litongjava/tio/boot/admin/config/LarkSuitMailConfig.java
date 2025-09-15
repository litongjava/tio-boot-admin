package com.litongjava.tio.boot.admin.config;

import com.litongjava.hook.HookCan;
import com.litongjava.tio.boot.admin.mail.LarkSuitEmailUtils;
import com.litongjava.tio.boot.admin.mail.LarkSuitMail;

public class LarkSuitMailConfig {

  public void config() {
    LarkSuitMail larkSuitMail = new LarkSuitMail();
    LarkSuitEmailUtils.setMail(larkSuitMail);
    HookCan.me().addDestroyMethod(larkSuitMail::close);
  }
}
