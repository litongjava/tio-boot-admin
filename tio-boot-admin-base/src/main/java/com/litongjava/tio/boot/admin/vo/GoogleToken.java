package com.litongjava.tio.boot.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain=true)
public class GoogleToken {
  private String access_token;
  private Integer expires_in;
  private String scope;
  private String token_type;
  private String id_token;
}
