package com.litongjava.tio.boot.admin.services;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.kit.Kv;
import com.litongjava.data.model.DbJsonBean;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.jfinal.plugin.activerecord.Db;
import com.litongjava.jfinal.plugin.activerecord.Record;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CollegeDocumentUploadService {
  
  public void afterSaveOrUpdate(String f, Kv kv, DbJsonBean<Kv> dbJsonBean) {
    Kv outputKv = Aop.get(DocumentService.class).generateAndSavePDFthumbnail("sjsu/documents", kv);
    List<Kv> images = new ArrayList<>();
    images.add(outputKv);

    Record record = new Record();
    record.set("images", images);
    Long longId = dbJsonBean.getData().getLong("id");
    record.set("id", longId);

    String[] jsonFields = { "images" };
    boolean update = Db.update(f, "id", record, jsonFields);
    log.info("update result:{},{},{}", f, longId, update);
  }

}
