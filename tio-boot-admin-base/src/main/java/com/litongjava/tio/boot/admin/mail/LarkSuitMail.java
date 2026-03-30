package com.litongjava.tio.boot.admin.mail;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.litongjava.tio.utils.environment.EnvUtils;

public class LarkSuitMail {

  private final Session session;
  private final Properties prop;

  private final String mailHost;
  private final Integer smtpPort;
  private final String user;
  private final String password;
  private final String from;
  private final String mailTransportProtocol;

  public LarkSuitMail() {
    mailHost = EnvUtils.get("lark.mail.host");
    mailTransportProtocol = EnvUtils.get("lark.mail.protocol");
    smtpPort = EnvUtils.getInt("lark.mail.smpt.port");
    user = EnvUtils.get("lark.mail.user");
    password = EnvUtils.get("lark.mail.password");
    from = EnvUtils.get("lark.mail.from");

    prop = new Properties();
    prop.setProperty("mail.host", mailHost);
    prop.setProperty("mail.transport.protocol", mailTransportProtocol);
    prop.setProperty("mail.smtp.auth", "true");
    prop.setProperty("mail.smtp.port", smtpPort.toString());
    prop.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
    prop.setProperty("mail.smtp.socketFactory.fallback", "false");
    prop.setProperty("mail.smtp.socketFactory.port", smtpPort.toString());

    prop.setProperty("mail.smtp.connectiontimeout", "3000");
    prop.setProperty("mail.smtp.timeout", "5000");
    prop.setProperty("mail.smtp.writetimeout", "5000");

    // 关键：加入 Authenticator，支持账号密码登录
    session = Session.getInstance(prop, new Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
      }
    });
  }

  public void send(String to, String subject, String content, boolean isDebug) {
    try {
      session.setDebug(isDebug);

      MimeMessage message = new MimeMessage(session);
      message.setFrom(new InternetAddress(from));
      message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
      message.setSubject(subject);
      message.setText(content);

      Transport.send(message); // 内部自动 connect + send + close
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void sendHtml(String to, String subject, String html, boolean isDebug) {
    try {
      session.setDebug(isDebug);

      MimeMessage message = new MimeMessage(session);
      message.setFrom(new InternetAddress(from));
      message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
      message.setSubject(subject, "UTF-8");
      message.setContent(html, "text/html; charset=UTF-8");

      Transport.send(message);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
