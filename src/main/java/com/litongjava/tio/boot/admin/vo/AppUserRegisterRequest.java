package com.litongjava.tio.boot.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppUserRegisterRequest {
  private String email;
  private String password;
  private int userType; // 0：匿名，1：普通, 2:高级
  private int verification_type; //0 不验证邮箱 1 验证码验证 2 链接验证
  private Long userId;
}