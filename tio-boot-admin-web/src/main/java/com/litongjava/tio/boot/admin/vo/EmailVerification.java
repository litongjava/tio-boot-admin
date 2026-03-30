package com.litongjava.tio.boot.admin.vo;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerification {
  private int id;
  private String email;
  private String verificationCode;
  private Timestamp createTime;
  private Timestamp expireTime;
  private boolean verified;
}