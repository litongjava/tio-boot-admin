package com.litongjava.tio.boot.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppUserLoginVo {

  private String user_id;
  private String display_name;
  private String email;
  private String phone;
  private String photo_url;
  private String refresh_token;
  private String token;
  private Integer expires_in;

  public AppUserLoginVo(String userId, String displayName, String refreshToken, String token, int expires_in) {
    this.user_id = userId;
    this.display_name = displayName;
    this.refresh_token = refreshToken;
    this.token = token;
    this.expires_in = expires_in;
  }

  public AppUserLoginVo(String userId, String displayName, String email, String photo_url, String refreshToken, String token, int expires_in) {
    this.user_id = userId;
    this.display_name = displayName;
    this.email = email;
    this.photo_url = photo_url;
    this.refresh_token = refreshToken;
    this.token = token;
    this.expires_in = expires_in;
  }

  public AppUserLoginVo(String userId, String displayName, String email, String refreshToken, String token, int expires_in) {
    this.user_id = userId;
    this.display_name = displayName;
    this.email = email;
    this.refresh_token = refreshToken;
    this.token = token;
    this.expires_in = expires_in;
  }

}
