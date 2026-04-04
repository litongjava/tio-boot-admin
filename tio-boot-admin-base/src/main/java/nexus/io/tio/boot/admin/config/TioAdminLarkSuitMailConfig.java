package nexus.io.tio.boot.admin.config;

import nexus.io.tio.boot.admin.mail.LarkSuitEmailUtils;
import nexus.io.tio.boot.admin.mail.LarkSuitMail;

public class TioAdminLarkSuitMailConfig {

  public void config() {
    LarkSuitMail larkSuitMail = new LarkSuitMail();
    LarkSuitEmailUtils.setMail(larkSuitMail);
  }
}
