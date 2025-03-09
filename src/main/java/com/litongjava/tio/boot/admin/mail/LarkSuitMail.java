package com.litongjava.tio.boot.admin.mail;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.litongjava.tio.utils.environment.EnvUtils;

public class LarkSuitMail {

  private Session session;
  private Transport transport;
  private Properties prop;
  // 连接参数，从环境变量中读取
  private String mailHost;
  private Integer smtpPort;
  private String user;
  private String password;
  private String from;
  private String mailTransportProtocol;

  public LarkSuitMail() {
    // 初始化连接参数
    mailHost = EnvUtils.get("lark.mail.host");
    mailTransportProtocol = EnvUtils.get("lark.mail.protocol");
    smtpPort = EnvUtils.getInt("lark.mail.smpt.port");
    user = EnvUtils.get("lark.mail.user");
    password = EnvUtils.get("lark.mail.password");
    from = EnvUtils.get("lark.mail.from");

    // 设置邮件属性
    prop = new Properties();
    prop.setProperty("mail.host", mailHost);
    prop.setProperty("mail.transport.protocol", mailTransportProtocol);
    prop.setProperty("mail.smtp.auth", "true");
    prop.setProperty("mail.smtp.port", smtpPort.toString());
    prop.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
    prop.setProperty("mail.smtp.socketFactory.fallback", "false");
    prop.setProperty("mail.smtp.socketFactory.port", smtpPort.toString());

    // 创建Session
    session = Session.getInstance(prop);

    // 初次建立连接
    try {
      transport = session.getTransport();
      transport.connect(mailHost, smtpPort, user, password);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * 发送邮件，复用已建立的连接
   * @param to 收件人
   * @param subject 邮件主题
   * @param content 邮件内容
   * @param isDebug 是否开启调试模式
   */
  public synchronized void send(String to, String subject, String content, boolean isDebug) {
    // 设置调试模式
    session.setDebug(isDebug);
    MimeMessage message = new MimeMessage(session);
    try {
      // 如果连接断开，重新连接
      if (transport == null || !transport.isConnected()) {
        transport = session.getTransport();
        transport.connect(mailHost, smtpPort, user, password);
      }
      // 设置邮件信息
      message.setFrom(new InternetAddress(from));
      message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
      message.setSubject(subject);
      message.setText(content);

      // 复用 transport 发送邮件
      transport.sendMessage(message, message.getAllRecipients());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 关闭 SMTP 连接，可在应用退出时调用
   */
  public synchronized void close() {
    if (transport != null && transport.isConnected()) {
      try {
        transport.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}