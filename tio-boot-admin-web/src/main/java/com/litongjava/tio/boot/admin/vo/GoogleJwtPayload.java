package com.litongjava.tio.boot.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class GoogleJwtPayload {
  // 签发者
  private String iss;
  // 授权方
  private String azp;
  // 接收者
  private String aud;
  // 用户唯一标识
  private String sub;
  // 用户邮箱
  private String email;
  // 邮箱是否验证
  private boolean emailVerified;
  // 访问令牌哈希
  private String atHash;
  // 用户全名
  private String name;
  // 用户头像地址
  private String picture;
  // 名字
  private String givenName;
  // 姓氏
  private String familyName;
  // 签发时间（Unix 时间戳）
  private long iat;
  // 过期时间（Unix 时间戳）
  private long exp;
}
