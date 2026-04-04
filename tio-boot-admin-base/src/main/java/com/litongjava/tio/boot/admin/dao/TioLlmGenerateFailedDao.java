package com.litongjava.tio.boot.admin.dao;

import com.litongjava.tio.boot.admin.dto.TioLlmGenerateFailed;
import com.litongjava.tio.utils.snowflake.SnowflakeIdUtils;

import nexus.io.chat.UniChatRequest;
import nexus.io.db.DbJsonObject;
import nexus.io.db.activerecord.Row;
import nexus.io.db.base.DbBase;
import nexus.io.exception.GenerateException;

public class TioLlmGenerateFailedDao extends DbBase {

  @Override
  public String getTableName() {
    return TioLlmGenerateFailed.tableName;
  }

  public boolean save(UniChatRequest uniChatRequest, GenerateException e, String stackTrace) {
    Long groupId = uniChatRequest.getGroupId();
    String groupName = uniChatRequest.getGroupName();
    Long taskId = uniChatRequest.getTaskId();
    String taskName = uniChatRequest.getTaskName();
    String apiKey = uniChatRequest.getApiKey();

    String provider = uniChatRequest.getPlatform();
    String urlPerfix = e.getUrlPerfix();
    String requestJson = e.getRequestBody();
    Integer statusCode = e.getStatusCode();
    String responseBody = e.getResponseBody();

    return this.save(groupId, groupName, taskId, taskName, apiKey, provider, urlPerfix, requestJson, statusCode,
        responseBody, stackTrace, e.getElapsed());
  }

  public boolean save(Long groupId, String groupName, Long taskId, String taskName, String apiKey, String provider,
      String urlPerfix, String requestJson, Integer statusCode, String responseBody, String stackTrace,Long elapsed) {
    Row row = Row.by("id", SnowflakeIdUtils.id()).set("group_id", groupId).set("group_name", groupName)
        .set("task_id", taskId).set("task_name", taskName)
        //
        .set("api_key", apiKey).set("provider", provider)
        //
        .set("request_url", urlPerfix).set("request_body", new DbJsonObject(requestJson))
        //
        .set("response_code", statusCode).set("response_body", new DbJsonObject(responseBody))
        //
        .set("exception", stackTrace).set("elapsed", elapsed);  

    return super.save(row);
  }
}