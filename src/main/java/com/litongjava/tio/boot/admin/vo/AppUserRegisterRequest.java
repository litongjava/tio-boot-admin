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
  private int userType; // 0：学生，1：教授
}