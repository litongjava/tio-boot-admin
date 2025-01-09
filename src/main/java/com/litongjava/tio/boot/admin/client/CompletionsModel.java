package com.litongjava.tio.boot.admin.client;

import java.util.List;

import com.litongjava.openai.chat.ChatMessage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CompletionsModel {
  private String model;
  private List<ChatMessage> messages;
}
