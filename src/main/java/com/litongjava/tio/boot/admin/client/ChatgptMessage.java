package com.litongjava.tio.boot.admin.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatgptMessage {
  //system,assistant,user
  private String role,content;
}
