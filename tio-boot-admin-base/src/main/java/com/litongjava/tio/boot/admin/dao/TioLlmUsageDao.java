package com.litongjava.tio.boot.admin.dao;

import java.util.Iterator;
import java.util.List;

import com.litongjava.tio.boot.admin.dto.TioLlmUsage;
import com.litongjava.tio.utils.json.JsonUtils;
import com.litongjava.tio.utils.snowflake.SnowflakeIdUtils;

import lombok.extern.slf4j.Slf4j;
import nexus.io.chat.UniChatMessage;
import nexus.io.chat.UniChatRequest;
import nexus.io.chat.UniChatResponse;
import nexus.io.db.activerecord.Row;
import nexus.io.db.base.DbBase;
import nexus.io.openai.chat.ChatResponseMessage;

@Slf4j
public class TioLlmUsageDao extends DbBase {

  @Override
  public String getTableName() {
    return TioLlmUsage.tableName;
  }

  public void saveUsage(UniChatRequest uniChatRequest, UniChatResponse uniChatResponse, long elapsed) {
    if (uniChatResponse == null) {
      log.warn("Skipping saveUsage for task {} as generated response is null", uniChatRequest.getTaskId());
      return;
    }

    String systemPrompt = uniChatRequest.getSystemPrompt();
    List<UniChatMessage> messages = uniChatRequest.getMessages();
    Iterator<UniChatMessage> iterator = messages.iterator();
    while(iterator.hasNext()) {
      UniChatMessage next = iterator.next();
      if(next.getFiles()!=null) {
        next.setContent("Omit image").setFiles(null);
      }
    }

    String skipNullJson = JsonUtils.toSkipNullJson(messages);
    Row row = Row.by("id", SnowflakeIdUtils.id()).set("group_id", uniChatRequest.getGroupId())
        .set("group_name", uniChatRequest.getGroupName())
        //
        .set("task_id", uniChatRequest.getTaskId()).set("task_name", uniChatRequest.getTaskName())
        //
        .set("provider", uniChatRequest.getPlatform()).set("api_key", uniChatRequest.getApiKey())
        .set("model", uniChatRequest.getModel())
        //
        .set("usage", uniChatResponse.getUsage() != null ? JsonUtils.toSkipNullJson(uniChatResponse.getUsage()) : null)
        //
        .set("system_prompt", systemPrompt).set("messages", skipNullJson)
        //
        .set("elapsed", elapsed);
    try {
      ChatResponseMessage message = uniChatResponse.getMessage();
      if (message != null) {
        String content = message.getContent();
        row.set("content", content);
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    try {
      super.save(row);
      log.debug("Saved LLM usage for task: {}", uniChatRequest.getTaskId());
    } catch (Exception e) {
      log.error("Failed to save LLM usage for task: {}", uniChatRequest.getTaskId(), e);
    }
  }
}