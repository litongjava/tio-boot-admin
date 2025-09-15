package com.litongjava.tio.boot.admin.mail;

public class LarkSuitEmailUtils {

  public static LarkSuitMail mail;

  public static void setMail(LarkSuitMail mail) {
    LarkSuitEmailUtils.mail = mail;
  }

  /**
   * send mail
   * @param to
   * @param subject
   * @param content
   * @param isDebug
   */
  public static void send(String to, String subject, String content, boolean isDebug) {
    mail.send(to, subject, content, isDebug);
  }

  /**
   * send mail
   * @param to
   * @param subject
   * @param content
   */
  public static void send(String to, String subject, String content) {
    mail.send(to, subject, content, false);
  }
  
  public static void sendHtml(String to, String subject, String html) {
    mail.sendHtml(to, subject, html, false);
  }
  
  
}
