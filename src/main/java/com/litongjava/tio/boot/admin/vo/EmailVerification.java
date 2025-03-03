package com.litongjava.tio.boot.admin.vo;

import java.time.Instant;

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
  private Instant createTime;
  private Instant expireTime;
  private boolean verified;
}